package com.openbutler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.command.annotation.CommandScan;

@SpringBootApplication
public class OpenButlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenButlerApplication.class, args);
    }

}
