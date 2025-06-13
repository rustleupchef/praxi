package com.praxi.praxi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PraxiApplication {

	public static void main(String[] args) throws IOException {
		if (!new File("ip").exists()) {
            FileWriter writer = new FileWriter(new File("ip"));
			writer.write("");
			writer.close();
			return;
        }
		Scanner scanner = new Scanner(new File("ip"));
		if (!scanner.hasNextLine()) {
			scanner.close();
			return;
		}
		SpringApplication.run(PraxiApplication.class, args);
	}

}
