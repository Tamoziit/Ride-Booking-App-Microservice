package com.tamojit.rideservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    // TOPIC: ride.requested - matching-service subscribes to this topic
    @Bean
    public NewTopic rideRequestedTopic() {
        return TopicBuilder.name("ride.requested")
            .partitions(3)
            .replicas(1)
            .build();
    }

    // TOPIC: ride.matched - ride-service subscribes to this topic
    @Bean
    public NewTopic rideMatchedTopic() {
        return TopicBuilder.name("ride.matched")
            .partitions(3)
            .replicas(1)
            .build();
    }
}
