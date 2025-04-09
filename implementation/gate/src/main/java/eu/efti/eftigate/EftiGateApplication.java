package eu.efti.eftigate;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@SpringBootApplication
@EnableRabbit
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
public class EftiGateApplication {
    public static void main(final String[] args) {
        try {
            // Load cacerts from resources and write to a temp file
            File tempCacerts = File.createTempFile("temp-cacerts", ".jks");
            try (InputStream in = EftiGateApplication.class.getClassLoader().getResourceAsStream("certs/cacerts")) {
                if (in == null) {
                    System.err.println("ERROR: 'certs/cacerts' not found in classpath.");
                    System.exit(1);
                }
                Files.copy(in, tempCacerts.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // Set system properties to use custom truststore
            System.setProperty("javax.net.ssl.trustStore", tempCacerts.getAbsolutePath());
            System.setProperty("javax.net.ssl.trustStorePassword", "changeit"); // Replace with actual password

        } catch (Exception e) {
            System.err.println("Failed to initialize custom trust store:");
            e.printStackTrace();
            System.exit(1);
        }
        
        SpringApplication.run(EftiGateApplication.class, args);
    }
}
