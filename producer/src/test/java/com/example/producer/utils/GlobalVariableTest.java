package com.example.producer.utils;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "partition.number=10")
class GlobalVariableTest {

    @Autowired
    private GlobalVariable globalVariable;

    @Test
    void shouldInjectPartitionNumberFromProperties() {
        assertThat(globalVariable.getPartitionNumber()).isEqualTo(10);
    }
}

