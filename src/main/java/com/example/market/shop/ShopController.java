package com.example.market.shop;

import com.example.market.shop.dto.ShopDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("shops")
@RequiredArgsConstructor
public class ShopController {
    private final ShopService service;

    @GetMapping
    public Page<ShopDto> readPage(
            Pageable pageable
    ) {
        return service.readPage(pageable);
    }

    @GetMapping("{id}")
    public ShopDto readOne(
            @PathVariable("id")
            Long id
    ) {
        return service.readOne(id);
    }

    @PutMapping("{id}")
    public ShopDto update(
            @PathVariable("id")
            Long id,
            @RequestBody
            ShopDto dto
    ) {
        return service.update(id, dto);
    }

    @PutMapping("{id}/open")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestOpen(
            @PathVariable("id")
            Long id
    ) {
        service.requestOpen(id);
    }

    @PutMapping("{id}/close")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestClose(
            @PathVariable("id")
            Long id,
            @RequestBody
            ShopDto dto
    ) {
        service.requestClose(id, dto);
    }
}
