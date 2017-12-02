package ph.edu.stinovaliches.maydaychat;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.UUID;

public class MessagingActivity extends AppCompatActivity {

    EditText etComposer;

    ImageView sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        final ListView messageList = (ListView) findViewById(R.id.messageList);

        final MessageAdapter messageAdapter = new MessageAdapter(this,
                Application.communication.getMessages(), Application.nickname);

        messageList.setAdapter(messageAdapter);

        etComposer = (EditText) findViewById(R.id.messageComposer);
        sendButton = (ImageView) findViewById(R.id.sendButton);

        sendButton.setOnClickListener(new SendButtonListener());

        Application.communication.setNewMessageListener(new Communication.NewMessageListener() {
            @Override
            public void receive(LinkedList<Message> messages, Message message) {
                messageAdapter.notifyDataSetChanged();

                messageList.smoothScrollToPosition(messageList.getCount() -1);
            }
        });
    }

    class SendButtonListener implements Button.OnClickListener {

        @Override
        public void onClick(View view) {
            String content = etComposer.getText().toString();

            if (content.trim().length() == 0) {
                return;
            }

            Message message = new Message();

            message.id = UUID.randomUUID();
            message.senderName = Application.nickname;
            message.channelName = Application.channelName;
            message.nodeId = Application.communication.getNodeId();
            message.timestamp = System.currentTimeMillis() / 1000L;
            message.content = content;

            Application.communication.addMessage(message);

            etComposer.setText("");
        }
    }
}
