package com.example.consumer.steps;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


import com.example.consumer.client.JukeboxClient;
import com.example.consumer.dto.ActivationResponse;
import io.cucumber.java.en.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;



public class JukeboxSteps {

    private JukeboxClient jukeboxClient;
    private String serialNumber;
    private ActivationResponse response;

    // Mocks WebClient
    @Mock private WebClient mockWebClient;
    @Mock private WebClient.RequestBodyUriSpec mockRequestBodyUriSpec;
    @Mock private WebClient.RequestBodySpec mockRequestBodySpec;
    @Mock private WebClient.RequestHeadersSpec mockRequestHeadersSpec;
    @Mock private WebClient.ResponseSpec mockResponseSpec;

    public JukeboxSteps() {
        MockitoAnnotations.openMocks(this);
    jukeboxClient = new JukeboxClient(mockWebClient);
    }

    @Given("le numéro de série {string}")
    public void givenSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @When("j'active le jukebox")
    public void activateJukebox() {
        try {
            mockPipeline(serialNumber);
            response = jukeboxClient.activateJukebox(serialNumber).block();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Then("la réponse contient un identifiant de jukebox")
    public void verifyActivationSuccess() {
        assertNotNull(response);
        assertNotNull(response.getJukeboxId());
    }

    @Then("la réponse ne contient pas d'identifiant de jukebox")
    public void verifyActivationFallback() {
        assertNotNull(response);
        assertNull(response.getJukeboxId());
    }

    private void mockPipeline(String serialNumber) {
        when(mockWebClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri("/api/jukebox/activate")).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.contentType(any())).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.bodyValue(any())).thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.onStatus(any(), any())).thenReturn(mockResponseSpec);

        if (serialNumber.equals("SN-123")) {
            when(mockResponseSpec.bodyToMono(ActivationResponse.class))
                    .thenReturn(Mono.just(new ActivationResponse("JBOX-123")));
        } else {
            when(mockResponseSpec.bodyToMono(ActivationResponse.class))
                    .thenReturn(Mono.error(WebClientResponseException.create(404, "Not Found", null, null, null)));
        }

    }
}

