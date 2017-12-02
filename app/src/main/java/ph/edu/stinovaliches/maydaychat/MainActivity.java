package ph.edu.stinovaliches.maydaychat;

import android.app.Activity;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import io.underdark.Underdark;
import io.underdark.transport.Link;
import io.underdark.transport.Transport;
import io.underdark.transport.TransportKind;
import io.underdark.transport.TransportListener;
import android.provider.Settings.Secure;
import android.os.Handler;
import android.util.Log;

import java.util.EnumSet;

public class MainActivity extends AppCompatActivity {

    protected Transport transport;

    protected TransportListener transportListener;

    protected Handler handler;

    protected Activity activity;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;

        transportListener = new DefaultTransportListener();
        handler = new Handler(new DefaultHandlerCallback());

        EnumSet<TransportKind> transportKinds = EnumSet.of(TransportKind.WIFI, TransportKind.BLUETOOTH);

        transport = Underdark.configureTransport(
                Application.ID,
                Application.generateNodeId(getApplicationContext()),
                transportListener,
                handler,
                this,
                transportKinds);

        transport.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        transport.onMainActivityPaused();
    }

    @Override
    protected void onResume() {
        super.onResume();

        transport.onMainActivityResumed();
    }

    class DefaultTransportListener implements TransportListener {
        @Override
        public void transportNeedsActivity(Transport transport, ActivityCallback activityCallback) {
            Log.d("TRANSPORT_LISTENER", "Needs Activity");

            activityCallback.accept(activity);
        }

        @Override
        public void transportLinkConnected(Transport transport, Link link) {
            Log.d("TRANSPORT_LISTENER", "Connected: " + link.getNodeId());

            Frame introductionFrame = new Frame("HANDSHAKE", android.os.Build.MODEL);

            link.sendFrame(introductionFrame.getBytes());
        }

        @Override
        public void transportLinkDisconnected(Transport transport, Link link) {
            Log.d("TRANSPORT_LISTENER", "Disconnected: " + link.getNodeId());
        }

        @Override
        public void transportLinkDidReceiveFrame(Transport transport, Link link, byte[] bytes) {
            Frame frame = Frame.fromBytes(bytes);

            Log.d("TRANSPORT_LISTENER", "Frame received message from " + link.getNodeId() + ": " + frame.getKey() + ": " + frame.getValue());
        }
    }

    class DefaultHandlerCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message message) {
            return true;
        }
    }
}
