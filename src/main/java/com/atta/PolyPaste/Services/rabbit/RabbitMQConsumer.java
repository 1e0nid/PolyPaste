package com.atta.PolyPaste.services.rabbit;

import com.atta.PolyPaste.dto.Paste;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQConsumer {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQConsumer.class);

    @RabbitListener(queues = {"${rabbitmq.queue.name}"})
    public void consume(Paste message){
        log.info(String.format("RabbitMQConsumer: take message: %s", message.toString()));
    }
}
