package com.atta.PolyPaste.Services;

import com.atta.PolyPaste.DTO.Paste;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQConsumer {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQConsumer.class);

    @RabbitListener(queues = {"${rabbirmq.queue.name}"})
    public void consume(Paste message){
        log.info(String.format("RabbitMQConsumer: take message: %s", message.toString()));
    }
}
