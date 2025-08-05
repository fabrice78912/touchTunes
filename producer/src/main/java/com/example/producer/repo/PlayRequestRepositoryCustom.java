package com.example.producer.repo;


import com.example.producer.model.PlayRequest;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PlayRequestRepositoryCustom {
    Page<PlayRequest> findAllWithFilters(Pageable pageable, List<String> filters);
}

