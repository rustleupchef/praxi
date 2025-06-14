package com.praxi.praxi;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
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
    public String login(HttpSession session) {
        if (session.getAttribute("verified") != null) {
            return "redirect:/home";
        }
        return "login";
    }

    @GetMapping("/models")
    public String models(HttpSession session) {
        if (session.getAttribute("verified") == null) {
            return "redirect:/login";
        }
        return "models";
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
    @ResponseBody public Message submit(
        @RequestParam String prompt, 
        @RequestParam String model) throws IOException, InterruptedException {
        if (!modelExists(model)) {
            return new Message("Invalid model selected!", "error");
        }
        String response = send("GENERATE", prompt, model, "text");
        if (response.equals("")) {
            return new Message("Error connecting to server!", "error");
        }
        return new Message(response, "success");
    }

    @PostMapping("/get-models")
    @ResponseBody public String getModelsPost() throws IOException, InterruptedException {
        return send("GRAB_MODELS").trim();
    }

    private boolean modelExists(String model) throws IOException, InterruptedException {
        String[] validModels = getModels();
        for (String validModel : validModels) {
            if (validModel.equals(model)) {
                return true;
            }
        }
        return false;
    }

    private String[] getModels() throws IOException, InterruptedException {
        String response = send("GRAB_MODELS").trim();
        return response == "" ? new String[0] : response.split("\n");
    }

    private String send(String... messages) throws IOException, InterruptedException {
        try (Socket socket = new Socket(getIP(), 5080)) {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            DataInputStream dis = new DataInputStream(socket.getInputStream());

            for (String message : messages) {
                dos.writeInt(message.length());
                dos.write(message.getBytes());
            }
            dos.flush();

            int length = dis.readInt();
            byte[] buffer = new byte[length];
            dis.read(buffer, 0, length);

            dis.close();
            dos.close();

            socket.close();
            return new String(buffer);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getPassword() throws IOException {
        if (!new File("password").exists()) {
            return "";
        }
        Scanner scanner = new Scanner(new File("password"));
        String password = "";
        if (scanner.hasNextLine()) password = scanner.nextLine();
        scanner.close();
        return password == null ? "" : password;
    }

    private String getIP() throws IOException {
        Scanner scanner = new Scanner(new File("ip"));
        String ip = "";
        if (scanner.hasNextLine()) ip = scanner.nextLine();
        scanner.close();
        return ip == null ? "" : ip;
    }
}