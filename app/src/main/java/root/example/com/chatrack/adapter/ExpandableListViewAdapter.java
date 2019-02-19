package root.example.com.chatrack.adapter;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;
import root.example.com.chatrack.R;

public class ExpandableListViewAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> expandableListTitle;
    private HashMap<String, List<String>> expandableListDetail;
    private final String TAG = "ExpandableListViewAdapter";
    private String expandedListText;
    private String expandableListUri;

    public ExpandableListViewAdapter(Context context, List<String> expandableListTitle,
                                     HashMap<String, List<String>> expandableListDetail) {
        this.context = context;
        this.expandableListTitle = expandableListTitle;
        this.expandableListDetail = expandableListDetail;
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return this.expandableListDetail.get(this.expandableListTitle.get(listPosition))
                .get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        String getWhatClicked = (String) getChild(listPosition,expandedListPosition);
        String[] getWhatClickedArray = getWhatClicked.split(",");
        if (getWhatClickedArray.length > 1){
            expandedListText =  "";
            expandableListUri = "";
            expandedListText =  getWhatClickedArray[0];
            expandableListUri = getWhatClickedArray[1];
        }else {
            expandedListText =  "";
            expandableListUri = "";
            expandedListText = getWhatClickedArray[0];
        }
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_view_item, null);
        }
        TextView expandedListTextView = (TextView) convertView
                .findViewById(R.id.ListItem);
        CircleImageView expandableListImage = (CircleImageView) convertView
                .findViewById(R.id.FriendsProfile);
        expandedListTextView.setText(expandedListText);
        Glide.with(convertView).load(expandableListUri).into(expandableListImage);
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return this.expandableListDetail.get(this.expandableListTitle.get(listPosition))
                .size();
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.expandableListTitle.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.expandableListTitle.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(listPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_view_group, null);
        }
        TextView listTitleTextView = (TextView) convertView
                .findViewById(R.id.listTitle);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(listTitle);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }
}
