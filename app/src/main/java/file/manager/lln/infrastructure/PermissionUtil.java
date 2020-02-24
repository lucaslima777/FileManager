package file.manager.lln.infrastructure;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Utility class for access to runtime permission.
 */
public final class PermissionUtil {

    public enum PermissionResult {
        NO_STATE, SUCCESS, ERROR
    }

    private PermissionUtil() {
        // makes sure that this utility class will not be instantitated.
    }

    /**
     * Checks if user granted a permission
     *
     * @param context - context
     * @param permission       - permission to test
     * @return true if permisson granted. false otherwise.
     */
    public static boolean isPermissionGranted(Context context, String permission) {
        int grantResult = context.checkCallingOrSelfPermission(permission);

        return grantResult == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Checks if the result contains a {@link PackageManager#PERMISSION_GRANTED} result for a
     * permission from a runtime permissions request.
     *
     * @param grantPermissions - granted permissions array
     * @param grantResults     - grant result array
     * @param permission       - permission to test
     * @return true if permisson granted. false otherwise.
     */
    public static boolean isPermissionGranted(String[] grantPermissions, int[] grantResults, String permission) {
        for (int i = 0; i < grantPermissions.length; i++) {
            if (permission.equals(grantPermissions[i])) {
                return grantResults[i] == PackageManager.PERMISSION_GRANTED;
            }
        }

        return false;
    }
}
