package com.tamojit.matchingservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TOPIC: ride.requested
 * Published by ride-service
 * Consumed by matching-service
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RideRequestedEvent {
    private String rideId;
    private String riderId;

    private double pickupLatitude;
    private double pickupLongitude;
    private String pickupAddress;

    private double dropLatitude;
    private double dropLongitude;
    private String dropAddress;
}