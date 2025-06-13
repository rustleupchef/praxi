package com.praxi.praxi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
		SpringApplication.run(PraxiApplication.class, args);
	}

}
