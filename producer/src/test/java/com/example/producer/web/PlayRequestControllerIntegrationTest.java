package com.example.producer.web;

import com.example.producer.model.PlayRequest;
import com.example.producer.service.nosql.PlayRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.web.servlet.MockMvc;


import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(PlayRequestController.class)
@AutoConfigureMockMvc
class PlayRequestControllerIntegrationTest {

    private static final String BASE_URL = "/api/play-requests";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayRequestService playRequestService;

    @Autowired
    private ObjectMapper objectMapper;

    private PlayRequest createPlayRequest(String id, String status, String priority) {
        return PlayRequest.builder()
                .id(id)
                .status(status)
                .priority(priority)
                .requestedAt(Instant.now())
                .build();
    }

  /*  @Test
    void testGetAllPlayRequests_NoFilter() throws Exception {
        List<PlayRequest> list = List.of(createPlayRequest("1", "PENDING", "STANDARD"));

        // Mock le service pour renvoyer un Page comme d'habitude
        when(playRequestService.getAllPlayRequests(any(Pageable.class), anyString()))
                .thenReturn(new PageImpl<>(list));

        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                // JSONPath sur la Map générée par le controller
                .andExpect(jsonPath("$.content[0].id").value("1"))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
*/
    @Test
    void testGetAllPlayRequests_SortDesc() throws Exception {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "requestedAt"));
        List<PlayRequest> list = List.of(createPlayRequest("2", "PENDING", "STANDARD"));
        Page<PlayRequest> page = new PageImpl<>(list, pageable, list.size());

        when(playRequestService.getAllPlayRequests(pageable, null)).thenReturn(page);

        mockMvc.perform(get(BASE_URL)
                        .param("sortBy", "requestedAt")
                        .param("sortDirection", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("2"));
    }

    @Test
    void testGetAllPlayRequests_FilterSingle() throws Exception {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "requestedAt"));
        List<PlayRequest> list = List.of(createPlayRequest("3", "PENDING", "BOOSTED"));
        Page<PlayRequest> page = new PageImpl<>(list, pageable, list.size());

        String filter = "priority:BOOSTED";

        when(playRequestService.getAllPlayRequests(pageable, filter)).thenReturn(page);

        mockMvc.perform(get(BASE_URL)
                        .param("filter", filter))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].priority").value("BOOSTED"));
    }

    @Test
    void testGetAllPlayRequests_FilterMultiple() throws Exception {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "requestedAt"));
        List<PlayRequest> list = List.of(createPlayRequest("4", "PENDING", "BOOSTED"));
        Page<PlayRequest> page = new PageImpl<>(list, pageable, list.size());

        String filter = "priority:BOOSTED,status:PENDING";

        when(playRequestService.getAllPlayRequests(pageable, filter)).thenReturn(page);

        mockMvc.perform(get(BASE_URL)
                        .param("filter", filter))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.content[0].priority").value("BOOSTED"));
    }

    @Test
    void testGetAllPlayRequests_FilterEmpty() throws Exception {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "requestedAt"));
        List<PlayRequest> list = List.of(createPlayRequest("5", "PENDING", "STANDARD"));
        Page<PlayRequest> page = new PageImpl<>(list, pageable, list.size());

        when(playRequestService.getAllPlayRequests(pageable, "")).thenReturn(page);
        when(playRequestService.getAllPlayRequests(pageable, null)).thenReturn(page);

        // filter empty
        mockMvc.perform(get(BASE_URL)
                        .param("filter", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("5"));

        // filter null
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("5"));
    }

    @Test
    void testGetAllPlayRequests_FullCombination() throws Exception {
        Pageable pageable = PageRequest.of(1, 5, Sort.by(Sort.Direction.DESC, "requestedAt"));
        List<PlayRequest> list = List.of(createPlayRequest("6", "PENDING", "BOOSTED"));
        Page<PlayRequest> page = new PageImpl<>(list, pageable, 10);

        String filter = "priority:BOOSTED,status:PENDING";

        when(playRequestService.getAllPlayRequests(pageable, filter)).thenReturn(page);

        mockMvc.perform(get(BASE_URL)
                        .param("page", "1")
                        .param("size", "5")
                        .param("sortBy", "requestedAt")
                        .param("sortDirection", "desc")
                        .param("filter", filter))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("6"))
                .andExpect(jsonPath("$.totalElements").value(10));
    }

    @Test
    void testGetAllPlayRequests_FilterCommaOnly() throws Exception {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "requestedAt"));
        List<PlayRequest> list = List.of(createPlayRequest("7", "PENDING", "STANDARD"));
        Page<PlayRequest> page = new PageImpl<>(list, pageable, list.size());

        String filter = ","; // doit être traité comme filtre vide

        when(playRequestService.getAllPlayRequests(pageable, filter)).thenReturn(page);

        mockMvc.perform(get(BASE_URL)
                        .param("filter", filter))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("7"));
    }
}
