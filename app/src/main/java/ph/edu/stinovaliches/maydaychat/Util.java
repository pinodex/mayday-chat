package ph.edu.stinovaliches.maydaychat;

/**
 * Created by raphm on 02/12/2017.
 */

public class Util {

    public static long stringAsLong(String value) {
        byte[] bytes = value.getBytes();
        long result = 1;

        for (byte b: bytes) {
            result *= b;
        }

        if (result < 0) {
            result *= -1;
        }

        return result;
    }

    public static int stringAsInt(String value) {
        byte[] bytes = value.getBytes();
        int result = 1;

        for (byte b: bytes) {
            result *= b;
        }

        if (result < 0) {
            result *= -1;
        }

        return result;
    }

}
