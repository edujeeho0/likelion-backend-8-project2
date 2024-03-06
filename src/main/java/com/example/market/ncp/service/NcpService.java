package com.example.market.ncp.service;

import com.example.market.ncp.dto.PointDto;
import com.example.market.ncp.dto.direction.DirectionNcpResponse;
import com.example.market.ncp.dto.geocoding.GeoNcpResponse;
import com.example.market.ncp.dto.geolocation.GeoLocationNcpResponse;
import com.example.market.ncp.dto.rgeocoding.RGeoNcpResponse;

public interface NcpService {
    DirectionNcpResponse direction5(PointDto start, PointDto goal);
    DirectionNcpResponse direction5(String start, String goal);
    GeoNcpResponse geocode(String query, String coords, Integer page, Integer count);
    RGeoNcpResponse reverseGeocode(String coords);
    GeoLocationNcpResponse geoLocation(String ip);
}
