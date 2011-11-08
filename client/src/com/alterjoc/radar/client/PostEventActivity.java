package com.alterjoc.radar.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.alterjoc.radar.client.TozibabaService.AsyncCallback;
import com.alterjoc.radar.client.adapters.TopicAdapter;
import com.alterjoc.radar.client.database.DBAdapter;
import com.alterjoc.radar.client.log.Log;
import com.alterjoc.radar.common.data.EventInfo;
import com.alterjoc.radar.common.data.TopicInfo;
import org.jboss.capedwarf.common.data.Status;
import org.jboss.capedwarf.common.data.StatusInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.List;

import static com.alterjoc.radar.common.Constants.TAG_POST_EVENT;

/**
 * User: Dejan
 * Date: 17.8.2010
 * Time: 18:21:33
 */
public class PostEventActivity extends Activity {

    private boolean isAfterResume;

    private DecimalFormat format = new DecimalFormat("#0.000000");

    private EditText titleEdit;
    private EditText commentEdit;
    private Spinner topicSpinner;
    private Spinner deltaTimeSpinner;
    private EditText locationEdit;
    private TextView locationText;

    private Location chosenLocation;

    private Dialog confirmDialog;
    private ImageButton takePhotoButton;
    private Bitmap bigPhotoBitmap;
    private Bitmap smallPhotoBitmap;

    private static final int LOCATION_PICKER_ACTIVITY = 1;
    private static final int CAMERA_ACTIVITY = 2;

    private static final int PICK_FILE = 3;

    private Handler mHandler = new Handler();

    private EventInfo newEvent;

    private ImageButton publishButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the service started:
        TozibabaService.getInstance(this);
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        setContentView(layout);
        setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.tozibaba16);

        deltaTimeSpinner = new Spinner(this);
        String[] items = Tools.timeMap.keySet().toArray(new String[0]);
        SpinnerAdapter timeAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, items);
        deltaTimeSpinner = new Spinner(this);
        deltaTimeSpinner.setAdapter(timeAdapter);

        DBAdapter adapter = Application.getInstance().getDBHelper(PostEventActivity.this);
        List<TopicInfo> topics = adapter.getSubscribedTopics();
        Tools.reorderTopics(topics);

        TopicAdapter topicAdapter = new TopicAdapter(PostEventActivity.this, android.R.layout.simple_spinner_item, topics, true);
        topicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        topicSpinner = new Spinner(this);
        topicSpinner.setAdapter(topicAdapter);
        topicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!isAfterResume) {
                    Object selected = topicSpinner.getItemAtPosition(i);
                    long delta = ((TopicInfo) selected).getExpirationDelta();
                    int count = deltaTimeSpinner.getAdapter().getCount();
                    for (int j = 0; j < count; j++) {
                        Object item = deltaTimeSpinner.getItemAtPosition(j);
                        Long time = Tools.timeMap.get(item);
                        if (delta - time == 0) {
                            deltaTimeSpinner.setSelection(j);
                            break;
                        }
                    }
                }
                isAfterResume = false;
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                // Should never happen
            }
        });

        topicSpinner.setEnabled(!topicAdapter.isEmpty());

        commentEdit = new EditText(this);
        commentEdit.setHint(R.string.additional_comment);
        commentEdit.setSingleLine(false);
        commentEdit.setGravity(Gravity.TOP);
        commentEdit.setLines(2);
        commentEdit.setTextSize(Tools.getButtonsFontSize(this));

        titleEdit = new EditText(this);
        titleEdit.setHint(R.string.short_description);
        titleEdit.setSingleLine(true);
        titleEdit.setTextSize(Tools.getButtonsFontSize(this));

        publishButton = new ImageButton(this);
        publishButton.setImageDrawable(getResources().getDrawable(Tools.getImage(PostEventActivity.this, "send")));
        publishButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!Tools.isUserLoggedIn(PostEventActivity.this, true)) {
                    return;
                }
                StringBuilder builder = new StringBuilder(((TopicInfo) topicSpinner.getSelectedItem()).getName());
                builder.append("\n");
                builder.append(titleEdit.getText());
                builder.append("\n");
                builder.append(commentEdit.getText());
                builder.append("\n");
                builder.append(locationEdit.getText());

                confirmDialog = createAlert(new String(builder));
                confirmDialog.setOwnerActivity(PostEventActivity.this);
                confirmDialog.show();
            }
        });
        publishButton.setEnabled(!topicAdapter.isEmpty());

        takePhotoButton = new ImageButton(this);
        takePhotoButton.setImageDrawable(getResources().getDrawable(Tools.getImage(PostEventActivity.this, "addphoto")));
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (bigPhotoBitmap == null) {
                    captureImage();
                } else {
                    bigPhotoBitmap = null;
                    takePhotoButton.setImageDrawable(getResources().getDrawable(Tools.getImage(PostEventActivity.this, "addphoto")));
                }
            }
        });
        ImageButton choosePhotoButton = new ImageButton(this);
        choosePhotoButton.setImageDrawable(getResources().getDrawable(Tools.getImage(PostEventActivity.this, "folderphoto")));
        choosePhotoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent("com.alterjoc.radar.client.PICK_FILE");
                intent.putExtra("org.openintents.extra.TITLE", getResources().getString(R.string.choose_photo));
                intent.putExtra("org.openintents.extra.BUTTON_TEXT", getResources().getString(R.string.select));
                startActivityForResult(intent, PICK_FILE);
            }
        });

        locationEdit = new EditText(this);
        locationEdit.setSingleLine(true);
        locationEdit.setSelection(0);
        locationEdit.setTextSize(Tools.getButtonsFontSize(this));

        if (chosenLocation == null) {
            chosenLocation = Tools.getCurrentLocation(this);
        }
        String address = Tools.getAddress(chosenLocation.getLatitude(), chosenLocation.getLongitude(), this);
        locationEdit.setText(address);

        ImageButton locationPicker = new ImageButton(this);
        locationPicker.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    Intent intent = new Intent(PostEventActivity.this, Class.forName("com.alterjoc.radar.client.activities.locationpicker.LocationPickerActivity"));
                    intent.putExtra("address", locationEdit.getText().toString());
                    intent.putExtra("longitude", chosenLocation.getLongitude());
                    intent.putExtra("latitude", chosenLocation.getLatitude());
                    startActivityForResult(intent, LOCATION_PICKER_ACTIVITY);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        locationPicker.setImageDrawable(getResources().getDrawable(Tools.getImage(this, "map")));
        LinearLayout addressLayout = new LinearLayout(this);
        addressLayout.setOrientation(LinearLayout.HORIZONTAL);
        addressLayout.addView(locationEdit, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        addressLayout.addView(locationPicker, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 4));

        locationText = new TextView(this);
        locationText.setText(getResources().getString(R.string.location) + " (" + format.format(chosenLocation.getLongitude()) + ", " + format.format(chosenLocation.getLatitude()) + "):");
        LinearLayout firstRow = new LinearLayout(this);
        firstRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout topicLayout = new LinearLayout(this);
        topicLayout.setOrientation(LinearLayout.VERTICAL);
        TextView label = new TextView(this);
        label.setText(getResources().getString(R.string.channel) + ":");
        topicLayout.addView(label);
        topicLayout.addView(topicSpinner);
        firstRow.addView(topicLayout);
        LinearLayout deltaTimeLayout = new LinearLayout(this);
        deltaTimeLayout.setOrientation(LinearLayout.VERTICAL);
        label = new TextView(this);
        label.setText(getResources().getString(R.string.valid_to) + ":");
        deltaTimeLayout.addView(label);
        deltaTimeLayout.addView(deltaTimeSpinner, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        firstRow.addView(deltaTimeLayout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.addView(firstRow);
        layout.addView(locationText);
        layout.addView(addressLayout);
        layout.addView(titleEdit);
        layout.addView(commentEdit);
        LinearLayout buttonsLayout = new LinearLayout(this);
        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonsLayout.addView(takePhotoButton, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        buttonsLayout.addView(choosePhotoButton, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        buttonsLayout.addView(publishButton, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.addView(buttonsLayout);


        Tools.isUserLoggedIn(this, false);

    }

    @Override
    protected void onResume() {
        super.onResume();
        isAfterResume = true;
        DBAdapter adapter = Application.getInstance().getDBHelper(PostEventActivity.this);
        List<TopicInfo> topics = adapter.getSubscribedTopics();
        Tools.reorderTopics(topics);

        TopicInfo selected = (TopicInfo) topicSpinner.getSelectedItem();
        int idx = Tools.findItemInList(selected, topics);
        if (idx != -1) {
            topicSpinner.setSelection(idx);
        }

        if (topics == null || topics.size() == 0) {
            Toast toast = Toast.makeText(PostEventActivity.this, R.string.publish_on_subscribed, Toast.LENGTH_LONG);
            toast.show();
        }


        SharedPreferences settings = Tools.getAppPreferences(this);
        int selectedIndex = settings.getInt("selectedIndex", 0);
        deltaTimeSpinner.setSelection(selectedIndex);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences settings = Tools.getAppPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("selectedIndex", deltaTimeSpinner.getSelectedItemPosition());
        editor.commit();
    }

    private Dialog createAlert(String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.publish_event);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        TextView tv = new TextView(PostEventActivity.this);
        tv.setGravity(Gravity.CENTER);
        tv.setText(getResources().getString(R.string.confirm_publish) + "  \n" + text + "\n", TextView.BufferType.SPANNABLE);
        linearLayout.addView(tv);
        if (bigPhotoBitmap != null) {
            ImageView imageView = new ImageView(this);
            BitmapDrawable image = new BitmapDrawable(bigPhotoBitmap);
            image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
            imageView.setImageDrawable(new BitmapDrawable(bigPhotoBitmap));
            linearLayout.addView(imageView);
        }
        builder.setView(linearLayout);
        createSpanSections((Spannable) tv.getText());
        builder.setCancelable(true)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        TopicInfo selectedTopic = (TopicInfo) topicSpinner.getSelectedItem();
                        newEvent = new EventInfo();
                        newEvent.setTopicId(selectedTopic.getId());
                        newEvent.setTopicInfo(selectedTopic);
                        newEvent.setTimestamp(System.currentTimeMillis());
                        newEvent.setTitle(titleEdit.getText().toString());
                        newEvent.setComment(commentEdit.getText().toString());
                        newEvent.setLatitude((int) (chosenLocation.getLatitude() * 1000000d));
                        newEvent.setLongitude((int) (chosenLocation.getLongitude() * 1000000d));
                        newEvent.setExpirationDelta(Tools.timeMap.get((String) deltaTimeSpinner.getSelectedItem()));
                        newEvent.setAddress(locationEdit.getText().toString());
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        if (bigPhotoBitmap != null) {
                            boolean success = bigPhotoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            try {
                                baos.flush();
                                baos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            // The following is a workaround for Android 1.5 - compress doesnt work there in some cases
                            // (check details here http://code.google.com/p/android/issues/detail?id=2092)
                            if (!success) {
                                Bitmap cloneImg = Bitmap.createScaledBitmap(bigPhotoBitmap, bigPhotoBitmap.getWidth(), bigPhotoBitmap.getHeight(), false);
                                baos = new ByteArrayOutputStream();
                                cloneImg.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                try {
                                    baos.flush();
                                    baos.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            newEvent.setPhoto(baos.toByteArray());
                            baos = new ByteArrayOutputStream();
                            success = smallPhotoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            try {
                                baos.flush();
                                baos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (!success) {
                                Bitmap cloneImg = Bitmap.createScaledBitmap(smallPhotoBitmap, smallPhotoBitmap.getWidth(), smallPhotoBitmap.getHeight(), false);
                                baos = new ByteArrayOutputStream();
                                cloneImg.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                try {
                                    baos.flush();
                                    baos.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            newEvent.setSmallPhoto(baos.toByteArray());
                        }
                        postEvent();
                        Toast toast = Toast.makeText(PostEventActivity.this, R.string.event_in_background, Toast.LENGTH_LONG);
                        toast.show();
                        PostEventActivity.this.finish();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }

    private void createSpanSections(Spannable str) {
        str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, 25, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        str.setSpan(new ForegroundColorSpan(Color.GREEN), 0, 25, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void postEvent() {
        // TODO :: TozibabaService.getInstance may return null, cause it takes a while for it to start
        TozibabaService.getInstance(this).postEventAsync(newEvent, new AsyncCallback<StatusInfo>() {
            public void onSuccess(StatusInfo result) {
                if (result.getStatus() != Status.OK)
                    throw new RuntimeException("Status: " + result.getStatus());

                // Trigger downSync to get the new event down ASAP
                TozibabaService.getInstance(PostEventActivity.this).syncNow();

                // Ob uspešnem zaključku akcije zapri dialog in PostEventActivity
                mHandler.post(new Runnable() {
                    public void run() {
                        Toast toast = Toast.makeText(PostEventActivity.this, getResources().getString(R.string.event_to_channel) +
                                " " + newEvent.getTopicInfo().getName() + " " + getResources().getString(R.string.sent_success), Toast.LENGTH_LONG);
                        toast.show();
                        PostEventActivity.this.finish();
                    }
                });
            }

            public void onError(Throwable t) {
                Log.e(TAG_POST_EVENT, "Failed to post new event to server: ", t);
                // Ob neuspešnem zaključku akcije zapri dialog
                mHandler.post(new Runnable() {
                    public void run() {
                        Toast toast = Toast.makeText(PostEventActivity.this, R.string.error_event_not_sent, Toast.LENGTH_LONG);
                        toast.show();
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Application.RESULT_EXIT) {
            setResult(Application.RESULT_EXIT);
            finish();
        }

        if (requestCode == LOCATION_PICKER_ACTIVITY && data != null && data.getExtras() != null) {
            double latitude = (double) data.getExtras().getInt("latitude") / 1000000d;
            double longitude = (double) data.getExtras().getInt("longitude") / 1000000d;
            String address = data.getExtras().getString("address");
            chosenLocation.setLatitude(latitude);
            chosenLocation.setLongitude(longitude);
            locationEdit.setText(address);
            locationEdit.setSelection(0);
            locationText.setText(getResources().getString(R.string.location) + " (" + format.format(chosenLocation.getLongitude()) + ", " + format.format(chosenLocation.getLatitude()) + "):");
        }
        if (requestCode == CAMERA_ACTIVITY) {
            if (resultCode == RESULT_CANCELED) {
                return;
            } else {
                InputStream is = null;
                File file = getTempFile();
                try {
                    is = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                //On HTC Hero the requested file will not be created. Because HTC Hero has custom camera
                //app implementation and it works another way. It doesn't write to a file but instead
                //it writes to media gallery and returns uri in intent. More info can be found here:
                //http://stackoverflow.com/questions/1910608/android-actionimagecapture-intent
                //http://code.google.com/p/android/issues/detail?id=1480
                //So here's the workaround:
                if (is == null) {
                    Uri u = data.getData();
                    file = new File(u.getPath());
                }
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();

                }
                // Send really small photos - everything else causes only trouble - OutOfMemory, slow loading,...
                bigPhotoBitmap = decodeFile(file, 400);
                smallPhotoBitmap = decodeFile(file, 80);
                file.delete();
                takePhotoButton.setImageDrawable(getResources().getDrawable(Tools.getImage(PostEventActivity.this, "removephoto")));
            }

        }
        if (requestCode == PICK_FILE) {
            if (data != null && data.getData() != null) {
                Log.i(TAG_POST_EVENT, "data.getData() = " + data.getData());
                bigPhotoBitmap = decodeFile(new File(data.getData().getPath()), 500);
                smallPhotoBitmap = decodeFile(new File(data.getData().getPath()), 100);
                takePhotoButton.setImageDrawable(getResources().getDrawable(Tools.getImage(PostEventActivity.this, "removephoto")));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Bitmap decodeFile(File file, int max_image_size) {
        if (file == null || file.isDirectory()) {
            return null;
        }
        Bitmap bitmap = null;
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(file), null, o);
            int scale = 1;
            if (o.outHeight > max_image_size || o.outWidth > max_image_size) {
                scale = (int) Math.pow(2, (int) Math.ceil(Math.log(max_image_size / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            bitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, o2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return bitmap;

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

    private File getTempFile() {
        //it will return /sdcard/image.tmp
        return new File(Environment.getExternalStorageDirectory(), "image.tmp");
    }

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile()));

        startActivityForResult(intent, CAMERA_ACTIVITY);

    }
}

