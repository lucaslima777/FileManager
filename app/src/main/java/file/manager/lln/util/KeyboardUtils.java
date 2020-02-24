package file.manager.lln.util;

import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Classe utilitaria para o keyboard do device
 */
public final class KeyboardUtils {

    /**
     * Altura da área para visualização do teclado.
     * </p>
     * Considerando que a altura mínina de um botão é de 32 dp e no teclado temos 4 linhas:
     * 32 * 4 = 128 dp
     */
    private static final int SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD = 128;

    /**
     * Construtor privado para impedir a criação de instâncias do mesmo.
     */
    private KeyboardUtils() {
        // construtor privado
    }

    /**
     * Método para calcular na tela se o teclado esta vísivel
     *
     * @param rootView View layout root da tela
     * @return boolean retorno se o teclado esta vísivel
     */
    public static boolean isKeyboardShown(final View rootView) {

        //Desenha um retangulo na tela
        Rect rect = new Rect();
        rootView.getWindowVisibleDisplayFrame(rect);

        //Obtém a métrica de exibição da tela
        DisplayMetrics dm = rootView.getResources().getDisplayMetrics();

        //Diferença de altura entre a view e sua área visível
        int heightDiff = rootView.getRootView().getHeight() - (rect.bottom - rect.top);

        //Verifica se a diferença da altura na view é maior que o teclado visivel
        return Math.abs(heightDiff) > SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD * dm.density;
    }

    /**
     * Método para exibir o teclado na tela
     *
     * @param context contexto
     * @return
     */
    public static void exibeTelaEscolhaTeclado(Context context) {
        InputMethodManager ime = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (ime != null) {
            ime.showInputMethodPicker();
        }
    }
}