package com.alterjoc.radar.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.alterjoc.radar.client.TozibabaService.AsyncCallback;
import com.alterjoc.radar.client.log.Log;
import com.alterjoc.radar.common.data.TopicInfo;
import org.jboss.capedwarf.common.data.Status;
import org.jboss.capedwarf.common.data.StatusInfo;

/**
 * User: Dejan
 * Date: 18.8.2010
 * Time: 19:59:55
 */
public class AddTopicActivity extends Activity {

    private EditText nameET;
    private EditText descriptionET;
    private Dialog confirmDialog;
    private Handler mHandler = new Handler();
    private Spinner defaultTimeSpinner;
    private TopicInfo newTopic;
    private ImageButton publishButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        setContentView(layout);

        nameET = new EditText(this);
        descriptionET = new EditText(this);
        descriptionET.setOnKeyListener(new View.OnKeyListener(){
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                resetIsDataReady();
                return false;
            }
        });

        publishButton = new ImageButton(this);
        publishButton.setEnabled(false);
        publishButton.setImageDrawable(getResources().getDrawable(Tools.getImage(AddTopicActivity.this, "send")));
        publishButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!Tools.isUserLoggedIn(AddTopicActivity.this, true)) {
                    return;
                }
                confirmDialog = createAlert(nameET.getText().toString());
                confirmDialog.setOwnerActivity(AddTopicActivity.this);
                confirmDialog.show();
            }
        });

        TextView label = new TextView(this);
        nameET.setHint(R.string.channel_name_hint);
        nameET.setSingleLine(true);
        nameET.setOnKeyListener(new View.OnKeyListener(){
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                resetIsDataReady();
                return false;
            }
        });
        label.setText(R.string.channel_name);
        TextView warningLabel = new TextView(this);
        warningLabel.setText(R.string.warning_moderated);
        warningLabel.setTextColor(Color.YELLOW);
        layout.addView(warningLabel);
        layout.addView(label);
        layout.addView(nameET);
        label = new TextView(this);
        label.setText(R.string.topic_description);
        layout.addView(label);
        descriptionET.setHint(R.string.description_hint);
        descriptionET.setSingleLine(false);
        descriptionET.setLines(2);
        layout.addView(descriptionET);
        label = new TextView(this);
        label.setText(R.string.default_validity);
        String[] items = Tools.timeMap.keySet().toArray(new String[0]);
        SpinnerAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, items);
        defaultTimeSpinner  = new Spinner(this);
        defaultTimeSpinner.setAdapter(adapter);

        layout.addView(label);
        layout.addView(defaultTimeSpinner);
        layout.addView(publishButton);
        setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.tozibaba16);

        Tools.isUserLoggedIn(this, false);
    }

    private void resetIsDataReady(){
        Editable name = nameET.getText();
        Editable description = descriptionET.getText();
        publishButton.setEnabled(name != null && name.length() > 2 && name.length() < 51 && (description == null || description.length() < 100));
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

    private Dialog createAlert(final String topicName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_channel_title);
        builder.setMessage(getResources().getString(R.string.add_channel_warning) + topicName);
        builder.setCancelable(true)
                .setPositiveButton(R.string.confirm, Tools.wrapProtect(new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        TopicInfo topicInfo = new TopicInfo(topicName);
                        topicInfo.setDescription(descriptionET.getText().toString());
                        topicInfo.setExpirationDelta(Tools.timeMap.get((String)defaultTimeSpinner.getSelectedItem()));
                        newTopic = topicInfo;
                        showDialog(1);
                    }
                }))
                .setNegativeButton(R.string.cancel, Tools.wrapProtect(new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }));
        return builder.create();
    }

    @Override
    protected Dialog onCreateDialog(int id) {

        final ProgressDialog progress = new ProgressDialog(AddTopicActivity.this);
        progress.setMessage(getResources().getString(R.string.add_channel_in_progress));
        progress.setIndeterminate(true);

        TozibabaService.getInstance(this).createTopicAsync(newTopic, new AsyncCallback<StatusInfo>() {

           public void onSuccess(StatusInfo result) {
               if (result.getStatus() != Status.OK)
                   throw new RuntimeException("Status: " + result.getStatus());

               // Ob uspešnem zaključku akcije zapri dialog in PostEventActivity
               mHandler.post(new Runnable() {
                   public void run() {
                       progress.dismiss();
                       Toast toast = Toast.makeText(AddTopicActivity.this, R.string.channel_added, Toast.LENGTH_LONG);
                       toast.show();
                       AddTopicActivity.this.finish();
                   }
               });
           }

           public void onError(Throwable t) {
               Log.e("Tozibaba:AddTopic", "Failed to post new event to server: ", t);
               // Ob neuspešnem zaključku akcije zapri dialog
               mHandler.post(new Runnable() {
                   public void run() {
                       progress.dismiss();
                       Toast toast = Toast.makeText(AddTopicActivity.this, R.string.channel_error, Toast.LENGTH_LONG);
                       toast.show();
                   }
               });
           }
       });
        return progress;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Application.RESULT_EXIT) {
            setResult(Application.RESULT_EXIT);
            finish();
        }
    }
}
