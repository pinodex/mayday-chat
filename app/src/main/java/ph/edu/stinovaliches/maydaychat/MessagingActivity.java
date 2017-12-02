package ph.edu.stinovaliches.maydaychat;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ListView;

import java.util.LinkedHashMap;

public class MessagingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        ListView messageList = (ListView) findViewById(R.id.messageList);
        MessageAdapter messageAdapter = new MessageAdapter(this,
                Application.communication.getMessages(), Application.nickname);

        messageList.setAdapter(messageAdapter);
    }
}
