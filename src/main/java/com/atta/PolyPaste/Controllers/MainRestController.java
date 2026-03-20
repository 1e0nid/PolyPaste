package com.atta.PolyPaste.Controllers;

import com.atta.PolyPaste.DTO.Paste;
import com.atta.PolyPaste.Mapper.PasteMapper;
import com.atta.PolyPaste.Entitys.PasteEntity;
import com.atta.PolyPaste.Services.PasteService;
import com.atta.PolyPaste.Services.RabbitMQProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class MainRestController {

	private static final Logger log = LoggerFactory.getLogger(MainRestController.class);

	private RabbitMQProducer producer;

	private PasteService pasteService;

	private PasteMapper pasteMapper;

	public MainRestController(RabbitMQProducer producer, PasteService pasteService, PasteMapper pasteMapper) {
		this.producer = producer;
        this.pasteService = pasteService;
		this.pasteMapper = pasteMapper;
    }

	@PostMapping("/")
	public ResponseEntity<String> getPaste(@RequestBody PasteEntity inputPaste) {
		log.info("MainRestController: called getPaste");
		pasteService.createPaste(inputPaste);
		Paste newPaste = pasteMapper.toPaste(inputPaste);
		producer.sendMessage(newPaste);
		return ResponseEntity.status(HttpStatus.OK)
				.body(inputPaste.toString());
	}

}