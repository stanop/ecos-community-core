package ru.citeck.ecos;

public class Settings {

    private static String baseURL = System.getProperty("baseUrl");
    private static String login = System.getProperty("login");
    private static String password = System.getProperty("pass");

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
