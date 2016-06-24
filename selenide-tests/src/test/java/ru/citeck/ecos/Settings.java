package ru.citeck.ecos;

public class Settings {

    //private static String baseURL = "http://ecos-community.citeck.ru/share/page";
    private static String baseURL = "http://37.230.155.222:4580/share/page";
    //private static String baseURL = "http://ecos-community-4.citeck.ru/share/page/";
    //private static String baseURL = "http://spitamen.citeck.ru/share/page";

    private static String login = "admin";
    private static String password = "tiny room drive";
    //private static String password = "Qq123456";

    public static String getBaseURL()
    {
        return baseURL;
    }
    public  static String getLogin()
    {
        return login;
    }
    public  static String getPassword()
    {
        return password;
    }
}
