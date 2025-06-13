package com.praxi.praxi;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.password4j.Password;

@RestController
public class PraxiApiController {
    @GetMapping("/api/get-models")
    public String[] getModels(@RequestParam String password) throws IOException, InterruptedException {
        String correctPassword = getPassword();
        if (!Password.check(password, correctPassword).withBcrypt())
            return "You are not verified".split("\n");
        String response = send("GRAB_MODELS").trim();
        return response == "" ? new String[0] : response.split("\n");
    }

    @GetMapping("/api/generate")
    public HashMap<String, Object> generate(
        @RequestParam String model, 
        @RequestParam String prompt, 
        @RequestParam String password, 
        @RequestParam(required = false, defaultValue = "false") String trim) throws IOException, InterruptedException {

        String correctPassword = getPassword();
        if (!Password.check(password, correctPassword).withBcrypt())
            return new HashMap<String, Object>() {{
                put("error", "You are not verified");
            }};
        if (!modelExists(model, password))
            new HashMap<>() {{
                put("error", "Invalid model selected!");
            }};

        String format = trim.equals("true") ? "text" : "json";
        String response = send("GENERATE", prompt, model, format).trim();
        
        if (format.equals("json")) {
            Gson gson = new Gson();
            Type type = new TypeToken<HashMap<String, Object>>(){}.getType();
            return !response.equals("") 
            ? gson.fromJson(response, type) 
            : new HashMap<String, Object>() {{
                put("error", "Error connecting to server!");
            }};
        }

        return new HashMap<String, Object>() {{
            put("response", response);
        }};
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

    private String getIP() throws IOException {
        Scanner scanner = new Scanner(new File("ip"));
        String ip = "";
        if (scanner.hasNextLine()) ip = scanner.nextLine();
        scanner.close();
        return ip == null ? "" : ip;
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

    private boolean modelExists(String model, String password) throws IOException, InterruptedException {
        String[] validModels = getModels(password);
        for (String validModel : validModels) {
            if (validModel.equals(model)) {
                return true;
            }
        }
        return false;
    }
}
