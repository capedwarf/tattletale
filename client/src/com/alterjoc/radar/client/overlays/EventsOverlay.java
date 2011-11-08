package com.alterjoc.radar.client.overlays;

import android.content.Intent;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.TextView;
import com.alterjoc.radar.client.Application;
import com.alterjoc.radar.client.EventDetailsActivity;
import com.alterjoc.radar.client.EventsMapActivity;
import com.alterjoc.radar.client.R;
import com.alterjoc.radar.client.Tools;
import com.alterjoc.radar.client.database.DBAdapter;
import com.alterjoc.radar.client.graphics.CommentCountDrawable;
import com.alterjoc.radar.common.data.EventInfo;
import com.alterjoc.radar.common.data.ImageInfo;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.alterjoc.radar.client.EventsMapActivity.trimString;

/**
 * Displays all events for all categories to which the user is subscribed.
 * <p/>
 * User: Dejan
 * Date: 17.8.2010
 * Time: 9:30:40
 */
public class EventsOverlay extends ItemizedOverlay {
    private LinkedHashMap<Long, EventOverlayItem> mOverlayItems = new LinkedHashMap<Long, EventOverlayItem>();
    private EventsMapActivity mContext;
    private MapView parent;
    private AnimationThread singleAnimationThread;
    // The layout that contains the final bubble button with text
    private AbsoluteLayout wrapperLayout;
    private Drawable selectedMarker;
    private Drawable[] selectedMarkers;
    private int[] drawableIds = new int[]{
            R.drawable.event_bubble_0,
            R.drawable.event_bubble_1,
            R.drawable.event_bubble_2,
            R.drawable.event_bubble_3,
            R.drawable.event_bubble_4,
            R.drawable.event_bubble_5,
            R.drawable.event_bubble_6,
            R.drawable.event_bubble_7,
            R.drawable.event_bubble_8,
            R.drawable.event_bubble_9,
    };

    private Map<ImageInfo, FadeOutDrawable> imageCache = new HashMap<ImageInfo, FadeOutDrawable>();

    public EventsOverlay(EventsMapActivity context, MapView parent, Drawable selectedMarker) {
        super(selectedMarker);
        mContext = context;
        this.parent = parent;
        wrapperLayout = new AbsoluteLayout(mContext);
        this.parent.addView(wrapperLayout, new AbsoluteLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT, 0, 0));
        this.selectedMarker = boundCenterBottom(selectedMarker);

        selectedMarkers = new Drawable[10];

        for (int i = 0; i < drawableIds.length; i++) {
            prepareDrawable(i, drawableIds[i]);
        }
    }

    private void prepareDrawable(int index, int drawableId) {
        Drawable drawable = mContext.getResources().getDrawable(drawableId);
        drawable.setBounds(0, 0, selectedMarker.getIntrinsicWidth(), selectedMarker.getIntrinsicHeight());
        selectedMarkers[index] = boundCenterBottom(drawable);
    }

    public void addOverlayItems(List<EventInfo> events) {
        boolean changed = false;
        for (EventInfo event: events)
        {
           if (event.isArchived())
           {
              boolean ch = mOverlayItems.remove(event.getId()) != null;
              changed = changed ? changed : ch;
           }
           else
           {
              EventOverlayItem eoi = mOverlayItems.get(event.getId());
              if (eoi != null)
              {
                 eoi.setEvent(event);
              }
              else
              {
                 eoi = createEventOverlayItem(event);              
                 Drawable drawable = boundCenterBottom(eoi.getDefaultMarker());
                 eoi.setMarker(drawable);
                 mOverlayItems.put(event.getId(), eoi);
              }
              changed = true;
           }
        }

        populate();
    }

    private EventOverlayItem createEventOverlayItem(EventInfo event) {
       byte[] imageBytes;
       GeoPoint point = new GeoPoint(event.getLatitude(), event.getLongitude());
       EventOverlayItem overlayItem = new EventOverlayItem(
               point,
               trimString(event.getTopicInfo().getName()),
               event.getTopicInfo().getName() + "\n" +
               trimString(event.getTitle()) + "\n" +
               trimString(event.getAddress()) + "\n" +
               trimString(event.getUsername() + "," + Tools.sdf.format(new Date(event.getTimestamp()))), event);


       FadeOutDrawable marker = null;
       ImageInfo imageInfo = event.getTopicInfo().getImage();
       if (imageInfo != null) {
           imageBytes = imageInfo.readFromDBIntoArray();
           if (imageBytes != null && imageBytes.length > 0) {
               if (!imageCache.containsKey(event.getTopicInfo().getImage())) {
                   marker = new FadeOutDrawable(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length), event);
                   imageCache.put(event.getTopicInfo().getImage(), marker);
               } else {
                   marker = imageCache.get(event.getTopicInfo().getImage());
               }
           } else {
               marker = new FadeOutDrawable(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.userdefined), event);
           }
       } else {
           marker = new FadeOutDrawable(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.userdefined), event);
       }
       marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
       overlayItem.setDefaultMarker(marker);
       return overlayItem;
   }

   protected EventOverlayItem getItemByPos(int pos) {
      int i = 0;
      for (EventOverlayItem item: mOverlayItems.values())
      {
          if (i == pos)
             return item;
          i++;  
      }
      throw new ArrayIndexOutOfBoundsException(pos);      
   } 
   
    @Override
    protected OverlayItem createItem(int pos) {
       return getItemByPos(pos);
    }

    @Override
    public int size() {
        return mOverlayItems.size();

    }

    public void resetViews() {
        if (wrapperLayout.getVisibility() == View.VISIBLE) {
            wrapperLayout.setVisibility(View.INVISIBLE);
            for (EventOverlayItem overlayItem : mOverlayItems.values()) {
                overlayItem.setMarker(overlayItem.getDefaultMarker());
            }
        }
    }

    public void simulateTap(long eventInfoId){
        int i = 0;
        for (EventOverlayItem mOverlayItem : mOverlayItems.values()) {
            if (mOverlayItem.getEvent().getId().equals(eventInfoId)){
                onTap(i);
                break;
            }
            i++;
        }
    }

    /**
     * On click set center of the map, display the textbox and highlight the element
     */
    @Override
    protected boolean onTap(int index) {
        final EventOverlayItem item = getItemByPos(index);
        if (wrapperLayout.getVisibility() == View.VISIBLE) {
            resetViews();
            wrapperLayout.removeAllViews();
        }
        GeoPoint geoPoint = item.getPoint();
        final Point point = new Point();
        parent.getProjection().toPixels(geoPoint, point);
        final GeoPoint geoPointCentered = parent.getProjection().fromPixels(point.x, point.y);

        final Handler handler = new Handler() {
            private int counter;

            @Override
            public void handleMessage(Message msg) {
                // premature end of animation, go back to default:
                if (msg.what == 11){
                    item.setMarker(item.getDefaultMarker());
                    resetViews();
                    return;
                }
                // animated bubbles:
                if (msg.what < selectedMarkers.length) {
                    counter++;
                    item.setMarker(selectedMarkers[msg.what]);
                } else {
                    // last step (if it was not reached before) and bubble button:
                    if (counter < selectedMarkers.length - 1) {
                        item.setMarker(selectedMarkers[selectedMarkers.length - 1]);
                    }
                    Point point = new Point();
                    parent.getProjection().toPixels(geoPointCentered, point);
                    wrapperLayout.addView(createCloudButton(item.getSnippet(), ((EventOverlayItem)item).getEvent()), new AbsoluteLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                            point.x - 100, point.y - 100));
                    wrapperLayout.setVisibility(View.VISIBLE);
                }
            }
        };
        if (singleAnimationThread != null){
            singleAnimationThread.stopThread();
            while (!singleAnimationThread.isThreadStopped()){
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        singleAnimationThread = new AnimationThread(handler);
        singleAnimationThread.start();

        parent.getController().animateTo(geoPointCentered, new Runnable() {
            public void run() {
                Message message = Message.obtain();
                message.what = selectedMarkers.length;
                handler.sendMessage(message);
            }
        });
        return false;
    }

    public boolean onTouchEvent(MotionEvent event, MapView mapView) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            resetViews();
        }
        return false;
    }


    private Button createCloudButton(String text, final EventInfo event) {
        Button textButton = new Button(mContext);
        textButton.setWidth(200);
        textButton.setHeight(100);
        textButton.setGravity(Gravity.CENTER_HORIZONTAL);
        textButton.setText(text, TextView.BufferType.SPANNABLE);
        createSpanSections((Spannable)textButton.getText(), false, textButton.getText().toString());
        StateListDrawable drawables = new StateListDrawable();
        drawables.addState(new int[]{android.R.attr.state_pressed}, mContext.getResources().getDrawable(R.drawable.event_bubble_pressed));
        drawables.addState(new int[]{android.R.attr.state_enabled}, mContext.getResources().getDrawable(R.drawable.event_bubble_));

        textButton.setBackgroundDrawable(drawables);

        DBAdapter db = Application.getInstance().getDBHelper(mContext);
        CommentCountDrawable squareBackground = new CommentCountDrawable(
                BitmapFactory.decodeResource(mContext.getResources(), R.drawable.square_background),
                event.isDetailsRead(), db.countComments(event.getId()));
        squareBackground.setBounds(0, 0, squareBackground.getIntrinsicWidth(), squareBackground.getIntrinsicHeight());
        textButton.setCompoundDrawables(squareBackground, null, null, null);
        textButton.setSingleLine(false);

        textButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(mContext, EventDetailsActivity.class);
                intent.putExtra("eventId", event.getId());
                mContext.startActivity(intent);
                resetViews();
            }
        });
        return textButton;
    }

    private class AnimationThread extends Thread {

        private Handler handler;
        private int count = 0;
        private boolean stopThread = false;
        private boolean isThreadStopped = false;

        public AnimationThread(Handler handler) {
            this.handler = handler;
        }

        public void run() {
            while (count < 10 && !stopThread) {
                Message message = Message.obtain();
                message.what = count++;
                handler.sendMessage(message);
                try {
                    Thread.currentThread().sleep(75);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (stopThread){
                Message message = Message.obtain();
                message.what = 11;
                handler.sendMessage(message);
            }
            isThreadStopped = true;
        }

        public void stopThread(){
            stopThread = true;
        }

        public boolean isThreadStopped(){
            return isThreadStopped;
        }
    }

    private void createSpanSections(Spannable spannable, boolean isRead, String text) {
        String[] lines = text.split("\n");
        if (!isRead) {
            switch (lines.length){
                case 4:
                    spannable.setSpan(new StyleSpan(android.graphics.Typeface.NORMAL), lines[0].length() + lines[1].length()+ lines[2].length() + 2, lines[0].length() + lines[1].length() + lines[2].length() + lines[3].length() + 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannable.setSpan(new ForegroundColorSpan(Color.GRAY), lines[0].length() + lines[1].length() + lines[2].length() + 2, lines[0].length() + lines[1].length() + lines[2].length() + lines[3].length() + 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                case 3:
                    spannable.setSpan(new StyleSpan(android.graphics.Typeface.NORMAL), lines[0].length() + lines[1].length() + 2, lines[0].length() + lines[1].length() + lines[2].length() + 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannable.setSpan(new ForegroundColorSpan(Color.BLACK), lines[0].length() + lines[1].length() + 2, lines[0].length() + lines[1].length() + lines[2].length() + 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                case 2:
                    spannable.setSpan(new StyleSpan(android.graphics.Typeface.NORMAL), lines[0].length() + 1, lines[0].length() + 1 + lines[1].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannable.setSpan(new ForegroundColorSpan(Color.BLACK), lines[0].length() + 1, lines[0].length() + 1 + lines[1].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                case 1:
                    spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, lines[0].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannable.setSpan(new ForegroundColorSpan(Color.BLUE), 0, lines[0].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

        } else {
            spannable.setSpan(new ForegroundColorSpan(Color.LTGRAY), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private class FadeOutDrawable extends BitmapDrawable {

        private EventInfo event;

        public FadeOutDrawable(Bitmap bitmap, EventInfo event) {
            super(bitmap);
            this.event = event;
        }


        @Override
        public void draw(Canvas canvas) {
            long expirationDelta = event.getExpirationDelta();
            long startTime = event.getTimestamp();
            long currentTime = System.currentTimeMillis();

            long age = currentTime - startTime;

            int alpha = 0;
            if (age >= expirationDelta) {
                // Max transparency
                alpha = 100;
            } else if (age < expirationDelta * 0.75) {
                alpha = 255;
            } else {
                // transparency progresses to 55 with age
                alpha = (int) (255 - (age / expirationDelta) * 155);
            }

            // If the event is returned from archive to list, then dont apply alpha on it
            if (!event.isUnarchived()){
                setAlpha(alpha);
            }
            super.draw(canvas);
        }
    }


}
