package com.example.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class MessageController {

    private final WebClient webClient;

    // WebClient initialized to send requests to another service
    public MessageController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://external-service.com").build();
    }

    @PostMapping(value = "/message", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<MessageResponse> receiveAndForwardMessage(@RequestBody Mono<String> requestMono) {
        return requestMono.flatMap(message -> {
            System.out.println("Received message: " + message);

            // Modify the message before forwarding
            String modifiedMessage = message.toUpperCase() + " - Sent at " + LocalDateTime.now();

            // Forward the modified message to an external service
            return webClient.post()
                    .uri("/process")  // Assuming the external service has an endpoint at /process
                    .contentType(MediaType.TEXT_PLAIN)
                    .bodyValue(modifiedMessage)
                    .retrieve()
                    .bodyToMono(String.class) // Expecting a plain text response
                    .map(response -> new MessageResponse(response, "Forwarded Successfully"));
        });
    }
}
