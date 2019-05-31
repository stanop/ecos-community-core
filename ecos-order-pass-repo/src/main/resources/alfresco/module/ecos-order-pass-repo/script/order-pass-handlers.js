
    function onProcessStart() {
        if (document.properties['idocs:registrationNumber'] == null || document.properties['idocs:registrationNumber'] == ' ') {
            var numberTemplate = search.findNode("workspace://SpacesStore/idocs-order-pass-number-template");
            var registrationNumber = enumeration.getNumber(numberTemplate, document);
            document.properties['idocs:registrationNumber'] = registrationNumber;
        }
        document.save();
    }

    function isFromTelegram() {
        return document.properties["tel:isTelegramRequest"];
    }

    function sendMessageToTelegram(message) {
        if (isFromTelegram()) {
            var telegramBot = services.get('ecosTelegramBot');
            telegramBot.sendMessage(message, document.assocs['op:initiator'][0].properties['tel:telegramUserId']);
        }
    }

