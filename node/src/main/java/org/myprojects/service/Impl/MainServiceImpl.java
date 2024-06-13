package org.myprojects.service.Impl;

import lombok.extern.log4j.Log4j;
import org.myprojects.dao.AppUserDao;
import org.myprojects.dao.RawDataDao;
import org.myprojects.entity.AppUser;
import org.myprojects.entity.RawData;
import org.myprojects.service.MainService;
import org.myprojects.service.ProducerService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.myprojects.enums.UserState.BASIC_STATE;
import static org.myprojects.enums.UserState.WAIT_FOR_EMAIL_STATE;
import static org.myprojects.service.enums.ServiceCommands.*;

@Service
@Log4j
public class MainServiceImpl implements MainService {
    private final RawDataDao rawDataDao;
    private final ProducerService producerService;
    private final AppUserDao appUser;
    private final AppUserDao appUserDao;

    public MainServiceImpl(RawDataDao rawDataDao, ProducerService producerService, AppUserDao appUser, AppUserDao appUserDao) {
        this.rawDataDao = rawDataDao;
        this.producerService = producerService;
        this.appUser = appUser;
        this.appUserDao = appUserDao;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);

        var appUser = findOrSaveAppUser(update);
        var userState = appUser.getState();
        var text = update.getMessage().getText();
        var output = "";

        if (CANCEL.equals(text)) {
            output = cancelProcess(appUser);
        } else if (BASIC_STATE.equals(userState)) {
            output = processServiceCommand(appUser, text);
        } else if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
            // TODO: add email processing logic
        } else {
            log.error("Unknown user state: " + userState);
            output = "Unknown error... Enter /cancel and try again";
        }

        var chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);

    }

    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);

        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();

        if (isNotAllowToSendContent(chatId, appUser))     {
            return;
        }

        //TODO: add doc saving logic
        var answer = "The document has been successfully uploaded. Link to download: http://test.com/get-doc/1";
        sendAnswer(answer, chatId);
    }


    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);

        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();

        if (isNotAllowToSendContent(chatId, appUser))     {
            return;
        }

        //TODO: add photo saving logic
        var answer = "The document has been successfully uploaded. Link to download: http://test.com/get-photo/1";
        sendAnswer(answer, chatId);

    }

    private boolean isNotAllowToSendContent(Long chatId, AppUser appUser) {
        var userState = appUser.getState();

        if (!appUser.getIsActive()) {
            var error = "Register or activate an account to upload or download content";
            sendAnswer(error, chatId);
            return true;
        } else if (!BASIC_STATE.equals(userState)) {
            var error = "Cancel the current command with /cancel to upload the file";
            sendAnswer(error, chatId);
            return true;
        }
        return false;
    }

    private void sendAnswer(String output, Long chatId) {
        var sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.produceAnswer(sendMessage);
    }

    private String processServiceCommand(AppUser appUser, String cmd) {
        if (REGISTRATION.equals(cmd)) {
            //TODO: add registration
            return "Temporarily unavailable";
        } else if (HELP.equals(cmd)) {
            return help();
        } else if (START.equals(cmd)) {
            return "Greetings! Type /help to view the list of available commands";
        } else {
            return "Unknown command... Type /help to view the list of available commands";
        }
    }


    private String help() {
        return "List of available commands:\n"
                + "/cancel - cancel the current command:\n"
                + "/registration - registration of new user.";
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDao.save(appUser);

        return "Command canceled";
    }

    private AppUser findOrSaveAppUser(Update update) {
        var telegramUser = update.getMessage().getFrom();
        var persistentAppUser = appUser.findAppUserByTelegramUserId(telegramUser.getId());

        if (persistentAppUser == null) {
            var transiendAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .username(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .isActive(true)
                    .state(BASIC_STATE)
                    .build();

            return appUserDao.save(transiendAppUser);
        }
        return persistentAppUser;
    }

    private void saveRawData(Update update) {
        var rawData = RawData.builder()
                .event(update)
                .build();

        rawDataDao.save(rawData);
    }
}
