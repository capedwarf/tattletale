package com.alterjoc.radar.client;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import com.alterjoc.radar.client.log.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.alterjoc.radar.client.adapters.TopicAdapter;
import com.alterjoc.radar.client.database.DBAdapter;
import com.alterjoc.radar.common.Constants;
import com.alterjoc.radar.common.data.TopicInfo;

import java.util.List;


/**
 * User: Dejan
 * Date: 23.8.2010
 * Time: 18:04:03
 */
public class EditSubscriptionsActivity extends ListActivity {

   private boolean subscriptionChanges;
   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_LEFT_ICON);

        DBAdapter dbAdapter = Application.getInstance().getDBHelper(this);
        List<TopicInfo> topics = dbAdapter.getNonSystemTopics();
        Tools.reorderTopics(topics);
        ListAdapter adapter = new TopicAdapter(EditSubscriptionsActivity.this, android.R.layout.simple_list_item_multiple_choice, topics, false);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        setListAdapter(adapter);
        for (int i = 0; i < topics.size(); i++) {
            getListView().setItemChecked(i, topics.get(i).isUserSubscribed());
        }
        setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.tozibaba16);
        Tools.isUserLoggedIn(this, false);
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        if (!Tools.isUserLoggedIn(EditSubscriptionsActivity.this, true)) {
            return;
        }
        super.onListItemClick(l, v, position, id);
        long topicId = ((TopicInfo) getListAdapter().getItem(position)).getId();
        DBAdapter dbAdapter = Application.getInstance().getDBHelper(EditSubscriptionsActivity.this);
        TopicInfo topic = dbAdapter.getTopicForId(topicId);
        if (topic == null)
        {
            Log.w(Constants.TAG_SUBSCRIPTIONS, "Failed to load topic for id: " + topicId + " from localDB");
        }
        else
        {
           boolean newVal = l.isItemChecked(position);
           if (newVal != topic.isUserSubscribed())
           {
              topic.setUserSubscribed(newVal);
              dbAdapter.saveEntity(topic);
              subscriptionChanges = true;
           }
        }         
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Tools.buildMainMenu(menu, this, false, true, false, false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return Tools.processOptionsItemSelected(item, this);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Application.RESULT_EXIT) {
            setResult(Application.RESULT_EXIT);
            finish();
        }
    }
    
    @Override
    public void onPause()
    {
       super.onPause();
       if (subscriptionChanges)
       {
          subscriptionChanges = false;
          TozibabaService.getInstance(this).triggerSubscriptionUpSync();
       }
    }
}
