package com.atta.PolyPaste.controllers;

import com.atta.PolyPaste.dto.Paste;
import com.atta.PolyPaste.mapper.PasteMapper;
import com.atta.PolyPaste.entitys.PasteEntity;
import com.atta.PolyPaste.services.PasteService;
import com.atta.PolyPaste.services.rabbit.RabbitMQProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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

	@GetMapping("/me")
	public ResponseEntity<?> getCurrentUser() {
		var auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
			return ResponseEntity.status(401).body("Вы не авторизованы");
		}

		return ResponseEntity.ok("Ваш ID в контексте: " + auth);
	}

}