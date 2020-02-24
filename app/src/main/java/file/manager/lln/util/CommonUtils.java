package file.manager.lln.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Comparator;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


import static android.util.Patterns.EMAIL_ADDRESS;

public class CommonUtils {

    private static final Locale BRAZIL = new Locale("pt", "BR");
    private static final DecimalFormatSymbols REAL = new DecimalFormatSymbols(BRAZIL);
    private static final String EMPTY_STRING = "";
    private static final String DIGIT_CHARACTER = "-";
    private static final String DIGIT_TEXT = "dígito";
    private static final String STRING_EMPTY = "";
    private static final String PERCENT_SYMBOL = "%";
    private static final String TYPE_PERSON_ASSOCIATED = "COLIGADA";
    private static final String TYPE_PERSON_EMPLOYEE = "FUNCIONARIO";
    private static final String TYPE_PERSON_CORPORATE = "JURIDICA";
    private static final String TYPE_PERSON_INDIVIDUAL = "FISICA";

    private static final Map<String, String> TYPE_PERSON;

    private static SortedMap<Currency, Locale> currencyLocaleMap;

    static {
        TYPE_PERSON = new HashMap<>();
        TYPE_PERSON.put("0", TYPE_PERSON_INDIVIDUAL);
        TYPE_PERSON.put("1", TYPE_PERSON_CORPORATE);
        TYPE_PERSON.put("2", TYPE_PERSON_EMPLOYEE);
        TYPE_PERSON.put("3", TYPE_PERSON_ASSOCIATED);

        currencyLocaleMap = new TreeMap<>(new Comparator<Currency>() {
            @Override
            public int compare(Currency o1, Currency o2) {
                return o1.getCurrencyCode().compareTo(o2.getCurrencyCode());
            }
        });
        for (Locale locale : Locale.getAvailableLocales()) {
            try {
                Currency currency = Currency.getInstance(locale);
                currencyLocaleMap.put(currency, locale);
            } catch (IllegalArgumentException ex) {
                Log.e("erro", ex.getMessage());
            }
        }
    }

    public enum UserTypeEnum {
        ITAU,
        PERSONNALITE,
        UNICLASS
    }

    private static UserTypeEnum userTypeEnum;

    private CommonUtils() {
    }

    public static void setUserTypeEnum(UserTypeEnum userType) {
        userTypeEnum = userType;
    }

    public static UserTypeEnum getUserTypeEnum() {
        return userTypeEnum;
    }

    public static DecimalFormat getLabeledMoneyFormatter() {
        return new DecimalFormat("¤ ###,###,##0.00", REAL);
    }

    public static DecimalFormat getLabeledDollarFormatter() {
        return new DecimalFormat("US$ ###,###,##0.00", REAL);
    }

    public static DecimalFormat getLabeledMoneyMinusFormatter() {
        return new DecimalFormat("R$ ###,###,##0.00;- R$ ###,###,##0.00", REAL);
    }

    public static DecimalFormat getMoneyFormatter() {
        return new DecimalFormat("###,###,##0.00", REAL);
    }

    public static DecimalFormat getDoubleFormatter() {
        return new DecimalFormat("###,###,##0.00");
    }

    public static DecimalFormat getNumberFormatter() {
        return new DecimalFormat("###,###,##0", REAL);
    }

    public static DecimalFormat getPercentualFormatter() {
        DecimalFormat df = new DecimalFormat("##0.00", REAL);
        df.setPositiveSuffix("%");
        return df;
    }

    /**
     * Formatter para valores percentuais vindos da API
     * @return DecimalFormat formatter percentual para mais casas decimais
     */
    public static DecimalFormat getPercentualAPIFormatter() {
        DecimalFormat df = new DecimalFormat("###0.#####", REAL);
        df.setPositiveSuffix(PERCENT_SYMBOL);
        df.setNegativeSuffix(PERCENT_SYMBOL);
        return df;
    }

    private static String getCurrencySymbol(Locale locale) {
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(locale);
        return decimalFormatSymbols.getCurrencySymbol();
    }

    public static String getCurrencySymbol() {
        return getCurrencySymbol(BRAZIL);
    }

    public static String getCurrencySymbol(String currencyCode) {
        if (currencyCode != null) {
            try {
                Currency currency = Currency.getInstance(currencyCode);
                Locale locale = currencyLocaleMap.get(currency);
                if (locale != null) {
                    return currency.getSymbol(locale);
                }
            } catch (IllegalArgumentException ex) {
                Log.e("ERRO", ex.getMessage());
            }
        }
        return currencyCode;
    }

    public static boolean appInstalledOrNot(Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        boolean appInstalled;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            appInstalled = true;
        } catch (NameNotFoundException e) {
            Log.e("ERRO", e.getMessage());
            appInstalled = false;
        }
        return appInstalled;
    }

    public static String getVersion(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (NameNotFoundException e) {
            Log.e(e.getMessage(), e.getMessage());
            return EMPTY_STRING;
        }
    }

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && EMAIL_ADDRESS.matcher(email).matches();
    }

    public static int getResourceFromAttr(final Context context, final int attr) {
        final TypedValue typedValueAttr = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValueAttr, true);
        return typedValueAttr.resourceId;
    }

    public static boolean isKitKatOrNewer() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static double getDouble(String string) {
        return Double.parseDouble(TextUtils.isEmpty(string) ? "0" : string.replace(".", "").replace(",", "."));
    }

    public static boolean isDACCorrect(String numberValidate) {
        if (numberValidate.length() != 10) {
            return false;
        }
        int dv = calculateDAC(numberValidate.substring(0, 9));
        int dac = Integer.parseInt(numberValidate.substring(numberValidate.length() - 1));
        return dv == dac;
    }

    public static int calculateDAC(String numberValidate) {
        int sum = 0;
        int dv = 0;
        StringBuilder bldNumber = new StringBuilder();
        boolean doubleMultiply = true;

        for (int i = numberValidate.length(); i > 0; i--) {
            if (doubleMultiply) {
                bldNumber.append(String.valueOf(Integer.parseInt(numberValidate.substring(i - 1, i)) * 2));
            } else {
                bldNumber.append(String.valueOf(Integer.parseInt(numberValidate.substring(i - 1, i))));
            }
            doubleMultiply = !doubleMultiply;
        }

        String number = bldNumber.toString();

        for (int j = number.length() - 1; j >= 0; j--) {
            sum += Integer.valueOf(String.valueOf(number.charAt(j)));
        }
        int mod = sum % 10;
        if (mod != 0) {
            dv = 10 - mod;
        }

        return dv;
    }

    public static String getNumberToAccessibility(String number) {
        StringBuilder builder = new StringBuilder();
        for (char letter : number.toCharArray()) {
            builder.append(letter).append(DIGIT_CHARACTER);
        }
        return builder.toString();
    }

    public static String getAccountNumberInFullWithDigit(String account) {

        if (account.isEmpty() || (!account.contains(DIGIT_CHARACTER))) {
            return account;
        }

        String accountWithoutDigit = account.replaceAll(DIGIT_CHARACTER, STRING_EMPTY);

        StringBuilder builder = new StringBuilder();
        builder.append(accountWithoutDigit);
        builder.insert(accountWithoutDigit.length() - 1, DIGIT_TEXT);

        return builder.toString();
    }

    public static Double cleanAndParseToDouble(String strValue) {
        if (strValue != null && strValue.length() > 0) {
            String strLocal = strValue.replaceAll("[^0-9]*", "");

            if (strLocal.trim().isEmpty()) {
                return 0.0;
            }

            return Double.valueOf(strLocal) / 100;
        }

        return 0.0;
    }

    /**
     * Metodo para capturar teclado nativo
     * @param context - Contexto do teclado
     * @return InputMethodManager para manipular teclado
     */
    private static InputMethodManager getKeyboard(Context context) {
        return (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    /**
     * Metodo para fechar teclado nativo do android
     * @param view - View do teclado
     */
    public static void closeKeyboard(View view) {
        getKeyboard(view.getContext()).hideSoftInputFromWindow(view.getWindowToken(),0);
    }

    /**
     * Metodo que seta estado de clickable para multiplas views
     * @param clickable - booleano clickable passado para as views
     * @param views - views para setar como clickable baseado no booleano
     */
    public static void setClickableViews(boolean clickable, View... views) {
        for (View view : views) {
            view.setClickable(clickable);
        }
    }

    public static String getTypePerson(final String typePersonKey) {
        if (typePersonKey != null) {
            return TYPE_PERSON.get(typePersonKey);
        }
        return "";
    }

    /**
     * Retorna um valor monetário formatado com o sinal de positivo(+) / negativo(-) seguido da
     * moeda.
     * @param value Valor a ser formatado
     * @return Valor formatado Ex: + R$ 1.000,00 / - R$ 1.000,00
     */
    public static String getFormattedCurrencyValueWithSignal(Double value) {
        return getFormattedCurrencyValueWithSignal(value, false);
    }

    /**
     * Retorna um valor monetário formatado com o sinal de positivo(+) / negativo(-) seguido da
     * moeda.
     * @param value Valor a ser formatado
     * @param suppressAddSymbol Parametro para remover o sinal de positivo(+)
     * @return Valor formatado Ex: R$ 1.000,00 / + R$ 1.000,00 / - R$ 1.000,00
     */
    public static String getFormattedCurrencyValueWithSignal(Double value, boolean
            suppressAddSymbol) {

        DecimalFormat decimalFormat;
        if (!suppressAddSymbol) {
            decimalFormat = new DecimalFormat("+ R$ ###,###,##0.00;- R$ ###,###,##0.00", REAL);
        } else {
            decimalFormat = new DecimalFormat("R$ ###,###,##0.00;- R$ ###,###,##0.00", REAL);
        }
        return decimalFormat.format(value);

    }

}
