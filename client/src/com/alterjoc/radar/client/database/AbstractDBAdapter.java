package com.alterjoc.radar.client.database;

import android.content.Context;
import com.alterjoc.radar.client.database.data.SyncInfo;
import com.alterjoc.radar.client.log.Log;
import com.alterjoc.radar.client.sync.TimestampedSynchronization;
import com.alterjoc.radar.client.sync.TopicSynchronization;
import com.alterjoc.radar.common.data.CommentInfo;
import com.alterjoc.radar.common.data.EventInfo;
import com.alterjoc.radar.common.data.ImageInfo;
import com.alterjoc.radar.common.data.TopicInfo;
import org.jboss.capedwarf.common.data.Type;
import org.jboss.capedwarf.common.dto.Identity;
import org.jboss.capedwarf.common.dto.Timestamped;
import org.jboss.capedwarf.common.sql.SQLObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static com.alterjoc.radar.common.Constants.TAG_SYNC;

/**
 * Abstract DB adapter.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractDBAdapter implements DBAdapter
{
   private Context context;
   
   private LinkedList<Listener> listeners = new LinkedList<Listener>();

   protected AbstractDBAdapter(Context context)
   {
      if (context == null)
         throw new IllegalArgumentException("Null context");
      this.context = context;
   }

   protected Context getContext()
   {
      return context;
   }

   protected static <T> T getSingleResult(List<T> results)
   {
      return (results == null || results.isEmpty()) ? null : results.get(0);
   }

   // --- Tx lifecycle

   protected abstract void begin();

   protected abstract void end();

   protected abstract void commit();

   protected abstract void rollback();

   // --- CRUD

   protected abstract long store(SQLObject object);

   protected abstract int update(SQLObject object);

   protected abstract int delete(SQLObject entity);

   protected abstract <T extends Identity> T findEntity(Long id, Class<T> entityClass);

   protected abstract <T> List<T> getAll(Class<T> entityClass);

   protected abstract <T extends Timestamped> T getLatestEntity(Class<T> entityClass);

   /**
    * Close adapter.
    */
   public abstract void close();

   // -------------------------------------------

   protected long saveObject(SQLObject entity, boolean tx)
   {
      if (tx)
      {
         begin();
      }
      try
      {
         long result = store(entity);
         if (tx)
         {
            commit();
         }
         return result;
      }
      catch (Throwable t)
      {
         if (tx)
         {
            rollback();
         }
         throw new RuntimeException(t);
      }
      finally
      {
         if (tx)
         {
            end();
         }
      }
   }

   public long saveEntity(SQLObject entity)
   {
      return saveObject(entity, true);
   }

   public int updateEntity(SQLObject entity)
   {
      begin();
      try
      {
         int result = update(entity);
         commit();
         return result;
      }
      catch (Throwable t)
      {
         rollback();
         throw new RuntimeException(t);
      }
      finally
      {
         end();
      }
   }

   /**
    * Use this method for saving lists of objects. Commit will only be executed once, at the end of saving.
    *
    * @param entities the entities
    */
   public void saveEntities(List<? extends SQLObject> entities)
   {
      if (entities == null || entities.isEmpty())
         return;

      begin();
      try
      {
         for (SQLObject entity : entities)
         {
            saveObject(entity, false);
         }
         commit();
      }
      catch (Throwable t)
      {
         rollback();
         throw new RuntimeException(t);
      }
      finally
      {
         end();
      }
   }

   public void deleteEntities(List<? extends SQLObject> entities)
   {
      if (entities == null || entities.isEmpty())
         return;

      begin();
      try
      {
         for (SQLObject entity : entities)
         {
            delete(entity);
         }
         commit();
      }
      catch (Throwable t)
      {
         rollback();
         throw new RuntimeException(t);
      }
      finally
      {
         end();   
      }
   }

   public void putSyncInfo(SyncInfo info)
   {
      saveObject(info, true);
   }

   public EventInfo readEventInfo(long id)
   {
      return findEntity(id, EventInfo.class);
   }

   public void insertImage(ImageInfo imageInfo, byte[] bytes)
   {
      imageInfo.readFromBytesIntoDB(bytes);
      saveObject(imageInfo, true);
   }

   public void setEventNotified(long id)
   {
      EventInfo event = findEntity(id, EventInfo.class);
      if (event == null)
         return;

      event.setUserNotified(true);
      updateEntity(event);
   }

   public TopicInfo getTopicForId(Long id)
   {
      return findEntity(id, TopicInfo.class);
   }

   public List<TopicInfo> getAllTopics()
   {
      return getAll(TopicInfo.class);
   }

   public boolean isEventUserNotified(String id)
   {
      EventInfo event = findEntity(Long.parseLong(id), EventInfo.class);
      return event != null && event.isUserNotified();
   }

   public void updateTopics(TopicSynchronization sync, List<TopicInfo> topics) throws InterruptedException
   {
      for (TopicInfo current : topics)
      {
         sync.getSyncRun().getSyncRunner().checkInterrupted();

         Log.d(TAG_SYNC, "Received " + current.toShortString());
         TopicInfo found = getTopicForId(current.getId());

         if (found == null)
         {
            Log.d(TAG_SYNC, "Creating new TopicInfo: " + current.getName());
            byte[] photo = current.getPhoto();
            if (photo == null)
            {
               Log.d(TAG_SYNC, "No photo on topic: " + current.getName() + ".");
               //ImageInfo img = sync.getDefaultImage(this);
               //current.setImage(img);
            }
            else
            {
               Log.d(TAG_SYNC, "Topic contains a photo: " + current.getName() + ". Creating new ImageInfo ...");
               // create ImageInfo
               ImageInfo img = createImageInfo();
               img.setName(current.getName());
               insertImage(img, photo);
               current.setImage(img);
               current.setImagePk(img.getPk());
            }
            current.initUserSubscribed();
            saveEntity(current);
         }
         else
         {
            Log.d(TAG_SYNC, "Updating existing TopicInfo: " + current.getName());

            // but then again - we don't update at all
            // as we only add new
            found.update(current);
            updateEntity(found);
         }
      }
   }

   public void updateEvents(TimestampedSynchronization sync, List<EventInfo> events) throws InterruptedException
   {
      LinkedList<EventInfo> updated = new LinkedList<EventInfo>();
      
      for (EventInfo current : events)
      {
         current.autoArchiveIfExpired();
         sync.getSyncRun().getSyncRunner().checkInterrupted();

         Log.d(TAG_SYNC, "Received " + current.toShortString());
         EventInfo found = findEntity(current.getId(), EventInfo.class);

         if (found == null)
         {
            Log.d(TAG_SYNC, "Creating new EventInfo: " + current.getTitle());

            // Attach TopicInfo
            Long id = current.getTopicId();
            if (id == null)
            {
               Log.e(TAG_SYNC, "Not writing to localDB event without topicId: " + current.getTopicId());
               continue;
            }

            TopicInfo topic = current.getTopicInfo();
            if (topic == null)
            {
               topic = getTopicForId(id);
               if (topic == null)
               {
                  Log.w(TAG_SYNC, "Not writing to localDB event with topicId: " + current.getTopicId() + " - no such topic");
                  continue;
               }
               current.setTopicInfo(topic);
            }

            if (Type.SYSTEM == current.getType())
            {
               // TODO -- update location   
            }

            saveEntity(current);
            updated.add(current);
         }
         else
         {
            Log.d(TAG_SYNC, "Updating existing EventInfo: " + current.getTitle());
            // but then again - we don't update at all
            // as we only add new
            //found.update(current);
            updateEntity(found);
            updated.add(found);
         }
      }

      if (updated.size() > 0)
         notifyEventsUpdated(updated);      
   }

   public void updateComments(TimestampedSynchronization sync, List<CommentInfo> comments) throws InterruptedException
   {
      LinkedList<CommentInfo> updated = new LinkedList<CommentInfo>();

      for (CommentInfo current : comments)
      {
         sync.getSyncRun().getSyncRunner().checkInterrupted();

         Log.d(TAG_SYNC, "Received " + current.toShortString());
         CommentInfo found = findEntity(current.getId(), CommentInfo.class);

         if (found == null)
         {
            // check if event exists
            EventInfo event =  findEntity(current.getEventId(), EventInfo.class);
            if (event == null)
            {
               Log.d(TAG_SYNC, "Ignoring CommentInfo - we don't have an event for it: " + current.toShortString());
               continue;
            }
             // Set it to unread
            event.setDetailsRead(false);
            Log.d(TAG_SYNC, "Creating new CommentInfo: " + current.getComment());
            saveEntity(current);
            saveEntity(event);
            updated.add(current);
         }
         else
         {
            // but then again - we don't update at all
            // as we only add new
            //found.update(current);
            Log.d(TAG_SYNC, "Updating existing CommentInfo: " + current.getComment());
            updateEntity(found);
            updated.add(found);
         }
      }

      if (updated.size() > 0)
         notifyCommentsUpdated(updated);      
   }

   public TopicInfo getLatestTopicInfo()
   {
      return getLatestEntity(TopicInfo.class);
   }

   public EventInfo getLatestEventInfo()
   {
      return getLatestEntity(EventInfo.class);
   }

   public CommentInfo getLatestCommentInfo()
   {
      return getLatestEntity(CommentInfo.class);
   }
   
   @SuppressWarnings("unchecked")
   protected void notifyListeners(ListenerVisitor visitor)
   {
      LinkedList<Listener> lss;
      synchronized (this) 
      {
         lss = (LinkedList<Listener>) listeners.clone();
      }
      
      for(Listener l: lss)
      {
         try
         {
            visitor.handle(l);
         }
         catch (Exception e)
         {
            Log.w(TAG_SYNC, "IGNORED: Exception in notification listener: ", e);
         }
      }
   }
   
   protected void notifyEventsUpdated(final List<EventInfo> events)
   {
      notifyListeners(new ListenerVisitor() {
         public void handle(Listener l)
         {            
            l.eventsUpdated(events);
         }
      });
   }
   
   protected void notifyCommentsUpdated(final List<CommentInfo> comments)
   {
      notifyListeners(new ListenerVisitor() {
         public void handle(Listener l)
         {            
            l.commentsUpdated(comments);
         }
      });
   }
   
   @SuppressWarnings("unchecked")
   public synchronized void addListener(Listener lis)
   {      
      LinkedList<Listener> lss = (LinkedList<Listener>) listeners.clone();
      for (Listener l: lss)
      {
         if (l == lis)
            return;
      }
      lss.add(lis);
      listeners = lss;
   }
   
   @SuppressWarnings("unchecked")
   public synchronized Listener removeListener(Listener lis)
   {
      LinkedList<Listener> lss = (LinkedList<Listener>) listeners.clone();
      Iterator<Listener> it = lss.iterator();
      while (it.hasNext())
      {
         Listener l = it.next();
         if (l == lis)
         {
            it.remove();
            listeners = lss;
            return l;
         }
      }
      return null;
   }
   
}
