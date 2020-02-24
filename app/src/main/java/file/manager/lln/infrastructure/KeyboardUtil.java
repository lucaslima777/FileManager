package file.manager.lln.infrastructure;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

public class KeyboardUtil {

    private Activity mActivity;
    private KeyboardStateListener mListener;
    private boolean mKeyboardIsOpened;

    private ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            Rect react = new Rect();
            int visibleThreshold = Math.round(UnitConverterUtils.convertDpToPx(mActivity, 100));
            View activityRoot = ((ViewGroup) mActivity.findViewById(android.R.id.content)).getChildAt(0);
            activityRoot.getWindowVisibleDisplayFrame(react);
            int heightDiff = activityRoot.getRootView().getHeight() - react.height();
            boolean keyboardIsVisible = heightDiff > visibleThreshold;

            if (keyboardIsVisible != mKeyboardIsOpened) {
                mKeyboardIsOpened = keyboardIsVisible;
                if (mListener != null && mKeyboardIsOpened) {
                    mListener.onKeyboardOpen();
                } else if (mListener != null) {
                    mListener.onKeyboardClose();
                }
            }
        }
    };

    public KeyboardUtil(Activity activity) {
        this.mActivity = activity;
        configListenerToRootView();
    }

    public void setOnKeyboardStateListener(KeyboardStateListener listener) {
        this.mListener = listener;
    }

    public boolean isKeyboardVisible() {
        return mKeyboardIsOpened;
    }

    /**
     * Este método não abre ou fecha o teclado! Somente muda o estado interno deste componente!
     *
     * @param keyboardIsOpened
     */
    public void forceSetKeyboardVisibility(boolean keyboardIsOpened) {
        mKeyboardIsOpened = keyboardIsOpened;
    }

    private void configListenerToRootView() {
        final View activityRootView = mActivity.getWindow().getDecorView().findViewById(android.R.id.content);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
    }

    public interface KeyboardStateListener {

        void onKeyboardOpen();

        void onKeyboardClose();
    }
}
