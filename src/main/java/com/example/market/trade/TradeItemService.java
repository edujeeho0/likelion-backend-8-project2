package com.example.market.trade;

import com.example.market.FileHandlerUtils;
import com.example.market.auth.AuthenticationFacade;
import com.example.market.auth.entity.UserEntity;
import com.example.market.trade.dto.TradeItemDto;
import com.example.market.trade.entity.TradeItem;
import com.example.market.trade.repo.TradeItemRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeItemService {
    private final AuthenticationFacade authFacade;
    private final TradeItemRepo tradeItemRepo;
    private final FileHandlerUtils fileHandlerUtils;

    public TradeItemDto create(TradeItemDto dto) {
        UserEntity user = authFacade.extractUser();
        return TradeItemDto.fromEntity(tradeItemRepo.save(TradeItem.builder()
                .user(user)
                .title(dto.getTitle())
                .content(dto.getContent())
                .minPrice(dto.getMinPrice())
                .status(TradeItem.Status.ON_SALE)
                .build()));
    }

    public TradeItemDto readOne(Long id) {
        return tradeItemRepo.findById(id)
                .map(TradeItemDto::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public Page<TradeItemDto> readPage(Pageable pageable) {
        return tradeItemRepo.findAll(pageable)
                .map(TradeItemDto::fromEntity);
    }

    public TradeItemDto update(Long id, TradeItemDto dto) {
        TradeItem tradeItem = tradeItemRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity user = authFacade.extractUser();
        if (!tradeItem.getUser().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        tradeItem.setTitle(dto.getTitle());
        tradeItem.setContent(dto.getContent());
        tradeItem.setMinPrice(dto.getMinPrice());
        return TradeItemDto.fromEntity(tradeItemRepo.save(tradeItem));
    }

    public TradeItemDto updateImg(Long id, MultipartFile file) {
        TradeItem tradeItem = tradeItemRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity user = authFacade.extractUser();
        if (!tradeItem.getUser().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        String requestPath = fileHandlerUtils.saveFile(
                String.format("trades/%d/", tradeItem.getId()),
                "image",
                file
        );

        tradeItem.setImg(requestPath);
        return TradeItemDto.fromEntity(tradeItemRepo.save(tradeItem));
    }

    public void delete(Long id) {
        TradeItem tradeItem = tradeItemRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity user = authFacade.extractUser();
        if (!tradeItem.getUser().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        tradeItemRepo.delete(tradeItem);
    }


}
