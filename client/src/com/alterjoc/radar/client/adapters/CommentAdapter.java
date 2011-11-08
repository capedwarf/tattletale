package com.alterjoc.radar.client.adapters;

import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.alterjoc.radar.client.R;
import com.alterjoc.radar.client.Tools;
import com.alterjoc.radar.common.data.CommentInfo;

/**
 * User: Dejan
 * Date: 11.10.2010
 * Time: 11:55:19
 */
public class CommentAdapter extends ArrayAdapter {

    private String eventAuthor;

    public CommentAdapter(Context context, int textViewResourceId, List objects, String eventAuthor) {
        super(context, textViewResourceId, objects);
        this.eventAuthor = eventAuthor;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CommentInfo comment = (CommentInfo) getItem(position);
        if (convertView == null) {
            convertView = new CommentView(getContext());
        }

        StringBuilder sb = new StringBuilder();
        int[] styled = new int[3];
        sb.append(comment.getComment());
        styled[0] = 0;
        styled[1] = comment.getComment().length();
        sb.append("\n");
        String date = Tools.sdf.format(new Date(comment.getTimestamp()));
        sb.append(date);
        int count = getCount() - position;
        sb.append("    -" + count + "-");
        styled[2] = styled[1] + date.length() + 1;
        ((CommentView) convertView).commentTextView.setText(new String(sb), TextView.BufferType.SPANNABLE);
        createSpanSections((Spannable)((CommentView) convertView).commentTextView.getText(), styled);
        ((CommentView) convertView).userTexView.setText(comment.getUsername(), TextView.BufferType.SPANNABLE);
        int length = ((CommentView) convertView).userTexView.getText().length();

        if (eventAuthor != null){
            if (eventAuthor.equals(comment.getUsername())){
                ((Spannable)((CommentView) convertView).userTexView.getText()).setSpan(new ForegroundColorSpan(Color.YELLOW), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                ((Spannable)((CommentView) convertView).userTexView.getText()).setSpan(new ForegroundColorSpan(Color.rgb(255, 200, 0)), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

        }
        ((Spannable)((CommentView) convertView).userTexView.getText()).setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return convertView;
    }

    private void createSpanSections(Spannable str, int[] styled) {
        str.setSpan(new ForegroundColorSpan(Color.BLACK), styled[0], styled[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        str.setSpan(new ForegroundColorSpan(Color.GRAY), styled[1], styled[2], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        str.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), styled[1], styled[2], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private class CommentView extends LinearLayout {
        public TextView commentTextView;
        public TextView userTexView;

        public CommentView(Context context) {
            super(context);
            setBackgroundColor(Color.BLACK);
            setOrientation(LinearLayout.VERTICAL);
            commentTextView = new TextView(getContext());
            commentTextView.setPadding(2, 5, 2, 0);
            commentTextView.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.event_bubble_narrow_));
            commentTextView.setTextSize(Tools.getButtonsFontSize(getContext()));
            commentTextView.setSingleLine(false);
            commentTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            addView(commentTextView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
            userTexView = new TextView(getContext());
            userTexView.setSingleLine(true);
            userTexView.setGravity(Gravity.CENTER_HORIZONTAL);
            userTexView.setTextSize(Tools.getButtonsFontSize(getContext()) - 1);
            userTexView.setPadding(0, 0, 0, 5);
            setPadding(0, 3, 0, 3);
            addView(userTexView);
        }
    }


}
