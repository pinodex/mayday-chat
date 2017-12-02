package ph.edu.stinovaliches.maydaychat;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import io.underdark.Underdark;
import io.underdark.transport.Link;
import io.underdark.transport.Transport;
import io.underdark.transport.TransportKind;
import io.underdark.transport.TransportListener;

/**
 * Created by raphm on 02/12/2017.
 */

public class Communication {

    private boolean started = false;

    private long nodeId;

    private String channelName;

    private Context context;

    private Transport transport;

    private TransportListener transportListener;

    private FrameListener frameListener;

    private HashMap<Long, Link> links = new HashMap<>();

    private LinkedList<Message> messages = new LinkedList<>();

    private LinkedList<String> messageIds = new LinkedList<>();

    private LinkedList<Message> relayedMessages = new LinkedList<>();

    private LinkedList<String> relayedMessageIds = new LinkedList<>();

    private NewMessageListener newMessageListener;

    private EnumSet<TransportKind> transportKinds = EnumSet.of(TransportKind.WIFI, TransportKind.BLUETOOTH);

    public Communication(Context context) {
        this.context = context;

        nodeId = generateNodeId();

        transportListener = new DefaultTransportListener();

        transport = Underdark.configureTransport(123456, nodeId,
                transportListener, null, context, transportKinds);
    }

    public void init(String nickname, String channelName) {
        this.channelName = channelName;
    }

    public void addMessage(Message incomingMessage) {
        if (!incomingMessage.channelName.equalsIgnoreCase(channelName)) {
            // Relay
            relayedMessages.add(incomingMessage);
            relayedMessageIds.add(incomingMessage.id.toString());

            if (relayedMessages.size() > 20) {
                Message message = relayedMessages.removeFirst();
                relayedMessageIds.remove(message.id.toString());
            }

            return;
        }

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
        if (started) {
            return;
        }

        transport.start();

        started = true;
    }

    public void stopTransport() {
        if (transport == null) {
            return;
        }

        transport.stop();
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

    public HashMap<Long, Link> getLinks() {
        return links;
    }

    public LinkedList<Message> getMessages() {
        return messages;
    }

    public LinkedList<String> getMessageIds() {
        return messageIds;
    }

    public LinkedList<Message> getRelayedMessages() {
        return relayedMessages;
    }

    public LinkedList<String> getRelayedMessageIds() {
        return relayedMessageIds;
    }

    public void sendSync(Link link) {
        for (Message message: getMessages()) {
            String serializedMessage = message.serializeToJson();

            Frame frame = new Frame("MESSAGE", serializedMessage);

            link.sendFrame(frame.getBytes());
        }

        for (Message message: getRelayedMessages()) {
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

    public void uploadMessages() {
        Gson gson = new Gson();

        String data = gson.toJson(messages);
        String relayedData = gson.toJson(relayedMessages);

        RequestParams params = new RequestParams();
        params.put("data", data);

        HttpClient.post("/message/send", params, new TextHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseBody) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error) {

            }
        });

        RequestParams relayedParams = new RequestParams();
        relayedParams.put("data", relayedData);

        HttpClient.post("/message/send", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    public void downloadMessages() {
        HttpClient.get("/message/get-all", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);

                Gson gson = new Gson();

                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject messageObject = response.getJSONObject(i);
                        Message message = gson.fromJson(messageObject.toString(), Message.class);

                        addMessage(message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
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
