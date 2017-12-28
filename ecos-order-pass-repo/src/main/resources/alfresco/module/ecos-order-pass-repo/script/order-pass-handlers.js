
    function isFromTelegram() {
        return document.properties["tel:isTelegramRequest"];
    }

    function sendMessageToTelegram(message) {
        if (isFromTelegram()) {
            var telegramBot = services.get('ecosTelegramBot');
            telegramBot.sendMessage(message, document.assocs['op:initiator'][0].properties['tel:telegramUserId']);
        }
    }

