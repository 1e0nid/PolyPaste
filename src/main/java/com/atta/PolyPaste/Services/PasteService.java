package com.atta.PolyPaste.services;

import com.atta.PolyPaste.entitys.PasteEntity;
import com.atta.PolyPaste.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PasteService {

    private static final Logger log = LoggerFactory.getLogger(PasteService.class);

    private final UserRepository userRepository;

    public PasteService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void createPaste(PasteEntity pasteEntity) {
        log.info(String.format("PasteService: careate paste: %s", pasteEntity.toString()));
//        userRepository.save(pasteEntity);
    }

}
