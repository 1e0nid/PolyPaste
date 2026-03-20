package com.atta.PolyPaste.Services;

import com.atta.PolyPaste.DTO.Paste;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQProducer {
    @Value("${rabbirmq.exchange.name}")
    private String exchange;

    @Value("${rabbirmq.routing.key}")
    private String routingKey;

    private final RabbitTemplate rabbitTemplate;

    private static final Logger log = LoggerFactory.getLogger(RabbitMQProducer.class);

    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(Paste message){
        log.info(String.format("RabbitMQProducer: message paste: %s", message.toString()));
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
