package com.example.market.trade;

import com.example.market.trade.dto.TradeItemDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("trades")
@RequiredArgsConstructor
public class TradeItemController {
    private final TradeItemService service;

    @PostMapping
    public TradeItemDto create(
            @RequestBody
            TradeItemDto dto
    ) {
        return service.create(dto);
    }

    @GetMapping
    public Page<TradeItemDto> readPage(
            Pageable pageable
    ) {
        return service.readPage(pageable);
    }

    @GetMapping("{id}")
    public TradeItemDto readOne(
            @PathVariable("id")
            Long id
    ) {
        return service.readOne(id);
    }

    @PutMapping("{id}")
    public TradeItemDto update(
            @PathVariable("id")
            Long id,
            @RequestBody
            TradeItemDto dto
    ) {
        return service.update(id, dto);
    }

    @PutMapping(
            value = "{id}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public TradeItemDto updateImage(
            @PathVariable("id")
            Long id,
            @RequestParam("file")
            MultipartFile file
    ) {
        return service.updateImg(id, file);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable("id")
            Long id
    ) {
        service.delete(id);
    }
}
