package ph.edu.stinovaliches.maydaychat;

/**
 * Created by raphm on 02/12/2017.
 */

public class Frame {

    protected String key, value;

    public Frame(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public static Frame fromBytes(byte[] bytes) {
        String content = new String(bytes);
        String[] parts = content.split("\\|");

        return new Frame(parts[0], parts[1]);
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return String.format("%s|%s", this.key, this.value);
    }

    public byte[] getBytes() {
        return this.toString().getBytes();
    }

}
