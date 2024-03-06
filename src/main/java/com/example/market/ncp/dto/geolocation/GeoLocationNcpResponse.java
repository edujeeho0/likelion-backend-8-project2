package com.example.market.ncp.dto.geolocation;

import lombok.Data;

@Data
public class GeoLocationNcpResponse {
    private String requestId;
    private Integer returnCode;
    private GeoLocation geoLocation;
}
