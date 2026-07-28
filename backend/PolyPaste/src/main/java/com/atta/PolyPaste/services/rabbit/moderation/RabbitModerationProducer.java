package com.atta.PolyPaste.services.rabbit.moderation;

import com.atta.PolyPaste.dto.ModerationPasteMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitModerationProducer {

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key.output}")
    private String routingKey;

    private final RabbitTemplate rabbitTemplate;

    public void sendModerationResult(ModerationPasteMessageDto result) {
        log.info("Sending moderation result for ID [{}]: {}", result.id(), result.status());
        rabbitTemplate.convertAndSend(exchange, routingKey, result);
    }
}