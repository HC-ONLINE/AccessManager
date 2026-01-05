package com.hconline.accessmanager.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.hconline.accessmanager.security.NominaUserDetails;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(@AuthenticationPrincipal NominaUserDetails userDetails, Model model) {
        if (userDetails != null) {
            model.addAttribute("usuario", userDetails.getUsuario());
        }
        return "index";
    }
}
