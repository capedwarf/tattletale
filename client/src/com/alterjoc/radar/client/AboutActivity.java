package com.alterjoc.radar.client;

import android.app.ExpandableListActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.TextView;


/**
 * User: Dejan
 * Date: 18.10.2010
 * Time: 13:23:49
 */
public class AboutActivity extends ExpandableListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_LEFT_ICON);


        ExpandableListAdapter adapter = new AboutListAdapter();
        getExpandableListView().setAdapter(adapter);
        setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.tozibaba16);
    }


    private class AboutListAdapter extends BaseExpandableListAdapter {
        private String[] groups = {getResources().getString(R.string.description),
                getResources().getString(R.string.instructions),
                getResources().getString(R.string.terms_of_use),
                getResources().getString(R.string.about),
                getResources().getString(R.string.legal),
                getResources().getString(R.string.thanks)};
        private String[][] children = {
                {getResources().getString(R.string.description_info)},
                {
                        getResources().getString(R.string.map_info),
                        getResources().getString(R.string.list_info),
                        getResources().getString(R.string.detail_info),
                        getResources().getString(R.string.post_event_info),
                        getResources().getString(R.string.subscriptions_info),
                        getResources().getString(R.string.new_channel_info),
                        getResources().getString(R.string.filter_info),
                        getResources().getString(R.string.auto_archive_info),
                        getResources().getString(R.string.settings_info),
                        getResources().getString(R.string.widget_info)
                },
                {getResources().getString(R.string.terms_of_use_info)},
                {getResources().getString(R.string.development_info)},
                {
                        getResources().getString(R.string.legal_privacy_info),
                        getResources().getString(R.string.legal_wasnt_me_info),
                        getResources().getString(R.string.legal_respect_privacy_info),
                },
                {
                        getResources().getString(R.string.libraries_info),
                        getResources().getString(R.string.graphics_info)
                }
        };

        public Object getChild(int groupPosition, int childPosition) {
            return children[groupPosition][childPosition];
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            return children[groupPosition].length;
        }

        public TextView getGenericView() {
            // Layout parameters for the ExpandableListView
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);

            TextView textView = new TextView(AboutActivity.this);
            textView.setLayoutParams(lp);
            // Center the text vertically
            textView.setGravity(Gravity.TOP | Gravity.LEFT);
            // Set the text starting position
            textView.setPadding(36, 12, 0, 12);
            return textView;
        }

        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            TextView textView = getGenericView();
            textView.setText(Html.fromHtml(getChild(groupPosition, childPosition).toString()), TextView.BufferType.SPANNABLE);

            return textView;
        }

        public Object getGroup(int groupPosition) {
            return groups[groupPosition];
        }

        public int getGroupCount() {
            return groups.length;
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            TextView textView = getGenericView();
            textView.setTextSize(16);
            textView.setText(getGroup(groupPosition).toString(), TextView.BufferType.SPANNABLE);
            Spannable spannable = (Spannable) textView.getText();
            spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return textView;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean hasStableIds() {
            return true;
        }

    }

}
