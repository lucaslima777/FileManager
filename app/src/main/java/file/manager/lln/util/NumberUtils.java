package file.manager.lln.util;

import java.text.NumberFormat;

public class NumberUtils {

    // Precisão de comparação dos floats
    private static final float PRECISION_LEVEL = 0.001f;

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();

    private NumberUtils() {

    }

    public static boolean floatsAreEquals(float number1, float number2) {
        return Math.abs(number1 - number2) < PRECISION_LEVEL;
    }

    public static boolean doublesAreEquals(double number1, double number2) {
        return Math.abs(number1 - number2) < PRECISION_LEVEL;
    }

    public static String format(float value, int i) {

        NUMBER_FORMAT.setMinimumIntegerDigits(i);
        return NUMBER_FORMAT.format(value);

    }

    public static double getMaxPositiveValue(double... values) {
        double max = 0;
        for (double value : values) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    public static boolean isPair(int number) {
        return number % 2 == 0 ;
    }

    /**
     * Método para transformar o valor em negativo se for de saída ou débito caso o mesmo vanha positivo na API.
     *
     * @param value a ser transformado para negativo
     * @return value valor negativo
     */
    public static double configureValueAsNegative(Double value) {
        if (Double.compare(value, 0) > 0) {
            return -value;
        }
        return value;
    }

    public static String formatAsCurrency(long value) {
        return NumberFormat.getCurrencyInstance().format(value);
    }
}
