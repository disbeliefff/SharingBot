package org.myprojects.service.Impl;

import org.myprojects.dao.RawDataDao;
import org.myprojects.entity.RawData;
import org.myprojects.service.MainService;
import org.myprojects.service.ProducerService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class MainServiceImpl implements MainService {
    private final RawDataDao rawDataDao;
    private final ProducerService producerService;

    public MainServiceImpl(RawDataDao rawDataDao, ProducerService producerService) {
        this.rawDataDao = rawDataDao;
        this.producerService = producerService;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);

        var message = update.getMessage();
        var sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("Hello from NODE");
        producerService.produceAnswer(sendMessage);
    }

    private void saveRawData(Update update) {
        var rawData = RawData.builder()
                .event(update)
                .build();

        rawDataDao.save(rawData);
    }
}
