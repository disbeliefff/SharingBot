package org.myprojects.service.Impl;

import org.junit.jupiter.api.Test;
import org.myprojects.dao.RawDataDao;
import org.myprojects.entity.RawData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashSet;

@SpringBootTest
class MainServiceImplTest {

    @Autowired
    private RawDataDao rawDataDao;

    @Test
    public void testSaveRawData() {
        var update = new Update();
        var message = new Message();

        message.setText("test");
        update.setMessage(message);

        var rawData = RawData.builder()
                .event(update)
                .build();
        var testData = new HashSet<>();

        testData.add(rawData);
        rawDataDao.save(rawData);

        Assert.isTrue(testData.contains(rawData), "Entity not found in the set");
    }

}