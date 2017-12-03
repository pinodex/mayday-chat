package ph.edu.stinovaliches.maydaychat;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Communication communication;

    EditText etNickname, etChannel;

    Button btnConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[] {
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 1);
        }

        communication = new Communication(MainActivity.this);
        communication.setFrameListener(new DefaultFrameListener());

        Application.communication = communication;

        etNickname = (EditText) findViewById(R.id.etNickname);
        etChannel = (EditText) findViewById(R.id.etChannel);
        btnConnect = (Button) findViewById(R.id.btnConnect);

        btnConnect.setOnClickListener(new ConnectButtonListener());
    }

    protected void login(String nickname, String channelName) {
        Application.nickname = nickname;
        Application.channelName = channelName;

        communication.init(nickname, channelName);
        communication.startTransport();

        final Handler syncHandler = new Handler();

        syncHandler.postDelayed(new Runnable(){
            public void run(){
                communication.broadcastSync();

                syncHandler.postDelayed(this, 1000);
            }
        }, 1000);

        final Handler cloudHandler = new Handler();

        cloudHandler.postDelayed(new Runnable(){
            public void run(){
                communication.uploadMessages();
                communication.downloadMessages();

                cloudHandler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    protected class ConnectButtonListener implements Button.OnClickListener {
        @Override
        public void onClick(View view) {
            String nickname  = etNickname.getText().toString();
            String channelName = etChannel.getText().toString();

            if (nickname.trim().length() == 0)  {
                Application.showAlertDialog(MainActivity.this, "Error",
                        "Please enter a nickname");

                return;
            }

            if (!channelName.matches("[a-zA-Z0-9.\\\\]{3,}"))  {
                Application.showAlertDialog(MainActivity.this, "Error",
                        "Channel should consist of at least 3 alphanumeric characters.");

                return;
            }

            login(nickname, channelName);

            Intent intent = new Intent(MainActivity.this, MessagingActivity.class);
            startActivity(intent);
        }
    }

    protected class DefaultFrameListener implements Communication.FrameListener {

        @Override
        public void receive(String key, String value) {
            Log.d("FRAME", key + ": " + value);
        }
    }
}
