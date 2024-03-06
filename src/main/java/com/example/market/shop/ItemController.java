package com.example.market.shop;

import com.example.market.shop.dto.ShopItemDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("shops/{shopId}/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService service;

    @PostMapping
    public ShopItemDto create(
            @PathVariable("shopId")
            Long shopId,
            @RequestBody
            ShopItemDto dto
    ) {
        return service.create(shopId, dto);
    }

    @GetMapping
    public Page<ShopItemDto> readPage(
            @PathVariable("shopId")
            Long shopId,
            Pageable pageable
    ) {
        return service.readPage(shopId, pageable);
    }

    @GetMapping("{itemId}")
    public ShopItemDto readOne(
            @PathVariable("shopId")
            Long shopId,
            @PathVariable("itemId")
            Long itemId
    ) {
        return service.readOne(shopId, itemId);
    }

    @PutMapping("{itemId}")
    public ShopItemDto update(
            @PathVariable("shopId")
            Long shopId,
            @PathVariable("itemId")
            Long itemId,
            @RequestBody
            ShopItemDto dto
    ) {
        return service.update(
                shopId,
                itemId,
                dto
        );
    }

    @PutMapping(
            value = "{itemId}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ShopItemDto updateImage(
            @PathVariable("shopId")
            Long shopId,
            @PathVariable("itemId")
            Long itemId,
            @RequestParam("file")
            MultipartFile file
    ) {
        return service.updateImg(shopId, itemId, file);
    }

    @DeleteMapping("{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable("shopId")
            Long shopId,
            @PathVariable("itemId")
            Long itemId
    ) {
        service.delete(shopId, itemId);
    }
}
