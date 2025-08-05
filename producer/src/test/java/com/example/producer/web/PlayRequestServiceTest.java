package com.example.producer.web;

import com.example.producer.model.PlayRequest;
import com.example.producer.repo.PlayRequestRepository;
import com.example.producer.service.nosql.PlayRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PlayRequestServiceTest {

    @Mock
    private PlayRequestRepository playRequestRepository;

    @InjectMocks
    private PlayRequestService playRequestService;

    private PlayRequest reqPending;
    private PlayRequest reqCompleted;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        reqPending = PlayRequest.builder()
                .id("1")
                .status("PENDING")
                .priority("STANDARD")
                .build();

        reqCompleted = PlayRequest.builder()
                .id("2")
                .status("COMPLETED")
                .priority("BOOSTED")
                .build();
    }

    @Test
    void testGetAllPlayRequests_NoFilter() {
        Page<PlayRequest> page = new PageImpl<>(List.of(reqPending, reqCompleted));
        when(playRequestRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<PlayRequest> result = playRequestService.getAllPlayRequests(PageRequest.of(0, 10), null);

        assertEquals(2, result.getTotalElements());
        verify(playRequestRepository, times(1)).findAll(any(Pageable.class));
        verify(playRequestRepository, never()).findAllWithFilters(any(Pageable.class), anyList());
    }

    @Test
    void testGetAllPlayRequests_BlankFilter() {
        Page<PlayRequest> page = new PageImpl<>(List.of(reqPending));
        when(playRequestRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<PlayRequest> result = playRequestService.getAllPlayRequests(PageRequest.of(0, 10), "   ");

        assertEquals(1, result.getTotalElements());
        verify(playRequestRepository, times(1)).findAll(any(Pageable.class));
        verify(playRequestRepository, never()).findAllWithFilters(any(Pageable.class), anyList());
    }

    @Test
    void testGetAllPlayRequests_WithFilter() {
        Page<PlayRequest> page = new PageImpl<>(List.of(reqPending));
        when(playRequestRepository.findAllWithFilters(any(Pageable.class), eq(List.of("PENDING"))))
                .thenReturn(page);

        Page<PlayRequest> result = playRequestService.getAllPlayRequests(PageRequest.of(0, 10), "PENDING");

        assertEquals(1, result.getTotalElements());
        verify(playRequestRepository, times(1)).findAllWithFilters(any(Pageable.class), eq(List.of("PENDING")));
        verify(playRequestRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void testGetAllPlayRequests_WithMultipleFilters() {
        Page<PlayRequest> page = new PageImpl<>(List.of(reqPending, reqCompleted));
        when(playRequestRepository.findAllWithFilters(any(Pageable.class), eq(List.of("PENDING", "COMPLETED"))))
                .thenReturn(page);

        Page<PlayRequest> result = playRequestService.getAllPlayRequests(PageRequest.of(0, 10), "PENDING,COMPLETED");

        assertEquals(2, result.getTotalElements());
        verify(playRequestRepository, times(1)).findAllWithFilters(any(Pageable.class), eq(List.of("PENDING", "COMPLETED")));
        verify(playRequestRepository, never()).findAll(any(Pageable.class));
    }
}

