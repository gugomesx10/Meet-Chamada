package io.github.gugomesx10.meets;

import org.springframework.boot.SpringApplication;

public class TestGoogleMeetApplication {

    public static void main(String[] args) {
        SpringApplication.from(GoogleMeetApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
