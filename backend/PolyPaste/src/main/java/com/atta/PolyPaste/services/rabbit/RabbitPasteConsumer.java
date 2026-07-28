package com.atta.PolyPaste.services.rabbit;

import com.atta.PolyPaste.dto.ModerationPasteMessageDto;
import com.atta.PolyPaste.services.PasteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RabbitPasteConsumer {

    private final PasteService pasteService;

    @RabbitListener(queues = "${app.rabbitmq.queue.output}", concurrency = "3-5")
    public void consumeModeratedPaste(ModerationPasteMessageDto message) {
        log.info("Поток [{}] принял сообщение для сохранения: {}",
                Thread.currentThread().getName(), message.shortUrl());
        try {
            pasteService.savePaste(message);
            log.info("Паста {} успешно сохранена в базу данных", message.shortUrl());
        } catch (Exception e) {
            log.error("Ошибка при сохранении пасты {}: {}", message.shortUrl(), e.getMessage());
        }
    }
}