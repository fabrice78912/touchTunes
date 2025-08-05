package com.example.producer.repo.impl;


import com.example.producer.model.PlayRequest;
import java.util.ArrayList;
import java.util.List;

import com.example.producer.repo.PlayRequestRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class PlayRequestRepositoryImpl implements PlayRequestRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Page<PlayRequest> findAllWithFilters(Pageable pageable, List<String> filters) {

        Query query = new Query();

        if (filters != null) {
            List<Criteria> criteriaList = new ArrayList<>();

            for (String filter : filters) {
                // format key:value
                String[] parts = filter.split(":", 2);

                if (parts.length == 2) {
                    criteriaList.add(Criteria.where(parts[0].trim()).is(parts[1].trim()));
                }
            }

            if (!criteriaList.isEmpty()) {
                query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
            }
        }

        long total = mongoTemplate.count(query, PlayRequest.class);

        query.with(pageable);

        List<PlayRequest> results = mongoTemplate.find(query, PlayRequest.class);

        return new PageImpl<>(results, pageable, total);
    }
}

