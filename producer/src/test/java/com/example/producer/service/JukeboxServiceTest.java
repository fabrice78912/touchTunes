package com.example.producer.service;


import com.example.common_lib.model.response.ApiResponse;
import com.example.producer.model.Jukeboxe;
import com.example.producer.repo.JukeboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JukeboxServiceTest {

    @Mock
    private JukeboxRepository repository;

    @InjectMocks
    private JukeboxService jukeboxService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void activateJukebox_alreadyExists() {
        String serialNumber = "SN-001";
        Jukeboxe existing = Jukeboxe.builder()
                .serialNumber(serialNumber)
                .jukeboxId("JX000001")
                .build();

        // Mock de la recherche par serialNumber
        when(repository.findBySerialNumber(serialNumber))
                .thenReturn(Mono.just(existing));

        // Mock findByJukeboxId pour éviter NullPointerException
        when(repository.findByJukeboxId(anyString()))
                .thenReturn(Mono.empty());

        Mono<ApiResponse<String>> result = jukeboxService.activateJukebox(serialNumber);

        StepVerifier.create(result)
                .assertNext(resp -> {
                    assertEquals(HttpStatus.CONFLICT.value(), resp.getStatus());
                    assertEquals("Jukebox déjà activé avec ID : JX000001", resp.getMessage());
                    assertEquals("JUKBOX_ALREADY_EXISTS", resp.getCode());
                    assertEquals("JX000001", resp.getData());
                    assertNotNull(resp.getTimestamp());
                })
                .verifyComplete();

        verify(repository).findBySerialNumber(serialNumber);
        verify(repository, atLeastOnce()).findByJukeboxId(anyString());
    }

    @Test
    void activateJukebox_newActivation() {
        String serialNumber = "SN-002";

        // Aucun jukebox avec ce serialNumber
        when(repository.findBySerialNumber(serialNumber)).thenReturn(Mono.empty());

        // Aucun conflit sur l'ID généré
        when(repository.findByJukeboxId(anyString())).thenReturn(Mono.empty());

        // Sauvegarde du nouveau jukebox
        when(repository.save(any(Jukeboxe.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Mono<ApiResponse<String>> result = jukeboxService.activateJukebox(serialNumber);

        StepVerifier.create(result)
                .assertNext(resp -> {
                    assertEquals(HttpStatus.OK.value(), resp.getStatus());
                    assertEquals("Jukebox activé avec succès", resp.getMessage());
                    assertEquals("SUCCESS", resp.getCode());
                    assertNotNull(resp.getData());
                    assertTrue(resp.getData().startsWith("JX")); // ID généré
                    assertNotNull(resp.getTimestamp());
                })
                .verifyComplete();

        verify(repository).findBySerialNumber(serialNumber);
        verify(repository).findByJukeboxId(anyString());
        verify(repository).save(any(Jukeboxe.class));
    }

    @Test
    void generateUniqueId_collision() {
        String serialNumber = "SN-003";

        // Aucun jukebox avec ce serialNumber
        when(repository.findBySerialNumber(serialNumber)).thenReturn(Mono.empty());

        // Simule une collision sur le premier ID généré
        Jukeboxe existing = Jukeboxe.builder().jukeboxId("JXCOLLIDE").build();
        when(repository.findByJukeboxId(anyString()))
                .thenReturn(Mono.just(existing))  // première tentative -> collision
                .thenReturn(Mono.empty());        // deuxième tentative -> succès

        when(repository.save(any(Jukeboxe.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Mono<ApiResponse<String>> result = jukeboxService.activateJukebox(serialNumber);

        StepVerifier.create(result)
                .assertNext(resp -> {
                    assertEquals(HttpStatus.OK.value(), resp.getStatus());
                    assertEquals("Jukebox activé avec succès", resp.getMessage());
                    assertEquals("SUCCESS", resp.getCode());
                    assertNotNull(resp.getData());
                    assertTrue(resp.getData().startsWith("JX"));
                })
                .verifyComplete();

        verify(repository, times(2)).findByJukeboxId(anyString()); // deux appels à cause de la collision
        verify(repository).save(any(Jukeboxe.class));
    }

    @Test
    void activateJukebox_repositoryError() {
        String serialNumber = "SN-004";

        // Mock findBySerialNumber pour provoquer l'erreur
        when(repository.findBySerialNumber(serialNumber))
                .thenReturn(Mono.error(new RuntimeException("DB down")));

        // Mock findByJukeboxId pour éviter NullPointerException
        when(repository.findByJukeboxId(anyString()))
                .thenReturn(Mono.empty());

        Mono<ApiResponse<String>> result = jukeboxService.activateJukebox(serialNumber);

        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().equals("DB down"))
                .verify();

        verify(repository).findBySerialNumber(serialNumber);
        verify(repository, atLeastOnce()).findByJukeboxId(anyString());
    }

}
