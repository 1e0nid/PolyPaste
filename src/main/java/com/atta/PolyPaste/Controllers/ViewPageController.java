package com.atta.PolyPaste.Controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ViewPageController {

    private static final Logger log = LoggerFactory.getLogger(ViewPageController.class);

    @GetMapping("/")
    public String greeting(Model model) {
        return "home";
    }
}