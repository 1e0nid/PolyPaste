package com.atta.PolyPaste.services.rabbit;

import com.atta.PolyPaste.controllers.PasteController;
import com.atta.PolyPaste.dto.PasteMessageDto;
import com.atta.PolyPaste.dto.RequestPasteDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitPasteProducer {
    private static final Logger log = LoggerFactory.getLogger(RabbitPasteProducer.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key.input}")
    private String routingKey;

    public void sendToQueue(PasteMessageDto message) {
        log.info("paste send to rabbit");
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
