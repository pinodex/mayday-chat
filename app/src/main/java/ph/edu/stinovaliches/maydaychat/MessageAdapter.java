package ph.edu.stinovaliches.maydaychat;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.LinkedList;

/**
 * Created by raphm on 02/12/2017.
 */

public class MessageAdapter extends BaseAdapter {

    private LinkedList<Message> messages;

    private LayoutInflater mInflater;

    private String nickname;

    public MessageAdapter(Context context, LinkedList<Message> messages, String nickname) {
        this.messages = messages;
        this.nickname = nickname;

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public LinkedList<Message> getMessages() {
        return messages;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Message getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        Message message = messages.get(position);

        holder = new ViewHolder();

        convertView = mInflater.inflate(R.layout.layout_message_receiver, null);

        if (Application.communication.getNodeId().equalsIgnoreCase(message.nodeId)) {
            convertView = mInflater.inflate(R.layout.layout_message_sender, null);
        }

        holder.senderName = convertView.findViewById(R.id.senderName);
        holder.senderInitial = convertView.findViewById(R.id.chatHeadContent);
        holder.messageContent = convertView.findViewById(R.id.messageContent);

        convertView.setTag(holder);

        holder.senderName.setText(message.senderName);
        holder.senderInitial.setText(
                String.valueOf(Character.toUpperCase(message.senderName.charAt(0))));
        holder.messageContent.setText(message.content);

        return convertView;
    }

    public static class ViewHolder {
        public TextView senderName, senderInitial, messageContent;
    }

}
