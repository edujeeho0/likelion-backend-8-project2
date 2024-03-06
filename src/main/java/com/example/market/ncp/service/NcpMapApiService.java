package com.example.market.ncp.service;

import com.example.market.ncp.dto.direction.DirectionNcpResponse;
import com.example.market.ncp.dto.geocoding.GeoNcpResponse;
import com.example.market.ncp.dto.rgeocoding.RGeoNcpResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import java.util.Map;

public interface NcpMapApiService {
    @GetExchange("/map-direction/v1/driving")
    DirectionNcpResponse direction5(@RequestParam Map<String, Object> params);

    @GetExchange("/map-geocode/v2/geocode")
    GeoNcpResponse geocode(@RequestParam Map<String, Object> params);

    @GetExchange("/map-reversegeocode/v2/gc")
    RGeoNcpResponse reverseGeocode(@RequestParam Map<String, Object> params);
}
