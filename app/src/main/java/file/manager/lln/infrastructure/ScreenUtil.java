package file.manager.lln.infrastructure;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;

import file.manager.lln.R;

public class ScreenUtil {

    private static final boolean OPEN_KEYBOARD = true;
    private static final boolean CLOSE_KEYBOARD = false;
    private static final String RESOURCE_NAME = "status_bar_height";
    private static final String RESOURCE_TYPE = "dimen";
    private static final String RESOURCE_PACKAGE = "android";
    private static final String LOG_MESSAGE_FORMAT = "# %s #";
    private static final String FILE_PREFIX_FORMAT = "%s_";
    private static final String FILE_SUFFIX = ".png";
    private static final String AUTHORITY_SUFFIX = ".provider";
    private static final int SEARCH_BAR_HEIGHT = 67;

    private ScreenUtil() {
        // Makes sure that utility classes do not have a public constructor. Disallow instantiating.
    }

    public static int getSearchBarHeight() {
        return SEARCH_BAR_HEIGHT;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int resourceId = context.getResources().getIdentifier(RESOURCE_NAME, RESOURCE_TYPE, RESOURCE_PACKAGE);

            if (resourceId > 0) {
                result = context.getResources().getDimensionPixelSize(resourceId);
            }
        }

        return result;
    }

    public static int getToolBarHeight(Context context) {
        int[] attrs = new int[]{R.attr.actionBarSize};
        TypedArray ta = context.obtainStyledAttributes(attrs);
        int toolBarHeight = ta.getDimensionPixelSize(0, -1);
        ta.recycle();

        return toolBarHeight;
    }

    public static void configureMarginTop(Context context, View view) {
        if (view != null) {
            view.setPadding(view.getPaddingLeft(), view.getPaddingTop() + getStatusBarHeight(context), view.getPaddingRight(), view.getPaddingBottom());
        }
    }

    public static void configureMarginTopFitsWindows(Context context, View toolbar, View view) {
        configureMarginTop(context, toolbar);

        if (view != null) {
            view.setPadding(view.getPaddingLeft(), view.getPaddingTop() - getStatusBarHeight(context), view.getPaddingRight(), view.getPaddingBottom());
        }
    }

    private static void openCloseKeyboard(Context context, View view, boolean action) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        try {
            if (action) {
                imm.showSoftInput(view, 0);
            } else {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception e) {
            Log.e(ScreenUtil.class.getSimpleName(), action ? "openKeyboard" : "closeKeyboard", e);
        } finally {
            Log.e(ScreenUtil.class.getSimpleName(), String.format(LOG_MESSAGE_FORMAT, action ? "openKeyboard" : "closeKeyboard"));
        }
    }

    public static void openKeyboard(final Context context, final View view) {
        openCloseKeyboard(context, view, OPEN_KEYBOARD);
    }

    public static void closeKeyboard(Context context, View view) {
        openCloseKeyboard(context, view, CLOSE_KEYBOARD);
    }

    public static void closeKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = activity.getWindow().getDecorView().getRootView();
        }
        closeKeyboard(activity, view);
    }

    public static void hideStatusBar(Window window) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }

        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public static Display getDisplay(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay();
    }

    public static int getScreenWidth(Context context) {
        return getScreenSize(context).x;
    }

    public static Point getScreenSize(Context context) {
        Display display = getDisplay(context);
        Point size = new Point();
        display.getSize(size);

        return size;
    }

    public static float pxToDp(Context context, int px) {
        Display display = getDisplay(context);
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        return px / metrics.density;
    }

    /**
     * @param view
     * @return
     */
    public static Bitmap screenShot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    /**
     * @param view
     * @param imageName
     * @return
     */
    public static Uri screenShotAndSave(View view, String imageName) {
        File file;
        try {
            Bitmap bitmap = screenShot(view);
            File imageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            file = File.createTempFile(String.format(FILE_PREFIX_FORMAT, imageName), FILE_SUFFIX, imageDirectory);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            if (file.length() == 0) {
                file = null;
            }
        } catch (Exception e) {
            Log.e(ScreenUtil.class.getSimpleName(), "screenShotAndSave - ", e);
            file = null;
        }
        // Provider para compartilhar a tela em devices maiores que o Nougat
        return file != null ? FileProvider.getUriForFile(view.getContext(), view.getContext().getPackageName() + AUTHORITY_SUFFIX, file) : null;
    }

    /**
     * Esse método calcula a distancia de uma view do top da tela
     * ignorando se essa view está dentro de um parent ou não
     *
     * @param view
     * @return distancia da View para o top da tela
     */
    public static int getRelativeTop(View view) {
        if (view.getParent() == view.getRootView()) {
            return view.getTop();
        } else {
            return view.getTop() + getRelativeTop((View) view.getParent());
        }
    }
}