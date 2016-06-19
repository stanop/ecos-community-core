package ru.citeck.ecos.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Roman.Makarskiy on 19.06.2016.
 */
public class ConvertAmountInWordsTest {

    @Test
    public void convertTest() {
        assertEquals("Один миллион двести четыре тысячи девятьсот двадцать четыре рубля 67 копеек", ConvertAmountInWords.convert(1204924.67, "RUB"));
        assertEquals("Двадцать пять тысяч восемьсот девяносто два рубля 25 копеек", ConvertAmountInWords.convert(25892.25, "RUB"));
        assertEquals("Два триллиона двести пятьдесят восемь миллиардов четыреста сорок один миллион пятьсот пятьдесят восемь тысяч четыреста пятьдесят шесть рублей 52 копейки", ConvertAmountInWords.convert(2258441558456.51997, "RUB"));
        assertEquals("Три рубля 21 копейка", ConvertAmountInWords.convert(3.21, "RUB"));
        assertEquals("Десять рублей 00 копеек", ConvertAmountInWords.convert(10, "RUB"));
        assertEquals("Тридцать один рубль 80 копеек", ConvertAmountInWords.convert(31.8, "RUB"));

    }

    @Test
    public void convertExponentTest() {
        assertEquals("Девять миллиардов сто восемьдесят миллионов рублей 00 копеек", ConvertAmountInWords.convert(9.18E+09, "RUB"));
        assertEquals("Один миллион сорок одна тысяча двести девяносто восемь рублей 30 копеек", ConvertAmountInWords.convert(1.0412983E+06, "RUB"));
    }

    @Test
    public void convertEurTest() {
        assertEquals("Триста двадцать два евро 07 центов", ConvertAmountInWords.convert(322.07, "EUR"));
        assertEquals("Девятнадцать тысяч семьсот тринадцать евро 21 цент", ConvertAmountInWords.convert(19713.21, "EUR"));
        assertEquals("Триста сорок девять тысяч четыреста евро 02 цента", ConvertAmountInWords.convert(349400.02, "EUR"));
    }

    @Test
    public void convertUsdTest() {
        assertEquals("Двести девять долларов США 00 центов", ConvertAmountInWords.convert(209.00, "USD"));
        assertEquals("Тридцать три доллара США 70 центов", ConvertAmountInWords.convert(33.7, "USD"));
        assertEquals("Десять тысяч долларов США 00 центов", ConvertAmountInWords.convert(10000, "USD"));
    }
}
