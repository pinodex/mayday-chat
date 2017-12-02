package ph.edu.stinovaliches.maydaychat;

import com.google.gson.Gson;

import java.util.UUID;

/**
 * Created by raphm on 02/12/2017.
 */

public class Message {

    public UUID id;

    public String nodeId;

    public String senderName;

    public String channelName;

    public long timestamp;

    public String content;

    public String serializeToJson() {
        Gson gson = new Gson();

        return gson.toJson(this);
    }

    public static Message fromJson(String data) {
        Gson gson = new Gson();

        return gson.fromJson(data, Message.class);
    }

}
