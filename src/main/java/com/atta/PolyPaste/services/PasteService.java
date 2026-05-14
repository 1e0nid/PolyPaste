package com.atta.PolyPaste.services;

import com.atta.PolyPaste.controllers.PasteController;
import com.atta.PolyPaste.dto.PasteMessageDto;
import com.atta.PolyPaste.dto.RequestPasteDto;
import com.atta.PolyPaste.dto.UserPrincipalDto;
import com.atta.PolyPaste.services.rabbit.RabbitPasteProducer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasteService {

    private final UrlService urlService;
    private final RabbitPasteProducer rabbitPasteProducer;
    private static final Logger log = LoggerFactory.getLogger(PasteService.class);

    public String createPaste(RequestPasteDto request, UserPrincipalDto user) {
        String shortUrl = urlService.getFreeUrl();

        PasteMessageDto message = new PasteMessageDto(
                null,
                shortUrl,
                request.content(),
                user.id(),
                user.firstName(),
                user.lastName(),
                request.syntax(),
                request.expirationTime(),
                request.burnAfterRead()
        );

        log.info("create paste: " + message);

        rabbitPasteProducer.sendToQueue(message);

        return shortUrl;
    }
}
