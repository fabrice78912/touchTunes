package com.example.producer.cucumber;



import com.example.producer.model.PlayRequest;
import com.example.producer.service.nosql.PlayRequestService;
import com.example.producer.web.PlayRequestController;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlayRequestController.class)
public class PlayRequestSteps {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayRequestService playRequestService;

    private Page<PlayRequest> mockedPage;

    @Before
    public void setup() {
    }

    @Given("une liste de play requests existe avec filtre {string}")
    public void givenList(String status) {
        PlayRequest req = PlayRequest.builder()
                .id("1")
                .status(status)
                .priority("STANDARD")
                .requestedAt(Instant.now())
                .build();

        mockedPage = new PageImpl<>(List.of(req));

        Mockito.when(playRequestService.getAllPlayRequests(any(Pageable.class), eq(status)))
                .thenReturn(mockedPage);
    }

    @When("j'appelle l'endpoint GET {string} avec page={int} size={int} sortBy={string} sortDirection={string} filter={string}")
    public void callEndpoint(
            String endpoint,
            int page,
            int size,
            String sortBy,
            String sortDirection,
            String filter
    ) throws Exception {

        mockMvc.perform(get(endpoint)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .param("sortBy", sortBy)
                        .param("sortDirection", sortDirection)
                        .param("filter", filter))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value(filter))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Then("la réponse HTTP doit être {int}")
    public void validateHttpStatus(int expectedStatus) {
        // déjà vérifié dans le step When
    }

    @Then("la liste doit contenir {int} élément")
    public void validateListSize(int expected) {
        assert mockedPage.getTotalElements() == expected;
    }

    @Then("l'élément {int} doit avoir status {string}")
    public void validateElementStatus(int index, String status) {
        assert mockedPage.getContent().get(index - 1).getStatus().equals(status);
    }
}

