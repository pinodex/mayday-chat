package ph.edu.stinovaliches.maydaychat;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.widget.Toast;

import java.util.EnumSet;
import java.util.LinkedHashMap;
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

    int applicationId;

    Context context;

    Transport transport;

    TransportListener transportListener;

    LinkedHashMap<Long, Link> links;

    LinkedHashMap<Long, String> nicknames;

    FrameListener frameListener;

    String nickname;

    public Communication(int applicationId, Context context){
        this.applicationId = applicationId;
        this.context = context;

        EnumSet<TransportKind> transportKinds = EnumSet.of(
                TransportKind.WIFI, TransportKind.BLUETOOTH);

        links = new LinkedHashMap<>();
        nicknames = new LinkedHashMap<>();

        transportListener = new DefaultTransportListener();
        transport = Underdark.configureTransport(applicationId, generateNodeId(),
                transportListener, null, context, transportKinds);
    }

    public void startTransport() {
        transport.start();
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public LinkedHashMap<Long, Link> getLinks() {
        return links;
    }

    public LinkedHashMap<Long, String> getNicknames() {
        return nicknames;
    }

    public void send(Link link, Frame frame) {
        link.sendFrame(frame.getBytes());
    }

    public void send(long nodeId, Frame frame) {
        if (!links.containsKey(nodeId)) {
            return;
        }

        Link link = links.get(nodeId);

        send(link, frame);
    }

    public void broadcast(Frame frame) {
        for (Map.Entry<Long, Link> entry: getLinks().entrySet()) {
            Link link = entry.getValue();

            send(link, frame);
        }
    }

    public void initiateHandshake(Link link) {
        Frame frame = new Frame("SYN", getNickname());

        send(link, frame);
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
            initiateHandshake(link);

            links.put(link.getNodeId(), link);
        }

        @Override
        public void transportLinkDisconnected(Transport transport, Link link) {
            links.remove(link.getNodeId());
            nicknames.remove(link.getNodeId());
        }

        @Override
        public void transportLinkDidReceiveFrame(Transport transport, Link link, byte[] bytes) {
            Frame frame = Frame.fromBytes(bytes);

            frameListener.receive(frame.getKey(), frame.getValue());

            // Acknowledge client
            if (frame.getKey().equals("SYN")) {
                nicknames.put(link.getNodeId(), frame.getValue());

                Toast.makeText(context, frame.getValue() + " is connected", Toast.LENGTH_LONG).show();
            }
        }
    }

    public interface FrameListener {

        void receive(String key, String value);

    }

}
