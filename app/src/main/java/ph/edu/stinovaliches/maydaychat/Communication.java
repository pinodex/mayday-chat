package ph.edu.stinovaliches.maydaychat;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import io.underdark.Underdark;
import io.underdark.transport.Link;
import io.underdark.transport.Transport;
import io.underdark.transport.TransportKind;
import io.underdark.transport.TransportListener;

/**
 * Created by raphm on 02/12/2017.
 */

public class Communication {

    long nodeId;

    Context context;

    Transport transport;

    TransportListener transportListener;

    FrameListener frameListener;

    HashMap<Long, Link> links = new HashMap<>();

    LinkedList<Message> messages = new LinkedList<>();

    LinkedList<String> messageIds = new LinkedList<>();

    NewMessageListener newMessageListener;

    EnumSet<TransportKind> transportKinds = EnumSet.of(TransportKind.WIFI, TransportKind.BLUETOOTH);

    public Communication(Context context) {
        this.context = context;

        nodeId = generateNodeId();

        transportListener = new DefaultTransportListener();
    }

    public void init(String nickname, String channelName) {
        int id = Util.stringAsInt(channelName);

        transport = Underdark.configureTransport(id, nodeId,
                transportListener, null, context, transportKinds);
    }

    public LinkedList<Message> getMessages() {
        return messages;
    }

    public void addMessage(Message incomingMessage) {
        if (messageIds.contains(incomingMessage.id.toString())) {
            return;
        }

        messages.add(incomingMessage);
        messageIds.add(incomingMessage.id.toString());

        Collections.sort(messages, new Comparator<Message>() {
            @Override
            public int compare(Message message, Message nextMessage) {
                return Long.compare(message.timestamp, nextMessage.timestamp);
            }
        });

        if (newMessageListener != null) {
            newMessageListener.receive(messages, incomingMessage);
        }

        if (messages.size() > 20) {
            Message message = messages.removeFirst();
            messageIds.remove(message.id.toString());
        }
    }

    public void setNewMessageListener(NewMessageListener newMessageListener) {
        this.newMessageListener = newMessageListener;
    }

    public void startTransport() {
        transport.start();
    }

    public void setFrameListener(FrameListener listener) {
        this.frameListener = listener;
    }

    public long getNodeId () {
        return nodeId;
    }

    public long generateNodeId() {
        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        return Util.stringAsLong(deviceId);
    }

    public void sendSync(Link link) {
        for (Message message: getMessages()) {
            String serializedMessage = message.serializeToJson();

            Frame frame = new Frame("MESSAGE", serializedMessage);

            link.sendFrame(frame.getBytes());
        }
    }

    public void broadcastSync() {
        for (Link link: links.values()) {
            sendSync(link);
        }
    }

    public class DefaultTransportListener implements TransportListener {

        @Override
        public void transportNeedsActivity(Transport transport, ActivityCallback activityCallback) {
            activityCallback.accept((Activity) context);
        }

        @Override
        public void transportLinkConnected(Transport transport, Link link) {
            links.put(link.getNodeId(), link);
        }

        @Override
        public void transportLinkDisconnected(Transport transport, Link link) {
            links.remove(link.getNodeId());
        }

        @Override
        public void transportLinkDidReceiveFrame(Transport transport, Link link, byte[] bytes) {
            Frame frame = Frame.fromBytes(bytes);

            frameListener.receive(frame.getKey(), frame.getValue());

            if (frame.getKey().equals("MESSAGE")) {
                Message message = Message.fromJson(frame.getValue());

                addMessage(message);

                Log.d("TRANSPORT", "Message: " + frame.getValue());
            }
        }
    }

    public interface FrameListener {
        void receive(String key, String value);
    }

    public interface NewMessageListener {
        void receive(LinkedList<Message> messages, Message message);
    }

}
