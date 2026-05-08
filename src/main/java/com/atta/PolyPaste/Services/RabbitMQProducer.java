package com.atta.PolyPaste.Services;

import com.atta.PolyPaste.DTO.ModerationResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQProducer {

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key.output}")
    private String routingKey;

    private final RabbitTemplate rabbitTemplate;

    public void sendModerationResult(ModerationResultDTO result) {
        log.info("Sending moderation result for ID [{}]: {}", result.getId(), result.getStatus());
        rabbitTemplate.convertAndSend(exchange, routingKey, result);
    }
}