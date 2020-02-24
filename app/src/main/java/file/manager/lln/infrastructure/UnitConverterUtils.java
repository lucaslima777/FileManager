package file.manager.lln.infrastructure;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.applyDimension;

/*
Classe para conversão de unidades
*/
public class UnitConverterUtils {

    private UnitConverterUtils(){/*Nothing to do*/}

    public static int fontConvertDpToPx(Context context, float dp) {
        return (int) applyDimension(COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static float convertDpToPx(Context context, float dp) {
        Resources res = context.getResources();
        return dp * (res.getDisplayMetrics().densityDpi / 160f);
    }

    public static int convertSpToPx(Context context, float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    public static int convertPxToDp(Context context, int pixel){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixel, context.getResources().getDisplayMetrics());
    }
}