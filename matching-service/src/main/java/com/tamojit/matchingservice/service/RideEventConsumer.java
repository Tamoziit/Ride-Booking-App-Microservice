package com.tamojit.matchingservice.service;

import com.tamojit.matchingservice.event.RideRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RideEventConsumer {
    private final MatchingService matchingService;

    /**
     * FLOW:
     * ride-service -> Kafka (ride.requested) -> RideEventConsumer -> matching-service
     */
    @KafkaListener(
        topics = "ride.requested",
        groupId = "matching-service-group"
    )
    public void consumeRideRequestedEvent(RideRequestedEvent event) {
        try {
            matchingService.matchDriverForRide(event);
        } catch (Exception e) {
            log.error("Error in processing ride request: {} - {}", event.getRideId(), e.getMessage());

            // in prod: send to Dead letter queue for retry
        }
    }
}
