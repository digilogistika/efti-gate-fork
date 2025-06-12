package eu.efti.platformgatesimulator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class PlatformGateSimulatorApplication {
    public static void main(final String[] args) {
        SpringApplication.run(PlatformGateSimulatorApplication.class, args);
    }
}
