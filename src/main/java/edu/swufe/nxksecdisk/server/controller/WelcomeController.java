package edu.swufe.nxksecdisk.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Administrator
 */
@Controller
public class WelcomeController {

    @RequestMapping({"/"})
    public String home() {
        return "redirect:/home.html";
    }
}
