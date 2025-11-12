package com.keneyamuso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Application principale KènèyaMuso
 * Plateforme de suivi de santé maternelle et infantile au Mali
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling  // Active les tâches planifiées (rappels quotidiens)
public class KeneyaMusoApplication {

    public static void main(String[] args) {
        SpringApplication.run(KeneyaMusoApplication.class, args);
    }
}

