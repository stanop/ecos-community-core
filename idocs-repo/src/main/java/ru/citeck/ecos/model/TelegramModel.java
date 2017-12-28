package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

public interface TelegramModel {
    String NAMESPACE = "http://www.citeck.ru/model/telegram/common/1.0";

    QName IS_TELEGRAM_REQUEST_ASPECT = QName.createQName(NAMESPACE, "isTelegramRequest");
    QName HAS_TELEGRAM_USER_ID_ASPECT = QName.createQName(NAMESPACE, "hasTelegramUserId");

    QName IS_TELEGRAM_REQUEST_PROP = QName.createQName(NAMESPACE, "isTelegramRequest");
    QName TELEGRAM_USER_ID_PROP = QName.createQName(NAMESPACE, "telegramUserId");
}
