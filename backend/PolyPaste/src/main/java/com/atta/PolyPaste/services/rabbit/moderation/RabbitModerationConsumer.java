package com.atta.PolyPaste.services.rabbit.moderation;

import com.atta.PolyPaste.dto.ModerationPasteMessageDto;
import com.atta.PolyPaste.dto.PasteIndexDto;
import com.atta.PolyPaste.dto.PasteMessageDto;
import com.atta.PolyPaste.services.OpenSearchService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitModerationConsumer {

    private final ModerationService moderationService;
    private final RabbitModerationProducer producer;

    @RabbitListener(queues = "${app.rabbitmq.queue.input}")
    public void consume(PasteMessageDto message,
                        Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        log.info("Received message for moderation: ID [{}]", message.creatorId());

        try {
            String status = moderationService.moderate(message.content());

            ModerationPasteMessageDto result = new ModerationPasteMessageDto(
                    null,
                    message.shortUrl(),
                    message.content(),
                    message.creatorId(),
                    message.firstName(),
                    message.lastName(),
                    message.syntax(),
                    message.expirationTime(),
                    message.burnAfterRead(),
                    status,
                    message.visibility(),
                    message.password()
            );

            producer.sendModerationResult(result);

            channel.basicAck(tag, false);

        } catch (Exception e) {
            log.error("Error processing message ID [{}]: {}", message.creatorId(), e.getMessage());
            channel.basicNack(tag, false, true);
        }
    }
}