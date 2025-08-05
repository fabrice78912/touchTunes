package com.example.producer.web;

import com.example.common_lib.model.JukeboxEvent;
import com.example.common_lib.model.response.ApiResponse;
import com.example.producer.model.dto.ActivationResponse;
import com.example.producer.model.dto.HardwareInfo;
import com.example.producer.service.JukeboxProducerService;
import com.example.producer.service.JukeboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/jukebox")
@RequiredArgsConstructor
@Slf4j
public class JukeboxEventController {

  private final JukeboxProducerService producerService;

  private final JukeboxService jukeboxService;

  @PostMapping(
      value = "/activate",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ApiResponse<ActivationResponse>> activateJukebox(
      @RequestBody HardwareInfo hardwareInfo) {
    return jukeboxService
        .activateJukebox(hardwareInfo.serialNumber())
        .map(
            response ->
                ApiResponse.<ActivationResponse>builder()
                    .timestamp(response.getTimestamp())
                    .status(response.getStatus())
                    .message(response.getMessage())
                    .code(response.getCode())
                    .data(new ActivationResponse(response.getData())) // on adapte ici
                    .path("/api/jukebox/activate")
                    .build());
  }

  @PostMapping
  public Mono<ApiResponse<Map<String, String>>> publish(@RequestBody JukeboxEvent event) {
    return producerService.sendEvent(event, "/api/jukebox/events");
  }
}
