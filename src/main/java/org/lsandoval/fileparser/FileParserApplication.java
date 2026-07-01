package org.lsandoval.fileparser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FileParserApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileParserApplication.class, args);
	}

}
