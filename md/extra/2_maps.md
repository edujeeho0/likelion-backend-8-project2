# 사용자 위치기반 기능

이전 NCP 실습에 활용했던 코드를 다시 활용한다. `RestClient`를 바탕으로 HTTP interface를 만들어 사용한다.

```java
public interface NcpMapApiService {
    @GetExchange("/map-direction/v1/driving")
    DirectionNcpResponse direction5(@RequestParam Map<String, Object> params);

    @GetExchange("/map-geocode/v2/geocode")
    GeoNcpResponse geocode(@RequestParam Map<String, Object> params);

    @GetExchange("/map-reversegeocode/v2/gc")
    RGeoNcpResponse reverseGeocode(@RequestParam Map<String, Object> params);
}
```

```java
public interface NcpGeolocationService {
    @GetExchange
    GeoLocationNcpResponse geoLocation(@RequestParam Map<String, Object> params);
}
```

이때 이 두가지 `interface`를 묶어서 기본 인자를 추가하는 `NcpService`를 따로 만들어서 편의성을 늘린다.

```java
public interface NcpService {
    DirectionNcpResponse direction5(PointDto start, PointDto goal);
    DirectionNcpResponse direction5(String start, String goal);
    GeoNcpResponse geocode(String query, String coords, Integer page, Integer count);
    RGeoNcpResponse reverseGeocode(String coords);
    GeoLocationNcpResponse geoLocation(String ip);
}
```

실제로는 모든 기능을 사용하진 않는다.

## 중고 거래 장소 제안

장소 제안 자체는 또다른 Entity이다.
주소와 좌표를 입력할 수 있으며, 제안과 확정 상태가 존재한다. 주소와 좌표는 프런트에서 전달받는 것을 가정한다.

```java
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class TradeLocation extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;
    @ManyToOne(fetch = FetchType.LAZY)
    private TradeOffer offer;
    private String address;
    private String coordinates;

    @Setter
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Status status = Status.OFFERED;

    public enum Status {
        OFFERED, CONFIRMED
    }
}
```

장소 제안과 확정은 CRUD의 연장선이며, 진행하는 과정에서 거래가 확정된 상태인지, 제안한 사용자인지, 물품을 등록한 사용자인지 등을 검증한다.

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class OfferService {
    private final TradeItemRepo itemRepo;
    private final TradeOfferRepo offerRepo;
    private final TradeLocationRepo locationRepo;
    private final AuthenticationFacade authFacade;
    private final NcpApiService apiService;
    // ...
    
    public TradeLocationDto createLocation(
            Long offerId,
            TradeLocationDto locationDto
    ) {
        TradeOffer offer = offerRepo.findById(offerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!offer.getStatus().equals(TradeOffer.Status.CONFIRMED))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        if (locationRepo.existsByOfferIdAndStatus(offerId, TradeLocation.Status.CONFIRMED))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        UserEntity user = authFacade.extractUser();
        if (!offer.getUser().getId().equals(user.getId())
            && !offer.getItem().getUser().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        return TradeLocationDto.fromEntity(locationRepo.save(TradeLocation.builder()
                .user(user)
                .offer(offer)
                .address(locationDto.getAddress())
                .coordinates(locationDto.getCoordinates())
                .build()));
    }

    public TradeLocationDto acceptLocation(
            Long offerId,
            Long locationId
    ) {
        TradeLocation location = locationRepo.findById(locationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!location.getOffer().getId().equals(offerId))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        UserEntity user = authFacade.extractUser();
        if (location.getUser().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        location.setStatus(TradeLocation.Status.CONFIRMED);
        return TradeLocationDto.fromEntity(locationRepo.save(location));
    }
}
```

확정이 된 뒤에는 geolocation과 directions 5를 이용해 경로를 구할 수 있다. 이때 IP는 프런트에서 전달한다고 가정한다.

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class OfferService {
    private final TradeItemRepo itemRepo;
    private final TradeOfferRepo offerRepo;
    private final TradeLocationRepo locationRepo;
    private final AuthenticationFacade authFacade;
    private final NcpApiService apiService;
    private final AlertService alertService;

    // ...
    
    public DirectionNcpResponse findRoute(
            Long locationId,
            String fromIp
    ) {
        TradeLocation location = locationRepo.findById(locationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (location.getStatus().equals(OFFERED))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        GeoLocationNcpResponse geoResponse = apiService.geoLocation(fromIp);
        String fromCoords = String.format(
                "%s,%s",
                geoResponse.getGeoLocation().getLng(),
                geoResponse.getGeoLocation().getLat()
        );
        return apiService.direction5(fromCoords, location.getCoordinates());
    }
}
```

## 쇼핑몰 방문

쇼핑몰에 주소와 좌표 정보를 추가한다. 주소와 좌표는 프런트에서 전달받는 것을 가정한다.

```java
@Entity
public class Shop extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    private UserEntity owner;
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    private Category category;
    @Enumerated(EnumType.STRING)
    private Status status = Status.PREPARING;
    private String address;
    private String coordinates;
}
```

이후 Geolocation과 Directions 5를 이용해 현재의 IP 주소의 위치에서 쇼핑몰에 도달하는 방법을 제공받는 기능을 추가한다.

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopService {
    private final AuthenticationFacade authFacade;
    private final ShopRepo shopRepo;
    private final ShopOpenReqRepo openRepo;
    private final NcpApiService apiService;

    // ...
    public DirectionNcpResponse findRoute(Long id, String fromIp) {
        Shop shop = shopRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (shop.getStatus() != Shop.Status.OPEN)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        GeoLocationNcpResponse geoResponse = apiService.geoLocation(fromIp);
        String fromCoords = String.format(
                "%s,%s",
                geoResponse.getGeoLocation().getLng(),
                geoResponse.getGeoLocation().getLat()
        );
        return apiService.direction5(fromCoords, shop.getCoordinates());
    }
}
```

또한 주문 상태 갱신에 방문을 추가한다. 방문은 물리적으로 일어나는 행위인 만큼, 상태를 조정하는 것 외에는 특별한 작업이 필요하지 않다.

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final AuthenticationFacade authFacade;
    private final ShopRepo shopRepo;
    private final ShopItemRepo itemRepo;
    private final ShopItemOrderRepo orderRepo;
    private final TossHttpService tossHttpService;
    private final AlertService alertService;

    public ItemOrderDto updateState(Long orderId, ItemOrderDto dto) {
        ShopItemOrder order = getOrder(orderId);
        if (!order.getStatus().equals(ShopItemOrder.Status.ORDERED))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        UserEntity user = authFacade.extractUser();
        switch (dto.getStatus()) {
            // ...
            case VISIT -> {
                if (!order.getOrderUser().getId().equals(user.getId()))
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                order.setStatus(dto.getStatus());
            }
            // ...
        }
        return ItemOrderDto.fromEntity(orderRepo.save(order));
    }
}
```
