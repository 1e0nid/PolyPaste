package com.atta.PolyPaste.Services;

import com.atta.PolyPaste.Entitys.PasteEntity;
import com.atta.PolyPaste.Repository.PasteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PasteService {

    private static final Logger log = LoggerFactory.getLogger(PasteService.class);

    private final PasteRepository pasteRepository;

    public PasteService(PasteRepository pasteRepository) {
        this.pasteRepository = pasteRepository;
    }

    public void createPaste(PasteEntity pasteEntity) {
        log.info(String.format("PasteService: careate paste: %s", pasteEntity.toString()));
        pasteRepository.save(pasteEntity);
    }

}
