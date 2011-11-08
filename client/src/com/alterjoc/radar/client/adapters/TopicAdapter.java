package com.alterjoc.radar.client.adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.alterjoc.radar.client.R;
import com.alterjoc.radar.client.Tools;
import com.alterjoc.radar.common.data.ImageInfo;
import com.alterjoc.radar.common.data.TopicInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Dejan Date: 24.8.2010 Time: 22:04:06
 */
public class TopicAdapter extends ArrayAdapter {

    private Map<TopicInfo, Drawable> dropDownImages;
    private Map<TopicInfo, Drawable> images;
    private boolean showSmallViewIcons;

    public TopicAdapter(Context context, int textViewResourceId, List objects,
                        boolean showSmallViewIcons) {
        super(context, textViewResourceId, objects);

        dropDownImages = new HashMap<TopicInfo, Drawable>();
        images = new HashMap<TopicInfo, Drawable>();
        this.showSmallViewIcons = showSmallViewIcons;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        Drawable image = null;
        TopicInfo topic = (TopicInfo) getItem(position);
        if (!dropDownImages.containsKey((TopicInfo) getItem(position))) {
            ImageInfo img = topic.getImage();
            byte imageBytes[] = img == null ? null : img.readFromDBIntoArray();
            if (imageBytes != null && imageBytes.length > 0) {
                image = new BitmapDrawable(BitmapFactory.decodeByteArray(
                        imageBytes, 0, imageBytes.length));
            } else {
                image = new BitmapDrawable(BitmapFactory.decodeResource(
                        getContext().getResources(), R.drawable.userdefined));
            }
            dropDownImages.put(topic, image);
        }
        if (convertView == null) {
            convertView = super.getDropDownView(position, convertView, parent);
            ((TextView) convertView).setGravity(Gravity.CENTER);
            ((TextView) convertView).setSingleLine(true);
            ((TextView) convertView).setTextSize(Tools.getButtonsFontSize(getContext()));
        }
        ((TextView) convertView).setCompoundDrawablesWithIntrinsicBounds(dropDownImages.get(topic), null, null, null);
        ((TextView) convertView).setText(topic.getName());
        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Drawable image = null;
        TopicInfo topic = (TopicInfo) getItem(position);
        if (!images.containsKey(topic)) {
            ImageInfo img = topic.getImage();
            byte imageBytes[] = img == null ? null : img.readFromDBIntoArray();
            if (imageBytes != null && imageBytes.length > 0) {
                image = new BitmapDrawable(BitmapFactory.decodeByteArray(
                        imageBytes, 0, imageBytes.length));
            } else {
                image = new BitmapDrawable(BitmapFactory.decodeResource(
                        getContext().getResources(), R.drawable.userdefined));
            }
            images.put(topic, image);
        }
        if (convertView == null) {
            convertView = super.getView(position, convertView, parent);
            ((TextView) convertView).setGravity(Gravity.CENTER);
            ((TextView) convertView).setTextSize(Tools.getButtonsFontSize(getContext()));
        }
        Drawable drawable = null;
        if (showSmallViewIcons) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            ImageInfo img = topic.getImage();
            byte imageBytes[] = img == null ? null : img.readFromDBIntoArray();
            if (imageBytes != null && imageBytes.length > 0) {
                drawable = new BitmapDrawable(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options));
            } else {
                drawable = new BitmapDrawable(BitmapFactory.decodeResource(
                        getContext().getResources(), R.drawable.userdefined, options));
            }
        } else {
            drawable = images.get(topic);
        }

        ((TextView) convertView).setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        ((TextView) convertView).setText(topic.getName());

        return convertView;
    }
}