package com.alterjoc.radar.client;

import static com.alterjoc.radar.common.Constants.TAG_LOCATION_MGR;
import static com.alterjoc.radar.common.Constants.TAG_SERVICE;
import static com.alterjoc.radar.common.Constants.TAG_SYNC;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import com.alterjoc.radar.client.database.DBAdapter;
import com.alterjoc.radar.client.database.DBAdapter.Listener;
import com.alterjoc.radar.client.log.Log;
import com.alterjoc.radar.client.sync.AnyChangeSynchronization;
import com.alterjoc.radar.client.sync.AutoArchiveEventsJob;
import com.alterjoc.radar.client.sync.CommentSynchronization;
import com.alterjoc.radar.client.sync.EventSynchronization;
import com.alterjoc.radar.client.sync.Synchronization;
import com.alterjoc.radar.client.sync.SynchronizationRun;
import com.alterjoc.radar.client.sync.SynchronizationRunner;
import com.alterjoc.radar.client.sync.TidyUpProximityAlertsJob;
import com.alterjoc.radar.client.sync.TimeSynchronization;
import com.alterjoc.radar.client.sync.TopicSubscriptionSynchronization;
import com.alterjoc.radar.client.sync.TopicSynchronization;
import com.alterjoc.radar.common.data.CommentInfo;
import com.alterjoc.radar.common.data.EventInfo;
import com.alterjoc.radar.common.data.TopicInfo;
import com.alterjoc.radar.connect.server.ServerProxy;
import org.jboss.capedwarf.common.data.LoginInfo;
import org.jboss.capedwarf.common.data.Status;
import org.jboss.capedwarf.common.data.StatusInfo;
import org.jboss.capedwarf.common.data.UserInfo;
import org.jboss.capedwarf.connect.server.ServerProxyFactory;

public class TozibabaService extends AbstractService {

    private Timer timer;

    private SynchronizationRunner syncRunner;

    private UpSyncRunner upSyncRunner;

    private Listener proximityListener;
    
    private SynchronizationRun syncRun;

    private AnyChangeSynchronization anyChangeSync;
    
    /**
     * Currently running service instance
     */
    private static TozibabaService instance;


    /**
     * Get currently running service instance.
     * Service is supposed to be started in the MAIN LAUNCHER activity,
     * and stopped when using Exit menu option.
     *
     * @param context the context
     * @return TozibabaService instance of currently running service or null if service is not running
     */
    public static TozibabaService getInstance(Context context) {
        if (instance == null) {
            if (context != null) {
                Intent intent = new Intent(TozibabaService.ACTION_FOREGROUND);
                intent.setClass(context, TozibabaService.class);
                context.startService(intent);
            } else {
                // TODO :: in some cases we dont get context, that is in two calls - see sync classes
            }
        }
        return instance;
    }

    @Override
    void processSyncIntent() {
        Toast toast = Toast.makeText(this, R.string.data_refresh_background, Toast.LENGTH_LONG);
        toast.show();
        syncNow();
    }

    protected void doStart() {
        CurrentContext.setCurrent(this);
        try
        {
           if (instance != null) {
               Log.w(TAG_SERVICE, "Ignoring doStart() - service already started");
               return;
           }
           Log.i(TAG_SERVICE, "doStart");
           instance = this;
           LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
           Application.MasterLocationListener masterLocListener = Application.getInstance().getMasterLocationListener();
           List<String> providers = locationManager.getProviders(true);
           for (String provider : providers) {
               locationManager.requestLocationUpdates(provider, 5000, 100, masterLocListener);
               Log.i(TAG_LOCATION_MGR, "added LocationManager updates for provider '" + provider + "', masterListener: " + masterLocListener);
           }
   
           // we don't wait for the thread to start - might be a problem - we may see NPEs
           upSyncRunner = new UpSyncRunner();
           upSyncRunner.start();
   
           // run synchronization
           timer = new Timer("Sync Thread", true);
           syncRunner = new SynchronizationRunner(this, getSyncRun());
           timer.schedule(syncRunner, 0, getSyncPeriod());
           
           proximityListener = new DBAdapter.Listener() {
   
               public void commentsUpdated(List<CommentInfo> comments)
               {}
      
               public void eventsUpdated(List<EventInfo> events)
               {
                  Application.getInstance().getMasterLocationListener().addProximityAlerts(TozibabaService.this, events);
               }
      
               public void topicsUpdated(List<TopicInfo> topics)
               {}
           };
           Application.getInstance().getDBHelper(this).addListener(proximityListener);
        }
        finally
        {
           CurrentContext.cleanup();
        }
    }


    protected SynchronizationRun getSyncRun()
    {
       if (syncRun == null)
       {
          syncRun = new SynchronizationRun();
          LinkedList<Synchronization> syncs = new LinkedList<Synchronization>();
          syncs.add(new TimeSynchronization());
          syncs.add(getAnyChangeSynchronization());
          syncs.add(new TopicSynchronization());
          syncs.add(new EventSynchronization());
          syncs.add(new CommentSynchronization());
          syncs.add(new TidyUpProximityAlertsJob());
          syncs.add(new AutoArchiveEventsJob());
          syncRun.setSynchronizations(syncs);
       }
       return syncRun;
    }

   private Synchronization getAnyChangeSynchronization()
   {
      if (anyChangeSync == null)
      {
         anyChangeSync = new AnyChangeSynchronization();
      }
      return anyChangeSync;
   }

   protected void doStop() {
      CurrentContext.setCurrent(this);
      try
      {
        if (instance == null) {
            Log.w(TAG_SERVICE, "Ignoring doStop() - Stopped already");
            return;
        }
        Log.i(TAG_SERVICE, "doStop");
        instance = null;
        if (timer != null)
            timer.cancel();
        if (syncRunner != null)
            syncRunner.interrupt();

        upSyncRunner.quit();        
        
        Application application = Application.getInstance();
        application.getDBHelper(this).removeListener(proximityListener);
        
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Application.MasterLocationListener masterLocListener = application.getMasterLocationListener();
        locationManager.removeUpdates(masterLocListener);
        Log.i(TAG_LOCATION_MGR, "removed LocationManager updates for masterListener: " + masterLocListener);
        masterLocListener.clearProximityAlerts(this);
        masterLocListener.clearAllListeners();        
        
        ServerProxyFactory.shutdown(ServerProxy.class);
        application.closeDBHelper();
      }
      finally
      {
         CurrentContext.cleanup();
      }
    }


    private long getSyncPeriod() {
        long updPeriod = Long.valueOf(Application.getInstance().getPreferences(this).getEffectiveUpdatePeriodValue());
        return updPeriod * 60000;
    }

    // This method is called from onPreferenceChanged listener which is before the new period value has
    // been commited to Preferences ... that's why we pass period and don't use getSyncPeriod()

    public void reinitSyncTimer(long period, long waitToStart) {
        syncRunner.cancel();
        syncRunner.interrupt();
        syncRunner = new SynchronizationRunner(this, getSyncRun());
        if (waitToStart == -1)
            waitToStart = period;
        timer.schedule(syncRunner, waitToStart, period);
    }

    public void syncNow() {
        //timer.schedule(new SynchronizationRunner(), new Date());
       reinitSyncTimer(getSyncPeriod(), 0);
    }
    
    public void syncCommentsNow() {
       SynchronizationRun run = new SynchronizationRun();
       LinkedList<Synchronization> syncs = new LinkedList<Synchronization>();
       syncs.add(getAnyChangeSynchronization());
       syncs.add(new CommentSynchronization());
       run.setSynchronizations(syncs);

       SynchronizationRunner commentsRunner = new SynchronizationRunner(this, run);
       timer.schedule(commentsRunner, 0);
    }

    public synchronized void triggerSubscriptionUpSync() {
        upSyncRunner.execute(new RetryRunnable() {
            public void run() {
                try {
                    TopicSubscriptionSynchronization syncJob = new TopicSubscriptionSynchronization();
                    syncJob.sync();
                    final List<TopicInfo> newTopics = syncJob.getAndCleanNewTopics();
                    if (newTopics.size() > 0)
                    {
                        syncNow();
                        
                        SynchronizationRun run = new SynchronizationRun();
                        LinkedList<Synchronization> syncs = new LinkedList<Synchronization>();
                        syncs.add(new EventSynchronization() {
                            {
                                setAnyChangeCheck(false);
                                setTopicsForSync(newTopics);
                                setSinceTs(0);
                            }
                            @Override
                            public void init() {}
                           
                            @Override
                            public void updateTs(long val) {}                                                        
                        });
                        syncs.add(new CommentSynchronization() {
                           {
                               setAnyChangeCheck(false);
                               setTopicsForSync(newTopics);
                               setSinceTs(0);
                           }
                           @Override
                           public void init() {}
                           
                           @Override
                           public void updateTs(long val) {}                                                        
                        });
                        run.setSynchronizations(syncs);

                        SynchronizationRunner commentsRunner = new SynchronizationRunner(TozibabaService.this, run);
                        timer.schedule(commentsRunner, 0);
                    }
                }
                catch (Exception e) {
                    Log.e(TAG_SYNC, "Up-sync of topic subscriptions failed: ", e);
                    //incrementRetryInterval();
                    //Log.i(TAG_SYNC, "Scheduling retry in " + interval + " ms");
                    //upSyncRunner.executeDelayed(this, interval);
                }
            }
        });
    }

    /**
     * Creates or logs in the user on the server and returns security token to use for subsequent logins.
     * If attempting login, and not user creation, {@UserInfo#status} needs to be set to {@com.alterjoc.radar.common.data.Status#LOGIN}.
     * @param userInfo the username
     * @return login info
     */
    public LoginInfo createUser(UserInfo userInfo) {
        ServerProxy proxy = ServerProxyFactory.create(ServerProxy.class);
        return proxy.profileCreateUser(userInfo);
    }

    public void createUserAsync(final UserInfo userInfo, AsyncCallback<LoginInfo> r) {
        upSyncRunner.execute(new Executor<LoginInfo>(r) {
            LoginInfo execute() {
                return createUser(userInfo);
            }
        });

    }    

    private Status requestPasswordRecovery(String user)
    {
       ServerProxy proxy = ServerProxyFactory.create(ServerProxy.class);
       return proxy.serverRecovery(user);
    }
    
    public void requestPasswordRecoveryAsync(final String user, AsyncCallback<Status> r)
    {
       upSyncRunner.execute(new Executor<Status>(r) {

         @Override
         Status execute()
         {
            return requestPasswordRecovery(user);
         }          
       });
    }    
    
    public boolean profileAddTopic(Long topicId)
    {
       ServerProxy proxy = ServerProxyFactory.create(ServerProxy.class);
       Boolean res = proxy.profileAddTopic(topicId);
       return Boolean.TRUE.equals(res);
    }

    public void profileAddTopicAsync(final Long topicId, AsyncCallback<Boolean> r) {
        upSyncRunner.execute(new Executor<Boolean>(r) {
            Boolean execute() {
                return profileAddTopic(topicId);
            }
        });
    }

    private StatusInfo createTopic(TopicInfo info) {
        return ServerProxyFactory.create(ServerProxy.class).profileAddNewTopic(info);
    }

    public void createTopicAsync(final TopicInfo info, AsyncCallback<StatusInfo> r) {
        upSyncRunner.execute(new Executor<StatusInfo>(r) {
            StatusInfo execute() {
                return createTopic(info);
            }
        });
    }

    public boolean profileRemoveTopic(Long topicId) {
        ServerProxy proxy = ServerProxyFactory.create(ServerProxy.class);
        Boolean res = proxy.profileRemoveTopic(topicId);
        return Boolean.TRUE.equals(res);
    }

    public void profileRemoveTopicAsync(final Long topicId, AsyncCallback<Boolean> r) {
        upSyncRunner.execute(new Executor<Boolean>(r) {
            Boolean execute() {
                return profileRemoveTopic(topicId);
            }
        });
    }

    private StatusInfo postEvent(EventInfo event) {
        return ServerProxyFactory.create(ServerProxy.class).profilePostEvent(event);
    }

    public void postEventAsync(final EventInfo event, AsyncCallback<StatusInfo> r) {
        upSyncRunner.execute(new Executor<StatusInfo>(r) {
            StatusInfo execute() {
                return postEvent(event);
            }
        });
    }

    public StatusInfo postComment(CommentInfo comment) {
        return ServerProxyFactory.create(ServerProxy.class).profileAddComment(comment);
    }

    abstract class Executor<T> implements Runnable {
        AsyncCallback<T> callback;

        Executor(AsyncCallback<T> cb) {
            this.callback = cb;
        }

        public void run() {
            T ret;
            try {
                ret = execute();
                callback.onSuccess(ret);
            }
            catch (Throwable th) {
                callback.onError(th);
            }
        }

        abstract T execute();
    }


    public interface AsyncCallback<T> {
        void onSuccess(T result);

        void onError(Throwable t);
    }


    class UpSyncRunner extends Thread {

        private Handler handler;

        public void run() {
            Looper.prepare();
            try {
                handler = new Handler();
                Looper.loop();
            }
            finally {
                handler = null;
            }
        }

        public void executeDelayed(Runnable job, long interval) {
            Handler h = handler;
            if (h != null)
                h.postDelayed(job, interval);
        }

        public void execute(Runnable job) {
            Handler h = handler;
            if (h != null)
                h.post(job);
        }

        public void quit() {
            Handler h = handler;
            if (h != null)
                h.getLooper().quit();
        }
    }

    static abstract class RetryRunnable implements Runnable {
        protected long interval = 30000;

        public long incrementRetryInterval() {
            interval *= 2;
            return interval;
        }

    }
}
