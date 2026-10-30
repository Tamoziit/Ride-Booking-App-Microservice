package com.tamojit.locationservice.service;

import com.tamojit.locationservice.dto.DriverLocationRequest;
import com.tamojit.locationservice.dto.NearByDriverResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationService {
    private static final String DRIVERS_GEO_KEY = "drivers:locations";

    private final RedisTemplate<String, String> redisTemplate;

    public void updateDriverLocation(DriverLocationRequest driverLocationRequest) {
        log.info("Updating driver location for driver: {}", driverLocationRequest.getDriverId());

        // longitude FIRST, latitude SECOND: geospatial standard
        Point driverPoint = new Point(
            driverLocationRequest.getLongitude(),
            driverLocationRequest.getLatitude()
        );

        // GEOADD <KEY> <lat> <lon> <id>
        redisTemplate.opsForGeo().add(
            DRIVERS_GEO_KEY,
            driverPoint,
            driverLocationRequest.getDriverId()
        );

        log.info("Updated driver location for driver: {}", driverLocationRequest.getDriverId());
    }

    public List<NearByDriverResponse> findNearByDrivers(
        double latitude,
        double longitude,
        double radius // in km
    ) {
        log.info("Finding nearby drivers at lat: {}, long: {} within {} km", latitude, longitude, radius);

        // creating search area within the mentioned radius of <lat, long>
        Circle searchArea = new Circle(
            new Point(longitude, latitude),
            new Distance(radius, Metrics.KILOMETERS)
        );

        // GEOSEARCH
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo().radius(
            DRIVERS_GEO_KEY,
            searchArea,
            RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeCoordinates()
                .includeDistance()
                .sortAscending()
                .limit(10)
        );

        List<NearByDriverResponse> nearByDriverResponseList = new ArrayList<>();
        if (results != null) {
            results.getContent().forEach(result -> {
                RedisGeoCommands.GeoLocation<String> location = result.getContent();
                nearByDriverResponseList.add(new NearByDriverResponse(
                    location.getName(), // driverId
                    location.getPoint().getY(),
                    location.getPoint().getX(),
                    result.getDistance().getValue() // distance b/w rider & passenger
                ));
            });
        }

        log.info("Found {} nearby drivers", nearByDriverResponseList.size());
        return nearByDriverResponseList;
    }

    public void removeDriver(String driverId) {
        log.info("Removing driver: {}", driverId);

        // ZREM
        redisTemplate.opsForGeo().remove(DRIVERS_GEO_KEY, driverId);
    }
}
