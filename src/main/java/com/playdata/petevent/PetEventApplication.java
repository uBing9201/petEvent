package com.playdata.petevent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PetEventApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetEventApplication.class, args);
    }

}
