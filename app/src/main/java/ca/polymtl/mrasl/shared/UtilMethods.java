package ca.polymtl.mrasl.shared;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class UtilMethods {

    public static boolean checkPermission(Context context, String permission) {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

}
