package ph.edu.stinovaliches.maydaychat;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedList;

import io.underdark.Underdark;
import io.underdark.transport.Link;
import io.underdark.transport.Transport;
import io.underdark.transport.TransportKind;
import io.underdark.transport.TransportListener;

/**
 * Created by raphm on 02/12/2017.
 */

public class Communication {

    int applicationId;

    Context context;

    Transport transport;

    TransportListener transportListener;

    FrameListener frameListener;

    LinkedList<Message> messages;

    public Communication(int applicationId, Context context){
        this.applicationId = applicationId;
        this.context = context;

        EnumSet<TransportKind> transportKinds = EnumSet.of(
                TransportKind.WIFI, TransportKind.BLUETOOTH);

        transportListener = new DefaultTransportListener();
        transport = Underdark.configureTransport(applicationId, generateNodeId(),
                transportListener, null, context, transportKinds);

        messages = new LinkedList<>();
    }

    public LinkedList<Message> getMessages() {
        return messages;
    }

    public void addMessage(Message incomingMessage) {
        messages.add(incomingMessage);

        Collections.sort(messages, new Comparator<Message>() {
            @Override
            public int compare(Message message, Message nextMessage) {
                return message.timestamp - nextMessage.timestamp;
            }
        });
    }

    public void startTransport() {
        transport.start();
    }

    public void send(Link link, Frame frame) {
        link.sendFrame(frame.getBytes());
    }

    public void setFrameListener(FrameListener listener) {
        this.frameListener = listener;
    }

    public long generateNodeId() {
        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        byte[] deviceIdBytes = deviceId.getBytes();
        long result = 1;

        for (byte b: deviceIdBytes) {
            result *= b;
        }

        if (result < 0) {
            result *= -1;
        }

        return result;
    }

    public class DefaultTransportListener implements TransportListener {

        @Override
        public void transportNeedsActivity(Transport transport, ActivityCallback activityCallback) {
            activityCallback.accept((Activity) context);
        }

        @Override
        public void transportLinkConnected(Transport transport, Link link) {

        }

        @Override
        public void transportLinkDisconnected(Transport transport, Link link) {

        }

        @Override
        public void transportLinkDidReceiveFrame(Transport transport, Link link, byte[] bytes) {
            Frame frame = Frame.fromBytes(bytes);

            frameListener.receive(frame.getKey(), frame.getValue());
        }
    }

    public interface FrameListener {

        void receive(String key, String value);

    }

}
