package com.ser421.eliza.controller;

import com.ser421.eliza.service.ElizaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ElizaController {

    private final ElizaService elizaService;

    public ElizaController(ElizaService elizaService) {
        this.elizaService = elizaService;
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        if (session.getAttribute("userName") == null) {
            return "index";
        }
        model.addAttribute("name", session.getAttribute("userName"));
        model.addAttribute("conversation", elizaService.getConversation(session));
        return "index";
    }

    @PostMapping("/start")
    public String start(@RequestParam String name, HttpSession session) {
        session.setAttribute("userName", name.trim());
        return "redirect:/";
    }

    @PostMapping("/talk")
    public String talk(@RequestParam String message, HttpSession session, Model model) {
        String name = (String) session.getAttribute("userName");
        if (name == null) return "redirect:/";

        if ("/clear".equalsIgnoreCase(message.trim())) {
            elizaService.clearSession(session);
            return "redirect:/";
        }

        String response = elizaService.getResponse(message, name, session);
        model.addAttribute("name", name);
        model.addAttribute("conversation", elizaService.getConversation(session));
        return "index";
    }
}