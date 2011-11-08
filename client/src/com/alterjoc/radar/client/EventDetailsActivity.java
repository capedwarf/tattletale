package com.alterjoc.radar.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.alterjoc.radar.client.adapters.CommentAdapter;
import com.alterjoc.radar.client.database.DBAdapter;
import com.alterjoc.radar.client.log.Log;
import com.alterjoc.radar.common.data.CommentInfo;
import com.alterjoc.radar.common.data.EventInfo;
import com.alterjoc.radar.common.data.Image;
import com.alterjoc.radar.common.data.ImageInfo;
import com.alterjoc.radar.common.data.PhotoInfo;
import com.alterjoc.radar.connect.server.ServerProxy;
import org.jboss.capedwarf.common.data.Status;
import org.jboss.capedwarf.common.data.StatusInfo;
import org.jboss.capedwarf.connect.server.ServerProxyFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import static com.alterjoc.radar.common.Constants.TAG_EVENT_DETAILS;
import static com.alterjoc.radar.common.Constants.TAG_POST_COMMENT;

/**
 * User: Dejan
 * Date: 11.9.2010
 * Time: 18:35:02
 */
public class EventDetailsActivity extends Activity {
    private EventInfo event;
    private ListView listView;

    private Handler mHandler = new Handler();
    private boolean photoPresent = false;

    private boolean showErrorToast;
    private boolean firstOpen = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        final DBAdapter helper = Application.getInstance().getDBHelper(this);

        event = loadEvent(helper, getIntent().getLongExtra("eventId", -1));
        event.setDetailsRead(true);
        DBAdapter adapter = Application.getInstance().getDBHelper(this);
        adapter.saveEntity(event);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        ProgressBar smallPhotoProgress = new ProgressBar(this);
        smallPhotoProgress.setIndeterminate(true);

        final ImageView imageView = new ImageView(this);

        imageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (photoPresent) {
                    final ProgressBar bigPhotoProgress = new ProgressBar(EventDetailsActivity.this);
                    bigPhotoProgress.setIndeterminate(true);
                    final LinearLayout wrapper = new LinearLayout(EventDetailsActivity.this);
                    wrapper.setOrientation(LinearLayout.HORIZONTAL);
                    wrapper.setGravity(Gravity.CENTER);

                    final ImageView tmpImageView = new ImageView(EventDetailsActivity.this);
                    Long smallPhotoPk = event.getSmallPhotoPk();
                    if (smallPhotoPk != null && smallPhotoPk >= 0) {
                        ImageInfo image = helper.readImageInfo(smallPhotoPk);
                        byte[] photoBytes = image.readFromDBIntoArray();
                        Drawable eventPhoto = new BitmapDrawable(BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length));
                        tmpImageView.setImageDrawable(eventPhoto);
                        wrapper.addView(tmpImageView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    }
                    wrapper.addView(bigPhotoProgress, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                    AlertDialog.Builder builder = new AlertDialog.Builder(EventDetailsActivity.this);
                    builder.setTitle("Pripeta fotografija");
                    builder.setView(wrapper);
                    builder.show();

                    Thread thread = new Thread(new Runnable() {
                        public void run() {
                            // 1. check if the image is already in the database
                            // 2. save the photo once it is loaded
                            boolean isEventChanged = false;
                            showErrorToast = false;
                            final ImageView bigImageView = new ImageView(EventDetailsActivity.this);
                            // 1. Check if the big photo is already loaded:
                            Long bigPhotoPk = event.getBigPhotoPk();
                            if (bigPhotoPk != null) {
                                // 1.1 If the big photo is only one byte, set no photo image
                                if (bigPhotoPk < 0) {
                                    bigImageView.setImageDrawable(EventDetailsActivity.this.getResources().getDrawable(R.drawable.nophoto));
                                } else {
                                    ImageInfo image = helper.readImageInfo(bigPhotoPk);
                                    byte[] photoBytes = image.readFromDBIntoArray();
                                    Drawable eventPhoto = new BitmapDrawable(BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length));
                                    bigImageView.setImageDrawable(eventPhoto);
                                }
                            } else {
                                // 2. The big photo is not loaded yet, so load it now.
                                try {
                                    PhotoInfo bigPhotoInfo = ServerProxyFactory.create(ServerProxy.class).eventOnDemandPhoto(event.getEventId(), Image.BIG);
                                    byte[] bytes = bigPhotoInfo.getPhoto();
                                    if (bytes != null && bytes.length > 0) {
                                        ImageInfo imageInfo = helper.createImageInfo();
                                        imageInfo.readFromBytesIntoDB(bytes);
                                        bigPhotoPk = helper.saveEntity(imageInfo);
                                    } else {
                                        bigPhotoPk = -1l;
                                    }
                                    event.setBigPhotoPk(bigPhotoPk);
                                    isEventChanged = true;
                                    Drawable eventPhoto = new BitmapDrawable(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                                    bigImageView.setImageDrawable(eventPhoto);
                                } catch (Exception e) {
                                    Log.e(TAG_EVENT_DETAILS, "Failed to display big photo: ", e);
                                    showErrorToast = true;
                                    isEventChanged = false;
                                    bigImageView.setImageResource(R.drawable.nophoto);
                                }
                            }
                            mHandler.post(new Runnable() {
                                public void run() {
                                    if (showErrorToast) {
                                        Toast toast = Toast.makeText(EventDetailsActivity.this, "Napaka pri branju slike.", Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                    wrapper.removeView(bigPhotoProgress);
                                    wrapper.removeView(tmpImageView);
                                    wrapper.addView(bigImageView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                    showErrorToast = false;
                                }
                            });
                            if (isEventChanged) {
                                // 5. Store loaded photo on the event to the database
                                helper.updateEntity(event);
                            }
                        }
                    }, "EventDetails Fetch Big Image Thread");
                    thread.start();
                }
            }
        });
        final LinearLayout imageWrapper = new LinearLayout(this);
        imageWrapper.setPadding(1, 1, 1, 1);
        imageWrapper.setGravity(Gravity.CENTER);

        LinearLayout lineWrapper = new LinearLayout(this);
        lineWrapper.setOrientation(LinearLayout.HORIZONTAL);
        TextView eventTextView = new TextView(this);
        eventTextView.setGravity(Gravity.LEFT);
        eventTextView.setPadding(5, 2, 2, 2);
        eventTextView.setTextSize(Tools.getButtonsFontSize(this));
        eventTextView.setSingleLine(false);
        setEventText(eventTextView, event);

        imageWrapper.addView(smallPhotoProgress, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        imageWrapper.setBackgroundColor(Color.BLACK);

        LinearLayout borderWrapper = new LinearLayout(this);
        borderWrapper.setPadding(1, 1, 1, 1);
        borderWrapper.setBackgroundDrawable(getResources().getDrawable(R.drawable.border));
        borderWrapper.addView(imageWrapper, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));

        LinearLayout borderWrapper2 = new LinearLayout(this);
        borderWrapper2.setPadding(1, 1, 1, 1);
        borderWrapper2.setBackgroundDrawable(getResources().getDrawable(R.drawable.border2));
        borderWrapper2.addView(borderWrapper, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));

        lineWrapper.addView(borderWrapper2, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT, 2));
        lineWrapper.addView(eventTextView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        linearLayout.addView(lineWrapper, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        Button addCommentButton = new Button(this);

        StateListDrawable drawables = new StateListDrawable();
        drawables.addState(new int[]{android.R.attr.state_pressed}, getResources().getDrawable(R.drawable.comment_add_pressed48));
        drawables.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.comment_add48));

        addCommentButton.setBackgroundDrawable(drawables);
        addCommentButton.setPadding(2, 5, 2, 2);
        addCommentButton.setGravity(Gravity.RIGHT);
        addCommentButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!Tools.isUserLoggedIn(EventDetailsActivity.this, true)) {
                    return;
                }
                final EditText input = new EditText(EventDetailsActivity.this);
                input.setSingleLine(false);
                input.setLines(4);
                input.setGravity(Gravity.TOP);
                AlertDialog.Builder builder = new AlertDialog.Builder(EventDetailsActivity.this);
                builder.setView(input);
                builder.setTitle(R.string.comment);
                builder.setMessage(R.string.enter_text);
                builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast toast = Toast.makeText(EventDetailsActivity.this, R.string.text_in_background, Toast.LENGTH_LONG);
                        toast.show();
                        dialog.dismiss();
                        Thread postCommentThread = new Thread(new Runnable() {
                            public void run() {
                                String comment = input.getText().toString().trim();
                                CommentInfo commentInfo = new CommentInfo(comment, event.getTopicId(), event.getId());
                                Preferences prefs = Application.getInstance().getPreferences(EventDetailsActivity.this);
                                commentInfo.setUsername(prefs.getUserLogin());
                                if (sendComment(commentInfo)) {
                                    mHandler.post(new Runnable() {
                                        public void run() {
                                            // Ob uspešnem zaključku akcije zapri dialog
                                            Toast toast = Toast.makeText(EventDetailsActivity.this, R.string.comment_success, Toast.LENGTH_LONG);
                                            toast.show();
                                            DBAdapter dbh = Application.getInstance().getDBHelper(EventDetailsActivity.this);
                                            event = loadEvent(dbh, event.getId());
                                            List<CommentInfo> comments = event.getUserComments();
                                            Tools.reorderCommentsByTimestamp(comments);
                                            CommentAdapter commentAdapter = new CommentAdapter(EventDetailsActivity.this, android.R.layout.simple_list_item_1, comments, event.getUsername());
                                            listView.setAdapter(commentAdapter);
                                            listView.invalidate();
                                        }
                                    });
                                }
                            }
                        }, "EventDetails Post Comment Thread");
                        postCommentThread.start();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        Button deleteButton = new Button(this);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EventDetailsActivity.this);
                builder.setTitle(R.string.delete_message);
                builder.setMessage(R.string.confirm_delete);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        DBAdapter adapter = Application.getInstance().getDBHelper(EventDetailsActivity.this);
                        List<EventInfo> list = new ArrayList<EventInfo>();
                        list.add(event);
                        adapter.deleteEntities(list);
                        dialog.dismiss();
                        EventDetailsActivity.this.finish();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        drawables = new StateListDrawable();

        drawables.addState(new int[]{android.R.attr.state_pressed}, getResources().getDrawable(R.drawable.delete_button_pressed48));
        drawables.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.delete_button_normal48));

        deleteButton.setBackgroundDrawable(drawables);
        deleteButton.setPadding(2, 5, 2, 2);
        deleteButton.setGravity(Gravity.LEFT);

        final Button mapButton = new Button(this);
        mapButton.setEnabled(!event.isArchived());
        final Button archiveButton = new Button(this);
        archiveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                DBAdapter adapter = Application.getInstance().getDBHelper(EventDetailsActivity.this);
                event.setArchived(!event.isArchived());
                adapter.saveEntity(event);
                Toast toast = (event.isArchived()) ? Toast.makeText(EventDetailsActivity.this, R.string.event_archived, Toast.LENGTH_SHORT) :
                        Toast.makeText(EventDetailsActivity.this, R.string.event_unarchived, Toast.LENGTH_SHORT);
                toast.show();
                StateListDrawable drawables = new StateListDrawable();
                if (event.isArchived()){
                    drawables.addState(new int[]{android.R.attr.state_pressed}, getResources().getDrawable(R.drawable.unarchive_button_pressed48));
                    drawables.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.unarchive_button_normal48));
                } else {
                    drawables.addState(new int[]{android.R.attr.state_pressed}, getResources().getDrawable(R.drawable.archive_button_pressed48));
                    drawables.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.archive_button_normal48));
                }
                archiveButton.setBackgroundDrawable(drawables);
                mapButton.setEnabled(!event.isArchived());
            }
        });

        drawables = new StateListDrawable();

        // Order of added states is important!
        if (event.isArchived()){
            drawables.addState(new int[]{android.R.attr.state_pressed}, getResources().getDrawable(R.drawable.unarchive_button_pressed48));
            drawables.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.unarchive_button_normal48));
        } else {
            drawables.addState(new int[]{android.R.attr.state_pressed}, getResources().getDrawable(R.drawable.archive_button_pressed48));
            drawables.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.archive_button_normal48));
        }

        archiveButton.setBackgroundDrawable(drawables);
        archiveButton.setPadding(2, 5, 2, 2);
        archiveButton.setGravity(Gravity.LEFT);


        mapButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                SharedPreferences prefs = Tools.getAppPreferences(EventDetailsActivity.this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong("eventId", event.getId());
                editor.commit();
                Intent intent = new Intent(EventDetailsActivity.this, EventsMapActivity.class);
                EventDetailsActivity.this.startActivity(intent);
            }
        });

        drawables = new StateListDrawable();

        // Order of added states is important!
        drawables.addState(new int[]{android.R.attr.state_pressed}, getResources().getDrawable(R.drawable.show_on_map_button_pressed48));
        drawables.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.show_on_map_button_normal48));

        mapButton.setBackgroundDrawable(drawables);
        mapButton.setPadding(2, 5, 2, 2);
        mapButton.setGravity(Gravity.LEFT);



        Button thumbsUpButton = new Button(this);
        drawables = new StateListDrawable();

        drawables.addState(new int[]{android.R.attr.state_pressed}, getResources().getDrawable(R.drawable.thumbs_down48));
        drawables.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.thumbs_down48));

        thumbsUpButton.setBackgroundDrawable(drawables);
        thumbsUpButton.setPadding(2, 5, 2, 2);
        thumbsUpButton.setGravity(Gravity.LEFT);

        Button facebookButton = new Button(this);
        drawables = new StateListDrawable();

        drawables.addState(new int[]{android.R.attr.state_pressed}, getResources().getDrawable(R.drawable.facebook48));
        drawables.addState(new int[]{android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.facebook48));

        facebookButton.setBackgroundDrawable(drawables);
        facebookButton.setPadding(2, 5, 2, 2);
        facebookButton.setGravity(Gravity.LEFT);

        LinearLayout buttonsWrapper = new LinearLayout(this);
        buttonsWrapper.setOrientation(LinearLayout.HORIZONTAL);

        // TODO :: this should also be added when the event is archived (the problem is in setting the disabled gif)
        if (!event.isArchived()){
            buttonsWrapper.addView(mapButton, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }
        buttonsWrapper.addView(archiveButton, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        buttonsWrapper.addView(deleteButton, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        buttonsWrapper.addView(addCommentButton, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        buttonsWrapper.addView(thumbsUpButton, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        buttonsWrapper.addView(facebookButton, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        linearLayout.addView(buttonsWrapper, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        listView = new ListView(this);
        listView.setPadding(2, 5, 2, 2);

        linearLayout.addView(listView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));

        linearLayout.setPadding(2, 5, 2, 2);
        setContentView(linearLayout);
        setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.tozibaba16);

        final DBAdapter dbAdapter = Application.getInstance().getDBHelper(EventDetailsActivity.this);

        // Fetch the image from server in a separate thread
        Thread thread = new Thread(new Runnable() {
            public void run() {
                // 1. check if the image is already in the database
                // 2. save the photo once it is loaded
                PhotoInfo smallPhotoInfo = null;
                boolean isEventChanged = false;
                System.out.println("EventDetailsActivity.run");
                // 1. Check if the small photo is already loaded:
                Long smallPhotoPk = event.getSmallPhotoPk();
                if (smallPhotoPk != null) {
                    // 1.1 If the small photo is only one byte, set no photo image
                    if (smallPhotoPk < 0) {
                        mHandler.post(new Runnable() {
                            public void run() {
                                imageView.setImageResource(R.drawable.nophoto);
                            }
                        });
                    } else {
                        smallPhotoInfo = new PhotoInfo();
                        ImageInfo image = dbAdapter.readImageInfo(smallPhotoPk);
                        smallPhotoInfo.setPhoto(image.readFromDBIntoArray());
                    }
                } else {
                    // 2. If the photo is not loaded, fetch it from the server:
                    try {
                        PhotoInfo photoInfo = ServerProxyFactory.create(ServerProxy.class).eventOnDemandPhoto(event.getEventId(), Image.SMALL);
                        byte[] bytes = photoInfo.getPhoto();
                        if (bytes != null && bytes.length > 0) {
                            ImageInfo image = dbAdapter.createImageInfo();
                            image.readFromBytesIntoDB(bytes);
                            smallPhotoPk = dbAdapter.saveEntity(image);
                        } else {
                            smallPhotoPk = -1l;
                        }
                        event.setSmallPhotoPk(smallPhotoPk);
                        isEventChanged = true;
                        smallPhotoInfo = photoInfo;
                    } catch (Exception e) {
                        isEventChanged = false;
                        mHandler.post(new Runnable() {
                            public void run() {
                                Toast toast = Toast.makeText(EventDetailsActivity.this, R.string.error_reading_picture, Toast.LENGTH_SHORT);
                                toast.show();
                                imageView.setImageResource(R.drawable.nophoto);
                                imageView.setImageDrawable(EventDetailsActivity.this.getResources().getDrawable(R.drawable.nophoto));
                                imageView.setAdjustViewBounds(true);
                                imageWrapper.removeAllViews();
                                imageWrapper.addView(imageView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
                            }
                        });
                    }
                }

                // 3. If there is any photo, set it on the view and store it to the local database:
                if (smallPhotoPk != null && smallPhotoPk >= 0) {
                    event.setSmallPhoto(smallPhotoInfo.getPhoto());
                    photoPresent = true;
                    byte photoBytes[] = smallPhotoInfo.getPhoto();
                    final Drawable eventPhoto = new BitmapDrawable(BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length));
                    mHandler.post(new Runnable() {
                        public void run() {
                            imageView.setImageDrawable(eventPhoto);
                            imageView.setAdjustViewBounds(true);
                            imageWrapper.removeAllViews();
                            imageWrapper.addView(imageView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
                        }
                    });
                } else {
                    mHandler.post(new Runnable() {
                        public void run() {
                            imageView.setImageDrawable(EventDetailsActivity.this.getResources().getDrawable(R.drawable.nophoto));
                            imageView.setAdjustViewBounds(true);
                            imageWrapper.removeAllViews();
                            imageWrapper.addView(imageView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
                        }
                    });
                }
                if (isEventChanged) {
                    // 5. Store loaded photo on the event to the database
                    dbAdapter.updateEntity(event);
                }
            }
        }, "EventDetails Fetch Small Image Thread");
        thread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Application.getInstance().setEventActivity(null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Application.getInstance().setEventActivity(this);
        // perform comment sync
        if (firstOpen) {
            firstOpen = false;
            TozibabaService.getInstance(this).syncCommentsNow();
        }

        // reload event so that we have the latest comments
        DBAdapter helper = Application.getInstance().getDBHelper(this);
        event = loadEvent(helper, event.getId());
        List<CommentInfo> comments = event.getUserComments();
        Tools.reorderCommentsByTimestamp(comments);
        CommentAdapter adapter = new CommentAdapter(this, android.R.layout.simple_list_item_1, comments, event.getUsername());
        listView.setAdapter(adapter);
    }

    private void setEventText(TextView textView, EventInfo event) {
        StringBuilder eventText = new StringBuilder();
        int bookmarks[] = new int[6];
        addSpan(0, bookmarks, event.getTopicInfo().getName(), eventText, true);
        addSpan(1, bookmarks, event.getTitle(), eventText, true);
        addSpan(2, bookmarks, event.getComment(), eventText, true);
        addSpan(3, bookmarks, event.getUsername() + ", ", eventText, false);
        addSpan(4, bookmarks, event.getAddress() + ", ", eventText, false);
        addSpan(5, bookmarks, Tools.sdf.format(new Date(event.getTimestamp())), eventText, false);
        textView.setText(new String(eventText), TextView.BufferType.SPANNABLE);
        Spannable spannable = (Spannable) textView.getText();
        setStyle(spannable, bookmarks);
    }

    private void setStyle(Spannable spannable, int[] bookmarks) {
        spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, bookmarks[0], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(Color.GREEN), 0, bookmarks[0], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(Color.GREEN), bookmarks[0], bookmarks[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(Color.WHITE), bookmarks[1], bookmarks[2], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(Color.YELLOW), bookmarks[2], bookmarks[3], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), bookmarks[2], bookmarks[3], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(Color.LTGRAY), bookmarks[3], bookmarks[5], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(Color.GRAY), bookmarks[4], bookmarks[5], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), bookmarks[4], bookmarks[5], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void addSpan(int position, int breakpoints[], String text, StringBuilder builder, boolean newLine) {
        if (text != null) {
            builder.append(text);
            int size = text.length();
            if (newLine) {
                builder.append("\n");
                size++;
            }
            if (position > 0) {
                size += breakpoints[position - 1];
            }
            breakpoints[position] = size;
        }
    }

    private boolean sendComment(CommentInfo comment) {
        StatusInfo result;
        try {
            result = TozibabaService.getInstance(this).postComment(comment);
            if (result.getStatus() != Status.OK) {
                throw new RuntimeException("Status: " + result.getStatus());
            }
        } catch (Throwable e) {
            Log.e(TAG_POST_COMMENT, "Failed to post new comment to server: " + comment, e);
            // Ob neuspešnem zaključku akcije zapri dialog
            mHandler.post(new Runnable() {
                public void run() {
                    Toast toast = Toast.makeText(EventDetailsActivity.this, R.string.error_comment_not_sent, Toast.LENGTH_LONG);
                    toast.show();
                }
            });
            return false;
        }
        // On success
        DBAdapter helper = Application.getInstance().getDBHelper(EventDetailsActivity.this);
        comment.setId(result.getId());
        comment.setTimestamp(result.getTimestamp());
        helper.saveEntity(comment);

        // lazy handle comments
        List<CommentInfo> comments = event.getUserComments();
        if (comments == null || comments.isEmpty()) {
            comments = Collections.singletonList(comment);
            event.setUserComments(comments);
        } else if (comments.size() == 1) {
            comments = new ArrayList<CommentInfo>(comments);
            comments.add(comment);
            event.setUserComments(comments);
        } else {
            comments.add(comment);
        }
        Tools.reorderCommentsByTimestamp(comments);

        // Trigger downSync to get the new comment down ASAP
        // Actually no need for that as new comment has been added locally
        //TozibabaService.getInstance(EventDetailsActivity.this).syncNow();

        return true;
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

    private EventInfo loadEvent(DBAdapter helper, long id) {
        EventInfo eventInfo = helper.readEventInfo(id);
        if (eventInfo != null){
            List<CommentInfo> comments = helper.readComments(id);
            eventInfo.setUserComments(comments);
        }
        return eventInfo;
    }

    @SuppressWarnings({"unchecked"})
    void addComments(final List<CommentInfo> comments) {
        // we're only interested in comments that refer to our event
        final LinkedList<CommentInfo> filtered = new LinkedList<CommentInfo>();
        for (CommentInfo comment : comments) {
            if (event.getId().equals(comment.getEventId()))
                filtered.add(comment);
        }
        runOnUiThread(new Runnable() {
            public void run() {
                ArrayAdapter<CommentInfo> adapter = (ArrayAdapter<CommentInfo>) listView.getAdapter();

                // load current items into map - we have to check by id to prevent duplicates
                LinkedHashMap<Long, CommentInfo> commentMap = new LinkedHashMap<Long, CommentInfo>();
                int count = adapter.getCount();
                for (int i = 0; i < count; i++) {
                    CommentInfo comment = adapter.getItem(i);
                    commentMap.put(comment.getId(), comment);
                }

                for (CommentInfo ci : filtered) {
                    commentMap.put(ci.getId(), ci);
                }

                List<CommentInfo> comments = new LinkedList<CommentInfo>(commentMap.values());
                Tools.reorderCommentsByTimestamp(comments);
                CommentAdapter commentAdapter = new CommentAdapter(EventDetailsActivity.this, android.R.layout.simple_list_item_1, comments, event.getUsername());
                listView.setAdapter(commentAdapter);
                listView.invalidate();
            }
        });
    }
}
