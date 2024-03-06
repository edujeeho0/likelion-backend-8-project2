package com.example.market.ncp.service;

import com.example.market.ncp.dto.PointDto;
import com.example.market.ncp.dto.direction.DirectionNcpResponse;
import com.example.market.ncp.dto.geocoding.GeoNcpResponse;
import com.example.market.ncp.dto.geolocation.GeoLocationNcpResponse;
import com.example.market.ncp.dto.rgeocoding.RGeoNcpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NcpApiService implements NcpService {
    private final NcpMapApiService mapApiService;
    private final NcpGeolocationService geolocationService;


    @Override
    public DirectionNcpResponse direction5(PointDto start, PointDto goal) {
        return direction5(start.toQueryValue(), goal.toQueryValue());
    }

    @Override
    public DirectionNcpResponse direction5(String start, String goal) {
        Map<String, Object> params = new HashMap<>();
        params.put("start", start);
        params.put("goal", goal);
        return mapApiService.direction5(params);
    }

    @Override
    public GeoNcpResponse geocode(String query, String coords, Integer page, Integer count) {
        Map<String, Object> params = new HashMap<>();
        params.put("query", query);
        if (coords != null)
            params.put("coordinate", coords);
        if (page != null)
            params.put("page", page);
        if (count != null)
            params.put("count", count);
        return mapApiService.geocode(params);
    }

    @Override
    public RGeoNcpResponse reverseGeocode(String coords) {
        Map<String, Object> params = new HashMap<>();
        params.put("coords", coords);
        params.put("output", "json");
        return mapApiService.reverseGeocode(params);
    }

    @Override
    public GeoLocationNcpResponse geoLocation(String ip) {
        Map<String, Object> params = new HashMap<>();
        params.put("ip", ip);
        params.put("ext", "t");
        params.put("responseFormatType", "json");
        return geolocationService.geoLocation(params);
    }
}
