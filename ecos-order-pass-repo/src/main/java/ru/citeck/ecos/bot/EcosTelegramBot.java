package ru.citeck.ecos.bot;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;
import ru.citeck.ecos.model.IdocsModel;
import ru.citeck.ecos.model.OrderPassModel;
import ru.citeck.ecos.model.TelegramModel;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static ru.citeck.ecos.bot.ProcessStates.*;
import static ru.citeck.ecos.model.TelegramModel.TELEGRAM_USER_ID_PROP;


public class EcosTelegramBot extends TelegramLongPollingBot {

    @Autowired
    private NodeService nodeService;
    @Autowired
    private SearchService searchService;
    @Autowired
    private RetryingTransactionHelper retryingTransactionHelper;

    private String findUserByUserIdQuery = "TYPE:\"cm:person\" AND @tel\\:telegramUserId:%s";
    private String findUserByPhoneNumberQuery = "TYPE:\"cm:person\" AND @cm\\:telephone:%s";
    private String botToken;
    private static final String LOGTAG = "ECOS_BOT_LOGGER";
    private Map<UserChatKey, ProcessDTO> dataCacheMap = new HashMap<>();

    public EcosTelegramBot(String botToken) {
        this.botToken = botToken;
    }

    /**
     * Send message to user
     * @param messageText text of message
     * @param userId telegramUserId or chatId
     * */
    public void sendMessage(String messageText, String userId) throws TelegramApiException {
        execute(sendMessage(Long.valueOf(userId), null, messageText));
    }

    /**
     * Receives messages from bot
     *
     * */
    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                Message message = update.getMessage();
                if (message.hasText() || message.getContact() != null) {
                    AuthenticationUtil.runAsSystem((AuthenticationUtil.RunAsWork<Void>) () -> {
                        retryingTransactionHelper.doInTransaction((RetryingTransactionHelper.RetryingTransactionCallback<Void>) () -> {
                            handleIncomingMessage(message);
                            return null;
                        });
                        return null;
                    });

                }
            }
        } catch (Exception e) {
            BotLogger.error(LOGTAG, e);
        }
    }


    private SearchParameters getSearchParameters(String searchQuery) {
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setLimit(0);
        searchParameters.setLimitBy(LimitBy.UNLIMITED);
        searchParameters.setMaxItems(-1);
        searchParameters.setMaxPermissionChecks(Integer.MAX_VALUE);
        searchParameters.setMaxPermissionCheckTimeMillis(Integer.MAX_VALUE);
        searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        searchParameters.setQuery(searchQuery);
        return searchParameters;
    }

    private void handleIncomingMessage(Message message) throws TelegramApiException {
        UserChatKey userChatKey = new UserChatKey(message.getFrom().getId(), message.getChatId());
        ProcessDTO processDTO = dataCacheMap.get(userChatKey);
        NodeRef user = getUserByUserId(message.getFrom().getId());
        if (user == null) {
            user = tryUserAuthorised(message, userChatKey);
            processDTO = null;
        }
        if (user != null) {
            int state = STARTSTATE;
            if (processDTO != null) {
                state = processDTO.getState();
            } else {
                processDTO = new ProcessDTO();
                processDTO.setState(state);
                dataCacheMap.put(userChatKey, processDTO);
            }
            if (message.getText() != null && message.getText().equals("/order_pass")) {
                dataCacheMap.put(userChatKey, new ProcessDTO());
                dataCacheMap.get(userChatKey).setState(STARTSTATE);
                state = STARTSTATE;
            }
            if (!message.isUserMessage() && message.hasText()) {
                return;
            }
            SendMessage sendMessageRequest;
            if (message.getText() != null && message.getText().equals("/help")) {
                sendMessageRequest = sendHelpMessage(userChatKey);
            } else {
                switch (state) {
                    case STARTSTATE:
                        sendMessageRequest = sendStartProcessMessage(userChatKey, processDTO);
                        break;
                    case FIO_STATE:
                        String FIO = message.getText();
                        processDTO.getData().put("fio", FIO);
                        sendMessageRequest = sendDateRequest(userChatKey, processDTO);
                        break;
                    case DATE_STATE:
                        sendMessageRequest = sendDataReceivedMessage(userChatKey, processDTO, message);
                        processDTO.setAllDataReceived(true);
                        break;
                    default:
                        return;
                }
            }
            execute(sendMessageRequest);
            if (processDTO.getAllDataReceived() != null && processDTO.getAllDataReceived()) {
                createOrderPass(userChatKey, processDTO, user);
            }
        }
    }

    private NodeRef tryUserAuthorised(Message message, UserChatKey userChatKey) throws TelegramApiException {
        if (message.getContact() != null) {
            userChatKey.setPhone(message.getContact().getPhoneNumber());
            return authoriseUser(message);
        } else {
            SendMessage sendMessageRequest = sendMessageDefault(userChatKey);
            execute(sendMessageRequest);
            return null;
        }
    }

    private void createOrderPass(UserChatKey userChatKey, ProcessDTO processDTO, NodeRef user) {
        String orderPassDestination = "/app:company_home/st:sites/cm:order-pass/cm:documentLibrary/cm:order-passes";
        NodeRef rootNodeRef = null;
        ResultSet searchResults = searchService.query(
                StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                SearchService.LANGUAGE_XPATH, orderPassDestination);
        if (searchResults != null && searchResults.length() > 0) {
            rootNodeRef = searchResults.getNodeRef(0);
            Map<QName, Serializable> properties = new HashMap<>();
            properties.put(OrderPassModel.VISITOR_FULL_NAME_PROP, (Serializable) processDTO.getData().get("fio"));
            properties.put(OrderPassModel.VISITING_DATE_PROP, (Serializable) processDTO.getData().get("date"));
            properties.put(TelegramModel.IS_TELEGRAM_REQUEST_PROP, true);
            properties.put(IdocsModel.PROP_REGISTRATION_DATE, new Date());
            NodeRef orderPass = nodeService.createNode(rootNodeRef,
                    ContentModel.ASSOC_CONTAINS,
                    OrderPassModel.ORDER_PASS_TYPE,
                    OrderPassModel.ORDER_PASS_TYPE, properties).getChildRef();

            nodeService.createAssociation(orderPass, user, OrderPassModel.INITIATOR_ASSOC);
            dataCacheMap.remove(userChatKey);
        }
    }

    private NodeRef authoriseUser(Message message) throws TelegramApiException {
        String phoneNumber = message.getContact().getPhoneNumber();
        if (phoneNumber == null) {
            return null;
        }
        NodeRef user = null;
        String query = String.format(findUserByPhoneNumberQuery, phoneNumber);
        ResultSet resultSet = searchService.query(getSearchParameters(query));
        if (resultSet.getNodeRefs() != null && !resultSet.getNodeRefs().isEmpty()) {
            if (resultSet.getNodeRefs().size() > 1) {
                throw new IllegalStateException("Find more then one user with userId=" + phoneNumber);
            }
            user = resultSet.getNodeRefs().get(0);
            nodeService.setProperty(user, TELEGRAM_USER_ID_PROP, message.getContact().getUserID());
        }
        if (user == null) {
            execute(sendMessage(message.getChatId(), null, "По вашему телефону не найден пользователь в системе ECOS. Обратитесь к администратору."));
        }
        return user;
    }

    private NodeRef getUserByUserId(Integer id) {
        if (id == null) {
            return null;
        }
        NodeRef user = null;
        String query = String.format(findUserByUserIdQuery, id);
        ResultSet resultSet = searchService.query(getSearchParameters(query));
        if (resultSet.getNodeRefs() != null && !resultSet.getNodeRefs().isEmpty()) {
            if (resultSet.getNodeRefs().size() > 1) {
                throw new IllegalStateException("Find more then one user with userId=" + id);
            }
            user = resultSet.getNodeRefs().get(0);
        }

        return user;
    }

    private SendMessage sendHelpMessage(UserChatKey userChatKey) {
        return sendHelpMessage(userChatKey.getChatId(), "Отправьте /order_pass для создания нового пропуска.");
    }

    private SendMessage sendDataReceivedMessage(UserChatKey userChatKey, ProcessDTO processDTO, Message message) {
        String date = message.getText();
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        Date visitDate = null;
        try {
            visitDate = format.parse(date);
        } catch (ParseException e) {
            return sendMessage(userChatKey.getChatId(), null, "Введите дату предполагаемого посещения в формате ДД.ММ.ГГГГ");
        }
        if (visitDate != null) {
            processDTO.getData().put("date", visitDate);
        }
        processDTO.setState(DATA_RECEIVED);
        return sendMessage(userChatKey.getChatId(), null, "Данные введены успешно. Ожидайте согласования пропуска, информация поступит в чат");
    }

    private SendMessage sendDateRequest(UserChatKey userChatKey, ProcessDTO processDTO) {
        processDTO.setState(DATE_STATE);
        return sendMessage(userChatKey.getChatId(), null, "Введите дату предполагаемого посещения в формате ДД.ММ.ГГГГ");
    }

    private SendMessage sendStartProcessMessage(UserChatKey userChatKey, ProcessDTO processDTO) {
        ReplyKeyboardRemove keyboardRemove = new ReplyKeyboardRemove();
        keyboardRemove.setSelective(true);
        processDTO.setState(FIO_STATE);
        return sendMessage(userChatKey.getChatId(), keyboardRemove, "Вы успешно авторизировались. В данный момент можно заказать пропуск. Укажите ФИО посетителя.");
    }

    private SendMessage sendMessageDefault(UserChatKey userChatKey) {
        ReplyKeyboardMarkup replyKeyboardMarkup = getAuthoriseKeyboard();
        return sendMessage(userChatKey.getChatId(), replyKeyboardMarkup, "Для авторизации необходимо отправить свой контакт боту.");
    }

    private static SendMessage sendMessage(Long chatId, ReplyKeyboard replyKeyboardMarkup, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        if (replyKeyboardMarkup != null) {
            sendMessage.setReplyMarkup(replyKeyboardMarkup);
        }
        sendMessage.setText(message);
        return sendMessage;
    }

    private static SendMessage sendHelpMessage(Long chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(false);
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        return sendMessage;
    }

    private ReplyKeyboardMarkup getAuthoriseKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardButton button = new KeyboardButton("Авторизоваться");
        button.setRequestContact(true);
        keyboardFirstRow.add(button);
        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    @Override
    public String getBotUsername() {
        return "ECOS Telegram Bot";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

/**
 * Class - key for different users processes
 * */
    public class UserChatKey {
        private Integer userId;
        private Long chatId;
        private String phone;

        public UserChatKey(Integer userId, Long chatId) {
            this.userId = userId;
            this.chatId = chatId;
        }

        public Integer getUserId() {
            return userId;
        }

        public Long getChatId() {
            return chatId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        public void setChatId(Long chatId) {
            this.chatId = chatId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UserChatKey)) return false;

            UserChatKey that = (UserChatKey) o;

            if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
            return chatId != null ? chatId.equals(that.chatId) : that.chatId == null;
        }

        @Override
        public int hashCode() {
            int result = userId != null ? userId.hashCode() : 0;
            result = 31 * result + (chatId != null ? chatId.hashCode() : 0);
            return result;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    /**
     *Contains process data in cache
     *
     * */
    public class ProcessDTO {
        private int state;
        private Map<String, Object> data;
        private Boolean allDataReceived = false;

        public ProcessDTO() {
            state = UNAUTHORIZED;
            data = new HashMap<>();
        }

        public int getState() {
            return state;
        }

        public Map<String, Object> getData() {
            return data;
        }

        public void setState(int state) {
            this.state = state;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ProcessDTO)) return false;

            ProcessDTO that = (ProcessDTO) o;

            if (state != that.state) return false;
            return data.equals(that.data);
        }

        @Override
        public int hashCode() {
            int result = state;
            result = 31 * result + data.hashCode();
            return result;
        }

        public Boolean getAllDataReceived() {
            return allDataReceived;
        }

        public void setAllDataReceived(Boolean allDataReceived) {
            this.allDataReceived = allDataReceived;
        }
    }


}
