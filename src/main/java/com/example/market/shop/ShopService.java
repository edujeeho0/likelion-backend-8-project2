package com.example.market.shop;

import com.example.market.auth.AuthenticationFacade;
import com.example.market.auth.entity.UserEntity;
import com.example.market.ncp.dto.direction.DirectionNcpResponse;
import com.example.market.ncp.dto.geolocation.GeoLocationNcpResponse;
import com.example.market.ncp.service.NcpApiService;
import com.example.market.shop.dto.ShopDto;
import com.example.market.shop.entity.Shop;
import com.example.market.shop.entity.ShopOpenRequest;
import com.example.market.shop.repo.ShopOpenReqRepo;
import com.example.market.shop.repo.ShopRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopService {
    private final AuthenticationFacade authFacade;
    private final ShopRepo shopRepo;
    private final ShopOpenReqRepo openRepo;
    private final NcpApiService apiService;

    public ShopDto readOne(Long id) {
        Shop shop = shopRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity user = authFacade.extractUser();
        if (
                shop.getStatus() != Shop.Status.OPEN &&
                !shop.getOwner().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return ShopDto.fromEntity(shop);
    }

    public Page<ShopDto> readPage(Pageable pageable) {
        return shopRepo.findAllByStatus(Shop.Status.OPEN, pageable)
                .map(ShopDto::fromEntity);
    }

    @Transactional
    public ShopDto update(Long id, ShopDto dto) {
        Shop shop = checkOwner(id);
        shop.setName(dto.getName());
        shop.setDescription(dto.getDescription());
        shop.setCategory(dto.getCategory());
        shop.setAddress(dto.getAddress());
        shop.setCoordinates(dto.getCoordinates());
        return ShopDto.fromEntity(shopRepo.save(shop));
    }

    // 쇼핑몰 개설 허가 요청
    @Transactional
    public void requestOpen(Long id) {
        Shop shop = checkOwner(id);
        if (!(
                shop.getName() != null &&
                shop.getDescription() != null &&
                shop.getCategory() != null &&
                shop.getStatus() == Shop.Status.PREPARING
        ))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        if (openRepo.existsByShopIdAndIsApprovedIsNull(id))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        openRepo.save(ShopOpenRequest.builder()
                .shop(shop)
                .build());
    }

    @Transactional
    public void requestClose(Long id, ShopDto dto) {
        Shop shop = checkOwner(id);
        if (shop.getStatus() != Shop.Status.OPEN)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        shop.setCloseReason(dto.getCloseReason());
        shopRepo.save(shop);
    }

    private Shop checkOwner(Long id) {
        Shop shop = shopRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity user = authFacade.extractUser();
        if (!shop.getOwner().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return shop;
    }

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
