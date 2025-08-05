package com.example.consumer.web;

import com.example.consumer.service.ActivationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/activate")
public class Activate {

    private final ActivationService activationService;

    @PostMapping
    @Operation(summary = "Activate a jukebox")
    public Mono<Void> activate() {
        return activationService.activate();
    }
}

