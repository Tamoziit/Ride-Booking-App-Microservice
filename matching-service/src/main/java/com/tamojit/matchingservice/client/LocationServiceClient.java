package com.tamojit.matchingservice.client;

import com.tamojit.matchingservice.dto.NearByDriverResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * For inter-service API calls b/w matching-service & location-service
 */
@FeignClient(name = "location-service", url = "${location.service.url}")
public interface LocationServiceClient {
    @GetMapping("/api/v1/locations/drivers/nearby")
    public List<NearByDriverResponse> getNearByDrivers(
        @RequestParam double latitude,
        @RequestParam double longitude,
        @RequestParam double radius
    );
}
