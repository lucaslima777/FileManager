package file.manager.lln.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class PermissionUtils {

    public static final int REQUEST_SHOWCAMERA = 0;
    public static final int REQUEST_SHOWCONTACTS = 1;
    public static final int REQUEST_LOCATION = 2;
    public static final int REQUEST_CALLPHONE = 3;
    public static final int REQUEST_WRITE_EXTERNAL = 4;
    public static final int REQUEST_READ_EXTERNAL = 5;
    public static final int REQUEST_GET_ACCOUNT = 6;
    public static final int REQUEST_RECEIVE_SMS = 7;
    public static final int REQUEST_SHARE_CATEGORY = 21;

    private static final String[] PERMISSION_SHOWLOCATION = new String[]{Manifest.permission
            .ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

    private static final String[] PERMISSION_WRITE_EXTERNAL =
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static final String[] PERMISSION_READ_STORAGE = new String[]{Manifest.permission
            .READ_EXTERNAL_STORAGE};

    private PermissionUtils() {
    }

    public static String[] getPermissionWriteExternal() {
        return PERMISSION_WRITE_EXTERNAL;
    }

    public static String[] getPermissionReadExternal() {
        return PERMISSION_READ_STORAGE;
    }

    /**
     * Checks all given permissions have been granted.
     *
     * @param grantResults results
     * @return returns true if all permissions have been granted.
     */
    public static boolean verifyPermissions(int... grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if <code>Activity</code> or <code>Fragment</code> has access to all given permissions.
     *
     * @param context     context
     * @param permissions permissions
     * @return returns true if <code>Activity</code> or <code>Fragment</code> has access to all given permissions.
     */
    public static boolean hasSelfPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks given permissions are needed to show rationale.
     *
     * @param activity    activity
     * @param permissions permission list
     * @return returns true if one of the permission is needed to show rationale.
     */
    public static boolean shouldShowRequestPermissionRationale(Activity activity, String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks given permissions are needed to show rationale.
     *
     * @param fragment fragment
     * @param permissions permission list
     * @return returns true if one of the permission is needed to show rationale.
     */
    public static boolean shouldShowRequestPermissionRationale(Fragment fragment, String... permissions) {
        for (String permission : permissions) {
            if (fragment.shouldShowRequestPermissionRationale(permission)) {
                return true;
            }
        }
        return false;
    }


    public static String[] getLocationPermissions() {
        return PERMISSION_SHOWLOCATION;
    }

}
