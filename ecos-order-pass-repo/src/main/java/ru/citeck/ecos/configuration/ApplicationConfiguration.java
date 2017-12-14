package ru.citeck.ecos.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import ru.citeck.ecos.bot.EcosTelegramBot;

/**
 * Java spring configuration
 * */
@Configuration
public class ApplicationConfiguration {

    @Value("${telegram.bot.token}")
    String botToken;
    @Value("${telegram.bot.enabled}")
    Boolean botEnabled;

    @Bean(name="botsApi")
    public  TelegramBotsApi telegramBotsApi() {
        ApiContextInitializer.init();
        return new TelegramBotsApi();
    }

    @Bean(name="ecosTelegramBot")
    public EcosTelegramBot ecosTelegramBot(TelegramBotsApi telegramBotsApi) throws TelegramApiRequestException {
        EcosTelegramBot ecosTelegramBot = new EcosTelegramBot(botToken);
        if (botEnabled) {
            telegramBotsApi.registerBot(ecosTelegramBot);
        }
        return ecosTelegramBot;
    }


}
