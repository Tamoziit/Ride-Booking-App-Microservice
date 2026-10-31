package com.tamojit.matchingservice.service;

import com.tamojit.matchingservice.client.LocationServiceClient;
import com.tamojit.matchingservice.dto.NearByDriverResponse;
import com.tamojit.matchingservice.event.RideMatchedEvent;
import com.tamojit.matchingservice.event.RideRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchingService {
    private static final String RIDE_MATCHED_TOPIC = "ride.matched";
    private static final double DEFAULT_SEARCH_RADIUS_KM = 5.0;

    private final LocationServiceClient locationServiceClient;
    private final KafkaTemplate<String, RideMatchedEvent> kafkaTemplate;

    /**
     * Called when ride.requested event is consumed from Kafka
     * S1: Asking location-service for nearby drivers
     * S2: Scoring each driver to pick the best one
     * S3: Publish ride.matched event to Kafka
     */
    public void matchDriverForRide(RideRequestedEvent event) {
        List<NearByDriverResponse> nearByDrivers = locationServiceClient.getNearByDrivers(
            event.getPickupLatitude(),
            event.getPickupLongitude(),
            DEFAULT_SEARCH_RADIUS_KM
        );

        if (nearByDrivers.isEmpty()) {
            log.warn("No nearby drivers found for ride: {}", event.getRideId());
            return;
        }

        Optional<NearByDriverResponse> bestDriver = findBestDriver(nearByDrivers);

        if (bestDriver.isEmpty()) {
            log.warn("Couldn't find suitable driver for ride: {}", event.getRideId());
            return;
        }

        NearByDriverResponse assignedDriver = bestDriver.get();

        RideMatchedEvent rideMatchedEvent = new RideMatchedEvent(
            event.getRideId(),
            event.getRiderId(),
            assignedDriver.getDriverId(),
            assignedDriver.getLatitude(),
            assignedDriver.getLongitude(),
            assignedDriver.getDistanceInKm()
        );

        kafkaTemplate.send(RIDE_MATCHED_TOPIC, event.getRideId(), rideMatchedEvent);
        log.info("Ride matched event published to Kafka for ride: {}", rideMatchedEvent.getRideId());
    }

    /**
     * Scoring algo to find the best driver
     * Distance: 70% weight [less dist, more score]
     * Rating: 30% weight [more rating, more score]
     * --------------------------------------------
     * Score = (1 / (distance + 0.1)) * distanceWeight + rating * ratingWeight
     */
    private Optional<NearByDriverResponse> findBestDriver(List<NearByDriverResponse> nearByDrivers) {
        double distanceWeight = 0.7; // 70%
        double ratingWeight = 0.3; // 30%

        return nearByDrivers.stream()
            .max(Comparator.comparingDouble(driver -> {
                // distance score
                double distanceScore = 1.0 / (driver.getDistanceInKm() + 0.1); // adding 0.1 to denom. to avoid division by 0

                // simulating rating b/w 4.0-5.0
                double simulatedRating = 4.0 + Math.random();

                // final weighted score
                return (distanceScore * distanceWeight) + (simulatedRating * ratingWeight);
            }));
    }
}
