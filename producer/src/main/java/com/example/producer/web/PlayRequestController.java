package com.example.producer.web;

import com.example.common_lib.model.JukeboxEvent;
import com.example.common_lib.model.response.ApiResponse;
import com.example.producer.model.PlayRequest;
import com.example.producer.model.dto.PlayRequestDto;
import com.example.producer.service.nosql.PlayRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/play-requests")
public class PlayRequestController {

  private final PlayRequestService playRequestService;

  @PostMapping
  public ResponseEntity<ApiResponse<JukeboxEvent>> createPlayRequest(
      @RequestBody PlayRequestDto dto) {
    ApiResponse<JukeboxEvent> response =
        playRequestService.createPlayRequest(
            dto.getUserId(),
            dto.getTrackId(),
            dto.getJukeboxId(),
            dto.getEventType(),
            dto.getPriority());
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @GetMapping
  public Page<PlayRequest> getAllPlayRequests(
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size,
          @RequestParam(defaultValue = "requestedAt") String sortBy,
          @RequestParam(defaultValue = "asc") String sortDirection) {

    Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

    return playRequestService.getAllPlayRequests(pageable);
  }


}
