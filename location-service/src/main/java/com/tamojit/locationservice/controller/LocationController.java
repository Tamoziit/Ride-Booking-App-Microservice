package com.tamojit.locationservice.controller;

import com.tamojit.locationservice.dto.DriverLocationRequest;
import com.tamojit.locationservice.dto.NearByDriverResponse;
import com.tamojit.locationservice.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@Slf4j
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    // driver calls this every 3 sec.
    @PostMapping("/drivers/update")
    public ResponseEntity<String> updateDriverLocation(
        @RequestBody DriverLocationRequest driverLocationRequest
    ) {
        locationService.updateDriverLocation(driverLocationRequest);
        return ResponseEntity.ok("Driver location updated successfully");
    }

    // matching-service calls this endpoint when a ride is requested
    @GetMapping("/drivers/nearby")
    public ResponseEntity<List<NearByDriverResponse>> getNearByDrivers(
        @RequestParam double latitude,
        @RequestParam double longitude,
        @RequestParam(defaultValue = "5.0") double radius
    ) {
        return ResponseEntity.ok(locationService.findNearByDrivers(
            latitude,
            longitude,
            radius
        ));
    }

    //driver goes offline
    @DeleteMapping("/drivers/{driverId}")
    public ResponseEntity<String> removeDriver(@PathVariable String driverId) {
        locationService.removeDriver(driverId);
        return ResponseEntity.ok("Driver removed successfully");
    }
}
