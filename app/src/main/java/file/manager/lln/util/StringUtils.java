package file.manager.lln.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import file.manager.lln.R;

public final class StringUtils {

    public static final int IDENTIFICATION_LENGTH = 8;
    public static final int RECEIPT_LENGTH = 20;
    public static final int CHEQUE_LENGTH = 30;
    public static final int VOUCHER_COMMENT_LENGTH = 80;
    private static final int MINIMUM_FOR_SINGULAR = 1;
    private static final int MINIMUM_FOR_PLURAL = 2;
    public static final String UNDERSCORE = "_";
    public static final String SPACE = " ";
    public static final String EMPTY_STRING = "";
    public static final String SUSPENSION_POINTS = "...";
    public static final String SUSPENSION_POINTS_CHARACTER = String.valueOf('\u2026');

    private static final List<DoNotCapitalize> DO_NOT_CAPITALIZES = new ArrayList<>();
    public static final String HIDDEN_POINT_EMAIL = "\u2022";
    private static final String AT_SYMBOL = "@";
    private static final Pattern EMAIL_ADDRESS = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
    );
    private static final String REMOVE_LEADING_ZEROES_REGEX = "^0*";

    private StringUtils() {
    }

    public static String getNumberWithTwoDigits(int value) {
        return String.format("%02d", value);
    }

    public static String onlyNumbers(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\D+", "");
    }

    public static String getFirstWordAndCapitalize(String text) {
        return capitalize(getFirstWord(text));
    }

    public static String capitalize(String text) {
        if (text == null) {
            return null;
        }
        if (text.length() == 1) {
            return text.toUpperCase();
        }
        if (text.length() > 1) {
            return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
        }
        return "";
    }

    public static String capitalizeWordByWord(final String text) {
        if (text == null) {
            return null;
        }

        final StringBuilder ret = new StringBuilder(text.length());

        for (final String word : text.split(" ")) {
            if (!word.isEmpty()) {
                ret.append(capitalize(word));
            }
            if (ret.length() != text.length()) {
                ret.append(" ");
            }
        }

        return ret.toString();
    }

    public static String getFirstWord(String text) {
        if (text == null) {
            return null;
        }
        if (text.contains(SPACE)) {
            return text.substring(0, text.indexOf(SPACE));
        }
        return text;
    }

    public static String getNameInitials(String name) {
        if (name == null) {
            return null;
        }
        if (name.length() == 1) {
            return name.toUpperCase();
        }

        String[] names = name.split(SPACE);

        String firstName = names[0];

        if (names.length > 1) {
            String lastName = names[names.length - 1];
            return getFirstLetter(firstName) + getFirstLetter(lastName);
        }

        return getFirstLetter(firstName);
    }

    private static String getFirstLetter(String text) {
        if (text == null) {
            return null;
        }
        if (text.length() == 1) {
            return text.toUpperCase();
        }
        if (text.length() > 1) {
            return text.substring(0, 1).toUpperCase();
        }

        return "";
    }

    public static boolean contains(String value, String part) {
        return !TextUtils.isEmpty(value) && !TextUtils.isEmpty(part) &&
                value.toLowerCase().contains(part.toLowerCase());
    }

    /**
     * Esse método gera uma nova string com todas as primeiras letras das palavras maiúsculas
     *
     * @param text texto de entrada
     * @return texto de saída, com todas letras maiúsculas
     */
    public static String capitalizeWords(final String text) {
        return capitalizeWords(text, false);
    }

    /**
     * Esse método gera uma nova string com todas as primeiras letras das palavras maiúsculas
     *
     * @param text             texto de entrada
     * @param ignoreExceptions True se quiser ignorar exceções (palavras que não terão as
     *                         primeiras letras maiúsculas (ver método getDoNotCapitalizes())
     * @return texto de saída
     */
    public static String capitalizeWords(final String text, final boolean ignoreExceptions) {

        if (TextUtils.isEmpty(text)) {
            return text;
        }

        String word;
        String[] words = text.split(SPACE);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                builder.append(SPACE);
            }

            word = words[i];
            if (!ignoreExceptions) {
                DoNotCapitalize doNotCapitalize;
                doNotCapitalize = getDoNotCapitalize(word);
                if (doNotCapitalize != null) {
                    builder.append(doNotCapitalize.isToLowerCase() ? word.toLowerCase() : word);
                    continue;
                }
            }

            builder.append(capitalize(word));
        }

        return builder.toString();
    }

    private static List<DoNotCapitalize> getDoNotCapitalizes() {
        if (DO_NOT_CAPITALIZES.isEmpty()) {
            DO_NOT_CAPITALIZES.add(new DoNotCapitalize("da", true));
            DO_NOT_CAPITALIZES.add(new DoNotCapitalize("de", true));
            DO_NOT_CAPITALIZES.add(new DoNotCapitalize("do", true));
            DO_NOT_CAPITALIZES.add(new DoNotCapitalize("s.a"));
            DO_NOT_CAPITALIZES.add(new DoNotCapitalize("s.a."));
            DO_NOT_CAPITALIZES.add(new DoNotCapitalize("s/a"));
        }

        return DO_NOT_CAPITALIZES;
    }

    private static DoNotCapitalize getDoNotCapitalize(String value) {
        for (final DoNotCapitalize doNotCapitalize : getDoNotCapitalizes()) {
            if (doNotCapitalize.getWord().equalsIgnoreCase(value)) {
                return doNotCapitalize;
            }
        }

        return null;
    }

    public static String subStringIfNeeded(String value, int length) {
        String term = value;
        if (isntEmpty(term) && term.length() > length) {
            term = term.substring(term.length() - length);
        }
        return term;
    }

    public static List<String> splitString(String value, final int split) {
        if (value == null) {
            return new ArrayList<>();
        }
        List<String> list = new ArrayList<>();
        for (int i = 0; i < value.length(); i = i + split) {
            int endindex = Math.min(i + split, value.length());
            list.add(value.substring(i, endindex));
        }
        return list;
    }

    public static String[] iterableToArray(Iterable<String> iter) {
        List<String> list = new ArrayList<>();
        for (String item : iter) {
            list.add(item);
        }
        return list.toArray(new String[list.size()]);
    }

    public static boolean isntEmpty(String string) {
        return string != null && string.trim().length() > 0;
    }

    public static String fillStringWithChar(String str, Character charToFill, int maxLenght) {
        String term = str;
        if (term.length() < maxLenght) {
            term = charToFill + term;
            return fillStringWithChar(term, charToFill, maxLenght);
        } else {
            return term;
        }
    }


    public static String removeAccents(String targetStr) {
        return Normalizer.normalize(targetStr, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }


    public static String getFirstName(String name) {
        if (TextUtils.isEmpty(name)) {
            return "";
        }

        if (name.contains(" ")) {
            return name.substring(0, name.indexOf(' '));
        }

        return name;
    }


    public static Spanned getHtmlSpannedText(@NonNull String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return getSpannedAndroidN(text);
        } else {
            //noinspection deprecation
            return getSpanned(text);
        }
    }

    private static Spanned getSpanned(@NonNull String text) {
        return Html.fromHtml(text);
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Spanned getSpannedAndroidN(@NonNull String text) {
        return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY);
    }

    public static String getLastName(String name) {
        String lastName = "";

        if (TextUtils.isEmpty(name)) {
            return name;
        }

        String[] names = name.split(SPACE);
        if (names.length > 1) {
            lastName = names[names.length - 1];
        }
        return lastName;
    }

    /**
     * Trata a remoção de caracteres em campos formatados com máscaras, por exemplo Boleto, CPF,
     * CNPJ, etc
     *
     * @param editable campo preenchido
     * @param mask     máscara utilizada
     * @return StringBuilder atualizado
     */
    public static StringBuilder removeCaracterOnMaskedField(Editable editable, char[] mask) {
        final StringBuilder builder = new StringBuilder(editable);

        // Obtém a posição do último caracter excluído
        final int posicaoUltimoCaracter =
                mask.length > editable.length() ? editable.length() : mask.length - 1;

        // Verifica se o último caracter que foi excluído fazia parte da máscara
        final boolean ultimoCaracterEraMascara = mask[posicaoUltimoCaracter] != '#';

        // Se o último caracter excluido fazia parte da máscara, deve excluir até o primeiro
        // caracter que não faz parte da máscara
        if (ultimoCaracterEraMascara) {
            boolean encontrouCaracterValido = false;
            while (builder.length() > 0 && !encontrouCaracterValido) {
                encontrouCaracterValido = mask[builder.length() - 1] == '#';
                builder.deleteCharAt(builder.length() - 1);
            }
        }

        return builder;
    }

    public static boolean isEmpty(@Nullable String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isBlank(@Nullable String str) {
        return isEmpty(str) || str.trim().isEmpty();
    }


    public static String getFirstAndLastName(String fullName) {
        if (StringUtils.isntEmpty(fullName)) {
            String name = StringUtils.getFirstName(fullName).concat(" ").concat(StringUtils.getLastName(fullName));
            return StringUtils.capitalizeWords(name);
        }
        return "";
    }

    private static class DoNotCapitalize {
        private final String word;
        private final boolean toLowerCase;

        DoNotCapitalize(final String word) {
            this(word, false);
        }

        DoNotCapitalize(final String word, final boolean toLowerCase) {
            this.word = word;
            this.toLowerCase = toLowerCase;
        }

        String getWord() {
            return word;
        }

        public boolean isToLowerCase() {
            return toLowerCase;
        }
    }

    /***
     * Se a String for nula retorna "", em qualquer outro caso o valor não é alterado.
     * Útil especialmente pra acessibilidade.
     * @param str String a ser verificada e que receberá o valor de volta
     */
    public static String makeEmptyIfNull(@Nullable String str) {
        return str == null ? "" : str;
    }

    public static String removeLeftZeros(@NonNull String stringToRemoveZeros) {
        return stringToRemoveZeros.replaceFirst(REMOVE_LEADING_ZEROES_REGEX, "");
    }

    public static String replaceAccentuatedCharacters(String str) {
        return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    /***
     * Se a String for nula retorna "0", em qualquer outro caso o valor não é alterado.
     * @param string String a ser verificada e que receberá o valor de volta
     */
    public static String getDouble(String string) {
        return TextUtils.isEmpty(string) ? "0" : string.replace(".", "").replace(",", ".");
    }

    /**
     * Esse método exclui a ultima letra da String e insere reticências na mesma se ela possuir
     * mais caracteres que o valor passado por parâmentro
     * @param textToEllipsize          texto a ser incluído a reticências
     * @param ellipsizeAfterCharacters número de caracteres no qual será inserido a reticências depois
     * @return texto de saída
     */
    public static String ellipsizeText(String textToEllipsize, int ellipsizeAfterCharacters) {
        if (textToEllipsize.length() > ellipsizeAfterCharacters) {
            String textToShow = textToEllipsize.substring(0, ellipsizeAfterCharacters);
            return textToShow + SUSPENSION_POINTS;
        }
        return textToEllipsize;
    }

    /**
     * Formata um determinado e-mail com uma máscara que manterá apenas o caracter inicial e
     * final, tanto para o usuário quanto para o domínio, como os exemplos abaixo:
     * <ul>
     * <li>antonio@antonio.com - a*****o@a*****o.com</li>
     * <li>abcdef@gmail.com - a***ef@g***l.com</li>
     * <li>abc@abc.com.br - a*c@a*c.com.br</li>
     * <li>xy@xy.com - xy@xy.com</li>
     * </ul>
     * Os caracteres removidos sempre serão substituídos por caracteres "\u2022".
     *
     * @param rawEmail E-mail a receber a máscara. Caso o valor fornecido não for um e-mail válido,
     *                 o mesmo será ignorado e retornado sem modificações.
     * @return e-mail com a máscara aplicada.
     */
    public static String maskEmail(final String rawEmail) {

        if (TextUtils.isEmpty(rawEmail) || !EMAIL_ADDRESS.matcher(rawEmail).matches()) {
            return rawEmail;
        }

        StringBuilder result = new StringBuilder();

        String[] emailParts = rawEmail.split(AT_SYMBOL);
        String user = emailParts[0];
        String fullDomain = emailParts[1];

        int firstDot = fullDomain.indexOf('.');
        String domain = fullDomain.substring(0, firstDot);

        // usuário
        for (int i = 0; i < user.length(); i++) {
            if (i == 0 || i == user.length() - 1) {
                result.append(user.charAt(i));
            } else {
                result.append(HIDDEN_POINT_EMAIL);
            }
        }

        // arroba
        result.append(AT_SYMBOL);

        // parte do domínio com máscara
        for (int i = 0; i < domain.length(); i++) {
            if (i == 0 || i == domain.length() - 1) {
                result.append(domain.charAt(i));
            } else {
                result.append(HIDDEN_POINT_EMAIL);
            }
        }

        // demais partes do domínio
        result.append(fullDomain.substring(firstDot));

        return result.toString();
    }

    public static String maskEmailCiti(String emailCiti) {
        String[] emailParts = emailCiti.split("@");
        String user = emailParts[0];
        user = user.toLowerCase();
        String fullDomain = "@" + emailParts[1];
        fullDomain = fullDomain.toLowerCase();
        String maskedEmailUser = user.replaceAll("(?<=.{3}).(?=[^@]*?.{2})", "*");
        String maskedEmailDomain = fullDomain.replaceAll("(?<=.@{0}).(?=.[^@]*?.{1}.c)", "*");
        return maskedEmailUser + maskedEmailDomain;
    }

    public static String rightPadding(String str, int num, char paddingChar) {
        final Locale locale = new Locale("pt", "BR");
        final String paddingFormat = String.format(locale, "%%1$-%ds", num);
        return String.format(paddingFormat, str).replace(' ', paddingChar);
    }

    public static boolean equalStrings(@Nullable String a, @Nullable String b) {
        return (a == null && b == null) || (a != null && b != null && a.equals(b));
    }

    public static String getLastCharacters(String text, int characters) {
        if (text != null && text.length() >= characters) {
            return text.substring(text.length() - characters);
        }
        return text;
    }

    public static String removeNonNumeric(String value) {
        return !StringUtils.isEmpty(value) ? value.replaceAll("[^0-9]", "") : value;
    }
}