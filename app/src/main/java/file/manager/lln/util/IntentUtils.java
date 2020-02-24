package file.manager.lln.util;

import android.content.Intent;

/**
 * Classe IntentUtils.
 */
public class IntentUtils {

    private IntentUtils() {

    }

    /**
     * Add pdf info to intent intent.
     *
     * @param originIntent the origin intent
     * @param newIntent    the new intent
     * @return the intent
     */
    public static Intent addPdfInfoToIntent(Intent originIntent, Intent newIntent) {
        if (newIntent == null) {
            return null;
        }
        if (originIntent != null) {
            newIntent.setData(originIntent.getData());
            newIntent.setAction(originIntent.getAction());
            if (originIntent.getExtras() != null &&
                    originIntent.getExtras().getParcelable(Intent.EXTRA_STREAM) != null) {
                newIntent.getExtras().putParcelable(Intent.EXTRA_STREAM,
                        originIntent.getExtras().getParcelable(Intent.EXTRA_STREAM));
            }
        }
        return newIntent;
    }

}
