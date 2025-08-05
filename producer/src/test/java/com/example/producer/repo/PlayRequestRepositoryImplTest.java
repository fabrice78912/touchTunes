package com.example.producer.repo;

import com.example.producer.model.PlayRequest;
import com.example.producer.repo.impl.PlayRequestRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayRequestRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private PlayRequestRepositoryImpl playRequestRepository;


    // ---------------------------------------------------------
    // CASE 1 : no filters
    // ---------------------------------------------------------
    @Test
    void testFindAllWithFilters_NoFilters() {

        Pageable pageable = PageRequest.of(0, 10);

        List<PlayRequest> fakeResults = List.of(new PlayRequest());
        when(mongoTemplate.count(any(Query.class), eq(PlayRequest.class)))
                .thenReturn(1L);

        when(mongoTemplate.find(any(Query.class), eq(PlayRequest.class)))
                .thenReturn(fakeResults);

        Page<PlayRequest> page = playRequestRepository.findAllWithFilters(pageable, null);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(1);

        verify(mongoTemplate).count(any(Query.class), eq(PlayRequest.class));
        verify(mongoTemplate).find(any(Query.class), eq(PlayRequest.class));
    }

    // ---------------------------------------------------------
    // CASE 2 : single filter "priority:BOOSTED"
    // ---------------------------------------------------------
    @Test
    void testFindAllWithFilters_SingleFilter() {

        Pageable pageable = PageRequest.of(0, 10);

        when(mongoTemplate.count(any(), eq(PlayRequest.class))).thenReturn(1L);
        when(mongoTemplate.find(any(), eq(PlayRequest.class)))
                .thenReturn(List.of(new PlayRequest()));

        Page<PlayRequest> result = playRequestRepository.findAllWithFilters(
                pageable, List.of("priority:BOOSTED")
        );

        assertThat(result.getContent()).hasSize(1);

        // Capture the Query to assert criteria
        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).count(captor.capture(), eq(PlayRequest.class));

        Query builtQuery = captor.getValue();

        assertThat(builtQuery.getQueryObject().toString())
                .contains("priority=BOOSTED");
    }

    // ---------------------------------------------------------
    // CASE 3 : multiple filters "priority:BOOSTED", "status:PENDING"
    // ---------------------------------------------------------
    @Test
    void testFindAllWithFilters_MultipleFilters() {

        Pageable pageable = PageRequest.of(0, 10);

        when(mongoTemplate.count(any(), eq(PlayRequest.class))).thenReturn(2L);
        when(mongoTemplate.find(any(), eq(PlayRequest.class)))
                .thenReturn(List.of(new PlayRequest(), new PlayRequest()));

        Page<PlayRequest> result = playRequestRepository.findAllWithFilters(
                pageable, List.of("priority:BOOSTED", "status:PENDING")
        );

        assertThat(result.getTotalElements()).isEqualTo(2);

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).count(captor.capture(), eq(PlayRequest.class));

        String queryString = captor.getValue().getQueryObject().toString();

        assertThat(queryString).contains("priority=BOOSTED");
        assertThat(queryString).contains("status=PENDING");
    }

    // ---------------------------------------------------------
    // CASE 4 : malformed filter "priority" (no ':')
    // Should be ignored
    // ---------------------------------------------------------
    @Test
    void testFindAllWithFilters_IgnoredMalformedFilter() {

        Pageable pageable = PageRequest.of(0, 5);

        when(mongoTemplate.count(any(), eq(PlayRequest.class))).thenReturn(0L);
        when(mongoTemplate.find(any(), eq(PlayRequest.class))).thenReturn(List.of());

        Page<PlayRequest> result = playRequestRepository.findAllWithFilters(
                pageable, List.of("priority") // ❌ invalid
        );

        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    // ---------------------------------------------------------
    // CASE 5 : ensure pagination is applied
    // ---------------------------------------------------------
    @Test
    void testFindAllWithFilters_PaginationApplied() {

        Pageable pageable = PageRequest.of(2, 20); // page 2, size 20

        when(mongoTemplate.count(any(), eq(PlayRequest.class))).thenReturn(0L);
        when(mongoTemplate.find(any(), eq(PlayRequest.class))).thenReturn(List.of());

        playRequestRepository.findAllWithFilters(pageable, List.of("priority:BOOSTED"));

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(PlayRequest.class));

        Query q = captor.getValue();

        assertThat(q.getSkip()).isEqualTo(40); // page 2 * size 20
        assertThat(q.getLimit()).isEqualTo(20);
    }

    // ---------------------------------------------------------
    // CASE 6 : empty list of filters
    // same behaviour as no filters
    // ---------------------------------------------------------
    @Test
    void testFindAllWithFilters_EmptyList() {

        Pageable pageable = PageRequest.of(0, 10);

        when(mongoTemplate.count(any(), eq(PlayRequest.class))).thenReturn(1L);
        when(mongoTemplate.find(any(), eq(PlayRequest.class)))
                .thenReturn(List.of(new PlayRequest()));

        Page<PlayRequest> result = playRequestRepository.findAllWithFilters(
                pageable, List.of()
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}

