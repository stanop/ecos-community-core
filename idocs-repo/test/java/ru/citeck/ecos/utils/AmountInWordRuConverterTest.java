package ru.citeck.ecos.utils;

import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.utils.converter.amount.AmountInWordConverter;
import ru.citeck.ecos.utils.converter.amount.AmountInWordConverterFactory;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * @author Roman.Makarskiy on 19.06.2016.
 */
public class AmountInWordRuConverterTest {

    private AmountInWordConverter ruConverter;

    @Before
    public void initializationConverter() {
        Locale locale = new Locale("ru", "");
        I18NUtil.setLocale(locale);
        ruConverter = new AmountInWordConverterFactory().getConverter();
    }

    @Test
    public void convertRubTest() {
        assertEquals("Один миллион двести четыре тысячи девятьсот двадцать четыре рубля 67 копеек", ruConverter.convert(1204924.67, "RUB"));
        assertEquals("Двадцать пять тысяч восемьсот девяносто два рубля 25 копеек", ruConverter.convert(25892.25, "RUB"));
        assertEquals("Два триллиона двести пятьдесят восемь миллиардов четыреста сорок один миллион пятьсот пятьдесят восемь тысяч четыреста пятьдесят шесть рублей 52 копейки", ruConverter.convert(2258441558456.51997, "RUB"));
        assertEquals("Три рубля 21 копейка", ruConverter.convert(3.21, "RUB"));
        assertEquals("Десять рублей 00 копеек", ruConverter.convert(10, "RUB"));
        assertEquals("Тридцать один рубль 80 копеек", ruConverter.convert(31.8, "RUB"));

    }

    @Test
    public void convertExponentTest() {
        assertEquals("Девять миллиардов сто восемьдесят миллионов рублей 00 копеек", ruConverter.convert(9.18E+09, "RUB"));
        assertEquals("Один миллион сорок одна тысяча двести девяносто восемь рублей 30 копеек", ruConverter.convert(1.0412983E+06, "RUB"));
    }

    @Test
    public void convertEurTest() {
        assertEquals("Триста двадцать два евро 07 центов", ruConverter.convert(322.07, "EUR"));
        assertEquals("Девятнадцать тысяч семьсот тринадцать евро 21 цент", ruConverter.convert(19713.21, "EUR"));
        assertEquals("Триста сорок девять тысяч четыреста евро 02 цента", ruConverter.convert(349400.02, "EUR"));
    }

    @Test
    public void convertUsdTest() {
        assertEquals("Двести девять долларов США 00 центов", ruConverter.convert(209.00, "USD"));
        assertEquals("Тридцать три доллара США 70 центов", ruConverter.convert(33.7, "USD"));
        assertEquals("Десять тысяч долларов США 00 центов", ruConverter.convert(10000, "USD"));
    }

    @Test
    public void convertJpyTest() {
        assertEquals("Одна тысяча сто девяносто девять японских иен 00 сен", ruConverter.convert(1199.00, "JPY"));
        assertEquals("Одна японская иена 00 сен", ruConverter.convert(1, "JPY"));
        assertEquals("Две японских иены 02 сен", ruConverter.convert(2.02, "JPY"));
        assertEquals("Девять японских иен 50 сен", ruConverter.convert(9.50, "JPY"));
    }

    @Test
    public void convertUahTest() {
        assertEquals("Три гривны 21 копейка", ruConverter.convert(3.21, "UAH"));
        assertEquals("Одна гривна 00 копеек", ruConverter.convert(1, "UAH"));
        assertEquals("Две гривны 02 копейки", ruConverter.convert(2.02, "UAH"));
        assertEquals("Десять гривен 00 копеек", ruConverter.convert(10, "UAH"));
    }

    @Test
    public void convertGbpTest() {
        assertEquals("Одна тысяча сто девяносто девять фунтов стерлингов 00 пенсов", ruConverter.convert(1199.00, "GBP"));
        assertEquals("Двадцать два фунта стерлингов 50 пенсов", ruConverter.convert(22.50, "GBP"));
    }

    @Test
    public void convertByrTest() {
        assertEquals("Триста двадцать два белорусских рубля 07 копеек", ruConverter.convert(322.07, "BYR"));
        assertEquals("Девятнадцать тысяч семьсот тринадцать белорусских рублей 21 копейка", ruConverter.convert(19713.21, "BYR"));
        assertEquals("Триста сорок девять тысяч четыреста белорусских рублей 02 копейки", ruConverter.convert(349400.02, "BYR"));
    }
}
