package com.alterjoc.radar.client.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.alterjoc.radar.client.Application;
import com.alterjoc.radar.client.EventsMapActivity;
import com.alterjoc.radar.client.R;
import com.alterjoc.radar.client.Tools;
import com.alterjoc.radar.client.database.DBAdapter;
import com.alterjoc.radar.client.graphics.CommentCountDrawable;
import com.alterjoc.radar.client.graphics.FilteredDrawable;
import com.alterjoc.radar.common.data.EventInfo;
import com.alterjoc.radar.common.data.ImageInfo;

import java.text.DecimalFormat;
import java.util.*;

/**
 * User: Dejan
 * Date: 6.9.2010
 * Time: 20:47:54
 */
public class EventAdapter extends ArrayAdapter {

    private Map<ImageInfo, Drawable> drawables;
    private DecimalFormat format = new DecimalFormat("#0.00 km");
    private List<Integer> checkedItems;
    private List objects;
    private Intent onTextClickIntent;
    private boolean buttonsEnabled;


    public EventAdapter(Context context, int textViewResourceId, List objects) {
        super(context, textViewResourceId, objects);
        drawables = new HashMap<ImageInfo, Drawable>();
        checkedItems = new ArrayList<Integer>();
        this.objects = objects;
        buttonsEnabled = true;
    }

    public List<EventInfo> getCheckedEvents() {
        List<Integer> checked = getCheckedItems();
        List<EventInfo> checkedEvents = new ArrayList<EventInfo>();
        for (Integer i : checked) {
            checkedEvents.add((EventInfo) getItem(i));

        }
        return checkedEvents;
    }

    public void checkAll(boolean check) {
        if (check) {
            for (int i = 0; i < objects.size(); i++) {
                checkedItems.add(i);
            }
        } else {
            checkedItems = new ArrayList<Integer>();
        }
    }

    public void setOnTextClickIntent(Intent intent) {
        onTextClickIntent = intent;
    }

    public List<Integer> getCheckedItems() {
        return checkedItems;
    }

    @Override
    public void remove(Object object) {
        int index = objects.indexOf(object);
        if (index > -1 && index < checkedItems.size()) {
            checkedItems.remove((Integer) index);
        }
        super.remove(object);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        EventInfo event = (EventInfo) getItem(position);
        Drawable image;

        // Make new view only when there is nothing to reuse:
        if (convertView == null) {
            convertView = new CompoundView(getContext());
        }

        // Get the appropriate image
        ImageInfo imageInfo = event.getTopicInfo().getImage();
        if (imageInfo != null) {
            if (!drawables.containsKey(imageInfo)) {
                byte imageBytes[] = imageInfo.readFromDBIntoArray();
                if (imageBytes != null && imageBytes.length > 0){
                    image = new BitmapDrawable(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
                    drawables.put(event.getTopicInfo().getImage(), image);
                } else {
                    image = getContext().getResources().getDrawable(R.drawable.userdefined);
                }
            } else {
                image = drawables.get(event.getTopicInfo().getImage());
            }
        } else {
            image = getContext().getResources().getDrawable(R.drawable.userdefined);
        }


        int[] styled = new int[10];
        String text = createText(event, styled);

        ((CompoundView) convertView).position = position;
        ((CompoundView) convertView).eventId = event.getId();
        TextView tv = ((CompoundView) convertView).textView;
        DBAdapter db = Application.getInstance().getDBHelper(getContext());

        CommentCountDrawable commentCount = new CommentCountDrawable(
                BitmapFactory.decodeResource(getContext().getResources(), R.drawable.square_background),
                event.isDetailsRead(), db.countComments(event.getId()));
        commentCount.setBounds(0, 0, commentCount.getIntrinsicWidth(), commentCount.getIntrinsicHeight());
        tv.setCompoundDrawables(commentCount, null, null, null);
        tv.setText(text, TextView.BufferType.SPANNABLE);
        createSpanSections((Spannable) tv.getText(), event.isArchived(), styled, text.length());
        ((CompoundView) convertView).checkBox.setChecked(checkedItems.contains(position));
        StateListDrawable drawables = new StateListDrawable();
        drawables.addState(new int[]{android.R.attr.state_pressed}, new FilteredDrawable((BitmapDrawable)image));
        drawables.addState(new int[]{android.R.attr.state_enabled}, image);
        ((CompoundView) convertView).imageButton.setImageDrawable(drawables);
        ((CompoundView) convertView).imageButton.setBackgroundDrawable(null);

        return convertView;
    }

    private String createText(EventInfo event, int[] styled) {
        int d = (event.getDistance() == null) ? 0 : event.getDistance();
        String distance = (d == 0) ? "-.-" : format.format(event.getDistance() / 1000);
        String title = event.getTitle();
        String user = null;
        if (event.getUsername().length() > 8) {
            user = event.getUsername().substring(0, 8) + "...";
        } else {
            user = event.getUsername();
        }

        String time = Tools.sdf.format(new Date(event.getTimestamp()));
        String address = event.getAddress();

        StringBuffer buffer = new StringBuffer();

        int counter = 0;
        appendStyled(title, counter++, styled, buffer);
        counter++;
        buffer.append("\n");
        appendStyled(user, counter++, styled, buffer);
        counter++;
        buffer.append(", ");
        appendStyled(distance, counter++, styled, buffer);
        counter++;
        buffer.append(", ");
        appendStyled(time, counter++, styled, buffer);
        counter++;
        buffer.append("\n");
        appendStyled(address, counter, styled, buffer);
        return new String(buffer);
    }

    private void appendStyled(String text, int counter, int[] styled, StringBuffer buffer) {
        styled[counter++] = buffer.length();
        buffer.append(text);
        styled[counter] = buffer.length();

    }

    private void createSpanSections(Spannable str, boolean isArchived, int[] styled, int textLength) {
        if (!isArchived) {
            str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, styled[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            str.setSpan(new ForegroundColorSpan(Color.YELLOW), styled[2], styled[3], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), styled[2], styled[3], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            str.setSpan(new ForegroundColorSpan(Color.GREEN), styled[4], styled[5], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), styled[4], styled[5], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            str.setSpan(new ForegroundColorSpan(Color.LTGRAY), styled[6], styled[7], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), styled[6], styled[7], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, styled[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            str.setSpan(new ForegroundColorSpan(Color.YELLOW), styled[2], styled[3], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), styled[2], styled[3], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            str.setSpan(new ForegroundColorSpan(Color.LTGRAY), styled[4], styled[5], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), styled[4], styled[5], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            str.setSpan(new ForegroundColorSpan(Color.LTGRAY), styled[6], styled[7], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), styled[6], styled[7], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public void setButtonsEnabled(boolean enabled) {
        buttonsEnabled = enabled;
    }

    private class CompoundView extends LinearLayout {
        public TextView textView;
        public CheckBox checkBox;
        public ImageButton imageButton;
        public int position;
        public long eventId;

        public CompoundView(Context context) {
            super(context);
            setOrientation(LinearLayout.HORIZONTAL);
            setGravity(Gravity.RIGHT);
            imageButton = new ImageButton(getContext());
            imageButton.setEnabled(buttonsEnabled);
            imageButton.setOnClickListener(new OnClickListener(){
                public void onClick(View view) {
                    SharedPreferences prefs = Tools.getAppPreferences(getContext());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong("eventId", eventId);
                    editor.commit();
                    Intent intent = new Intent(getContext(), EventsMapActivity.class);
                    getContext().startActivity(intent);
                }
            });
            StateListDrawable drawable = new StateListDrawable();
            drawable.addState(new int[]{android.R.attr.state_pressed}, getContext().getResources().getDrawable(android.R.drawable.alert_light_frame));

            textView = new TextView(getContext());
            textView.setBackgroundDrawable(drawable);
            textView.setLines(3);
            textView.setClickable(true);
            textView.setGravity(Gravity.CENTER);
            switch (Tools.getDisplaySize(getContext())) {
                case Tools.SMALL:
                    textView.setTextSize(9);
                    break;
                case Tools.MEDIUM:
                    textView.setTextSize(11);
                    break;
                case Tools.BIG:
                    textView.setTextSize(12);
            }
            // TODO :: this should be outside of course
            textView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (onTextClickIntent != null) {
                        onTextClickIntent.putExtra("eventId", ((EventInfo) objects.get(position)).getId());
                        getContext().startActivity(onTextClickIntent);
                    }
                }
            });
            checkBox = new CheckBox(getContext());
            checkBox.setGravity(Gravity.RIGHT);

            addView(imageButton, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 3));
            addView(textView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            addView(checkBox, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 3));

            position = -1;
            checkBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (checkedItems.contains(position)) {
                        checkedItems.remove((Integer) position);
                    } else {
                        checkedItems.add(position);
                    }
                }
            });
        }
    }

}

