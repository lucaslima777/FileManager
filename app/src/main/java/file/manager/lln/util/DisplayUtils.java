package file.manager.lln.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

public final class DisplayUtils {

    private DisplayUtils() {
        throw new AssertionError();
    }

    @SuppressLint("NewApi")
    private static Point getDisplayPixelSize(final Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static int getDisplayPixelWidth(final Context context) {
        Point size = getDisplayPixelSize(context);
        return size.x;
    }

    public static int getDisplayPixelHeight(final Context context) {
        Point size = getDisplayPixelSize(context);
        return size.y;
    }

    public static int dpToPx(final Context context, final int dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
        return (int) px;
    }

    public static float dipToPixels(final Context context, final float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    @SuppressLint("NewApi")
    public static boolean isEqualOrMoreThanXxxHdpi(final Context context) {
        return context.getResources().getDisplayMetrics().densityDpi
                >= DisplayMetrics.DENSITY_XXXHIGH;
    }

    public static boolean isMdpi(final Context context) {
        return context.getResources().getDisplayMetrics().densityDpi ==
                DisplayMetrics.DENSITY_MEDIUM;
    }

    public static boolean isHdpi(final Context context) {
        return context.getResources().getDisplayMetrics().densityDpi
                == DisplayMetrics.DENSITY_HIGH;
    }

    /**
     * Método que retorna a altura da status bar
     *
     * @param activity
     * @return
     */
    public static int getStatusBarHeight(Activity activity) {
        Rect screenRect = new Rect();
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(screenRect);
        return screenRect.top;
    }

}