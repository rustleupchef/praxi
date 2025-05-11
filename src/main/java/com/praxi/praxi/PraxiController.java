package com.praxi.praxi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.password4j.Password;

import jakarta.servlet.http.HttpSession;


@Controller
public class PraxiController {
    @GetMapping("/")
    public String index(HttpSession session) {
        if (session.getAttribute("verified") == null) {
            return "redirect:/login";
        }
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home(HttpSession session) {
        if (session.getAttribute("verified") == null) {
            return "redirect:/login";
        }
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    @ResponseBody public Message loginPost(HttpSession session, @RequestParam String password) throws IOException {
        String correctPassword = getPassword();
        if (correctPassword.equals("")) {
            FileWriter writer = new FileWriter("password");
            writer.write(Password.hash(password).withBcrypt().getResult());
            writer.close();
            session.setAttribute("verified", true);
            return new Message("success", "success");
        }

        if (Password.check(password, correctPassword).withBcrypt()) {
            session.setAttribute("verified", true);
            return new Message("success", "success");
        } else {
            return new Message("Invalid password!", "error");
        }

    }

    @PostMapping("/submit")
    @ResponseBody public Message submit(@RequestParam String prompt, @RequestParam String model) {
        if (!modelExists(model)) {
            return new Message("Invalid model selected!", "error");
        }
        return new Message("Form submitted successfully!", "success");
    }

    private boolean modelExists(String model) {
        String[] validModels = getModels();
        for (String validModel : validModels) {
            if (validModel.equals(model)) {
                return true;
            }
        }
        return false;
    }

    private String[] getModels() {
        return new String[] {"mistral", "llava", "gemini"};
    }

    private String getPassword() throws IOException {
        Scanner scanner = new Scanner(new File("password"));
        String password = "";
        if (scanner.hasNextLine()) password = scanner.nextLine();
        scanner.close();
        return password;
    }
}
