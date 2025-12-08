package com.app.planetaconsciente.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        return handleWelcomeMessage(session, model, "home");
    }

    @GetMapping("/inicio")
    public String inicio(HttpSession session, Model model) {
        return handleWelcomeMessage(session, model, "inicio");
    }

    private String handleWelcomeMessage(HttpSession session, Model model, String viewName) {
        Boolean welcomeShown = (Boolean) session.getAttribute("welcomeShown");

        if (welcomeShown == null || !welcomeShown) {
            model.addAttribute("showWelcome", true);
            session.setAttribute("welcomeShown", true);
        } else {
            model.addAttribute("showWelcome", false);
        }

        return viewName;
    }
}
