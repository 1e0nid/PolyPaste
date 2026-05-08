package com.atta.PolyPaste.Services;

import com.atta.PolyPaste.DTO.ModerationResultDTO;
import com.atta.PolyPaste.DTO.PasteMessageDTO;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQConsumer {

    private final ModerationService moderationService;
    private final RabbitMQProducer producer;

    @RabbitListener(queues = "${app.rabbitmq.queue.input}")
    public void consume(PasteMessageDTO message,
                        Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        log.info("Received message for moderation: ID [{}]", message.getId());

        try {
            String status = moderationService.moderate(message.getContent());

            ModerationResultDTO result = new ModerationResultDTO(
                    message.getId(),
                    message.getContent(),
                    status
            );
            producer.sendModerationResult(result);

            channel.basicAck(tag, false);

        } catch (Exception e) {
            log.error("Error processing message ID [{}]: {}", message.getId(), e.getMessage());
            channel.basicNack(tag, false, true);
        }
    }
}