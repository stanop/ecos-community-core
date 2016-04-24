package ru.citeck.ecos.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Dolmatoff on 22.04.2016.
 */
public class ConvertAmountInWords {
    private static final String[][] ONE = {
            {"", "один", "два", "три", "четыре", "пять", "шесть", "семь", "восемь", "девять"},
            {"", "одна", "две", "три", "четыре", "пять", "шесть", "семь", "восемь", "девять"},
    };
    private static final String[] THOUSAND = {"", "сто", "двести", "триста", "четыреста", "пятьсот", "шестьсот", "семьсот", "восемьсот", "девятьсот"};
    private static final String[] TEN = {"", "десять", "одиннадцать", "двенадцать", "тринадцать", "четырнадцать", "пятнадцать", "шестнадцать", "семнадцать", "восемнадцать", "девятнадцать", "двадцать"};
    private static final String[] DECADE = {"", "десять", "двадцать", "тридцать", "сорок", "пятьдесят", "шестьдесят", "семьдесят", "восемьдесят", "девяносто"};

    private static String intact1, intact2, intact3;
    private static String fractional1, fractional2, fractional3;


    public static String convert(double i, String currency) {

        initializationCurrency(currency);

        String[][] DECLINATION = new String[][]{
                {fractional1, fractional2, fractional3, "1"},
                {intact1, intact2, intact3, "0"},
                {"тысяча", "тысячи", "тысяч", "1"},
                {"миллион", "миллиона", "миллионов", "0"},
                {"миллиард", "миллиарда", "миллиардов", "0"},
                {"триллион", "триллиона", "триллионов", "0"},
                //you can add more...
        };

        String s = String.valueOf(i);
        if (!s.contains(".")) {
            s += ".0";
        }

        BigDecimal amount = new BigDecimal(s);
        ArrayList<Long> segments = new ArrayList<>();

        // получаем отдельно рубли и копейки
        long total = amount.longValue();
        String[] divided = amount.toString().split("\\.");
        long fraction = Long.valueOf(divided[1]);
        if (!divided[1].substring(0, 1).equals("0")) {// начинается не с нуля
            if (fraction < 10)
                fraction *= 10;
        }
        String fractions = String.valueOf(fraction);
        if (fractions.length() == 1)
            fractions = "0" + fractions;
        long totalSegment = total;
        // Разбиватель суммы на сегменты по 3 цифры с конца
        while (totalSegment > 999) {
            long seg = totalSegment / 1000;
            segments.add(totalSegment - (seg * 1000));
            totalSegment = seg;
        }
        segments.add(totalSegment);
        Collections.reverse(segments);
        // Анализируем сегменты
        String result = "";
        if (total == 0) {// если Ноль
            result = "ноль " + getDeclination(0, DECLINATION[1][0], DECLINATION[1][1], DECLINATION[1][2]);
            return result + " " + fraction + " " + getDeclination(fraction, DECLINATION[0][0], DECLINATION[0][1], DECLINATION[0][2]);
        }
        // Больше нуля
        int amt = segments.size();
        for (Long segment : segments) {// перебираем сегменты
            int kind = Integer.valueOf(DECLINATION[amt][3]);// определяем род
            int currentSegment = Integer.valueOf(segment.toString());// текущий сегмент
            if (currentSegment == 0 && amt > 1) {// если сегмент ==0 И не последний уровень(там Units)
                amt--;
                continue;
            }
            String stringNumber = String.valueOf(currentSegment); // число в строку
            // нормализация
            if (stringNumber.length() == 1) stringNumber = "00" + stringNumber;// два нулика в префикс?
            if (stringNumber.length() == 2) stringNumber = "0" + stringNumber; // или лучше один?
            // получаем циферки для анализа
            int number1 = Integer.valueOf(stringNumber.substring(0, 1)); //первая цифра
            int number2 = Integer.valueOf(stringNumber.substring(1, 2)); //вторая
            int number3 = Integer.valueOf(stringNumber.substring(2, 3)); //третья
            int number23 = Integer.valueOf(stringNumber.substring(1, 3)); //вторая и третья
            // Супер-нано-анализатор циферок
            if (currentSegment > 99) result += THOUSAND[number1] + " "; // Сотни
            if (number23 > 20) {// >20
                result += DECADE[number2] + " ";
                result += ONE[kind][number3] + " ";
            } else { // <=20
                if (number23 > 9) result += TEN[number23 - 9] + " "; // 10-20
                else result += ONE[kind][number3] + " "; // 0-9
            }
            // Единицы измерения (рубли...)
            result += getDeclination(currentSegment, DECLINATION[amt][0], DECLINATION[amt][1], DECLINATION[amt][2]) + " ";
            amt--;
        }
        // Копейки в цифровом виде

        result = result + "" + fractions + " " + getDeclination(fraction, DECLINATION[0][0], DECLINATION[0][1], DECLINATION[0][2]);
        result = result.replaceAll(" {2,}", " ");

        return result;
    }

    private static void initializationCurrency(String currency){

        switch (currency) {
            case "USD": {
                fractional1 = "центр";
                fractional2 = "цента";
                fractional3 = "центов";

                intact1 = "доллар США";
                intact2 = "доллара США";
                intact3 = "долларов США";
                break;
            }
            case "RUB": {
                initializationRub();
                break;
            }
            case "EUR": {
                fractional1 = "цент";
                fractional2 = "цента";
                fractional3 = "центов";

                intact1 = "евро";
                intact2 = "евро";
                intact3 = "евро";
                break;
            }
            default: {
                initializationRub();
                break;
            }
        }
    }

    private static void initializationRub() {
        fractional1 = "копейка";
        fractional2 = "копейки";
        fractional3 = "копеек";

        intact1 = "рубль";
        intact2 = "рубля";
        intact3 = "рублей";
    }


    private static String getDeclination(long n, String form1, String from2, String form5) {
        n = Math.abs(n) % 100;
        long n1 = n % 10;
        if (n > 10 && n < 20) return form5;
        if (n1 > 1 && n1 < 5) return from2;
        if (n1 == 1) return form1;
        return form5;
    }
}
