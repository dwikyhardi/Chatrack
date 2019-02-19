package root.example.com.chatrack.adapter;


import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import root.example.com.chatrack.R;
import root.example.com.chatrack.dataModel.getChatHistory;

public class ListViewAdapter extends ArrayAdapter<getChatHistory> implements View.OnClickListener {

    private ArrayList<getChatHistory> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView SenderName,TimeStamp,ChatInBuble;
        CircleImageView profileImageChat;
    }

    public ListViewAdapter(ArrayList<getChatHistory> data, Context context) {
        super(context, R.layout.chat_style, data);
        this.dataSet = data;
        this.mContext = context;

    }

    @Override
    public void onClick(View v) {

        int position = (Integer) v.getTag();
        Object object = getItem(position);
        getChatHistory dataModel = (getChatHistory) object;

        switch (v.getId()) {

        }
    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        getChatHistory dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.chat_style, parent, false);
            viewHolder.ChatInBuble = (TextView) convertView.findViewById(R.id.ChatInBuble);
            viewHolder.SenderName = (TextView) convertView.findViewById(R.id.SenderName);
            viewHolder.TimeStamp = (TextView) convertView.findViewById(R.id.TimeStamp);
            viewHolder.profileImageChat =(CircleImageView) convertView.findViewById(R.id.profileImageChat);
            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }
        viewHolder.TimeStamp.setText(dataModel.getChatTime());
        viewHolder.SenderName.setText(dataModel.getChatUserName());
        viewHolder.ChatInBuble.setText(dataModel.getChatMessage());
        lastPosition = position;
        // Return the completed view to render on screen
        return convertView;
    }
}

