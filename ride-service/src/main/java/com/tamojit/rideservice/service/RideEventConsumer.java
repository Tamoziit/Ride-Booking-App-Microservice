package com.tamojit.rideservice.service;

import com.tamojit.rideservice.event.RideMatchedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RideEventConsumer {
    private final RideService rideService;

    @KafkaListener(
        topics = "ride.matched",
        groupId = "ride-service-group"
    )
    public void consumeRideMatchedEvent(RideMatchedEvent event) {
        try {
            rideService.updateRideWithDriver(
                event.getRideId(),
                event.getDriverId()
            );
        } catch (Exception e) {
            log.error("Error in processing ride request: {} - {}", event.getRideId(), e.getMessage());

            // in prod: send to Dead letter queue for retry
        }
    }
}
