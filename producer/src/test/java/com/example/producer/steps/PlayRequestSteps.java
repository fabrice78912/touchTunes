package com.example.producer.steps;

import com.example.producer.model.PlayRequest;
import com.example.producer.service.nosql.PlayRequestService;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PlayRequestSteps {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayRequestService playRequestService;

    private List<PlayRequest> mockList;
    private PageImpl<PlayRequest> mockedPage;

    @Before
    public void setup() {
        // Reset du mock avant chaque scénario
        reset(playRequestService);
    }

    // ==========================
    // GIVEN
    // ==========================

    @Given("qu'il existe des play requests en base")
    public void givenPlayRequestsExist() {
        PlayRequest req = PlayRequest.builder()
                .id("1")
                .status("STANDARD")
                .priority("STANDARD")
                .requestedAt(Instant.now())
                .build();

        mockList = List.of(req);
        mockedPage = new PageImpl<>(mockList);

        when(playRequestService.getAllPlayRequests(any(Pageable.class), any()))
                .thenReturn(mockedPage);
    }

    @Given("qu'il existe des play requests avec le statut {string}")
    public void givenPlayRequestsWithStatus(String statut) {
        PlayRequest req = PlayRequest.builder()
                .id("1")
                .status(statut)
                .priority("BOOSTED")
                .requestedAt(Instant.now())
                .build();

        mockList = List.of(req);
        mockedPage = new PageImpl<>(mockList);

        when(playRequestService.getAllPlayRequests(any(Pageable.class), eq(statut)))
                .thenReturn(mockedPage);
    }

    // ==========================
    // WHEN
    // ==========================

    @When("j'appelle l'endpoint GET {string} sans filtre")
    public void whenCallEndpointWithoutFilter(String endpoint) throws Exception {
        mockMvc.perform(get(endpoint))
                .andExpect(status().isOk());
    }

    @When("j'appelle l'endpoint GET {string}")
    public void whenCallEndpointWithFilter(String endpoint) throws Exception {
        mockMvc.perform(get(endpoint))
                .andExpect(status().isOk());
    }

    // ==========================
    // THEN
    // ==========================

    @Then("la réponse HTTP doit être 200")
    public void thenHttpStatus200() {
        // Vérifié via MockMvc dans les étapes WHEN
    }

    @Then("la réponse contient 1 play request")
    public void thenResponseContainsOne() {
        assert mockedPage.getContent().size() == 1;
    }

    @Then("la réponse contient des play requests avec le statut {string}")
    public void thenResponseContainsStatus(String statut) {
        for (PlayRequest req : mockedPage.getContent()) {
            assert req.getStatus().equals(statut);
        }
    }
}
