package com.atta.PolyPaste;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PolyPasteApplication {

	public static void main(String[] args) {
		SpringApplication.run(PolyPasteApplication.class, args);
	}

}
