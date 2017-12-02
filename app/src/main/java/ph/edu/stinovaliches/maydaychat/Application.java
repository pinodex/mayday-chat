package ph.edu.stinovaliches.maydaychat;

/**
 * Created by raphm on 02/12/2017.
 */

import android.content.Context;
import android.provider.Settings.Secure;

public class Application {

    public static int ID = 1;

    public static int NODE_ID = 1;

    public static long generateNodeId(Context context) {
        String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

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

}
