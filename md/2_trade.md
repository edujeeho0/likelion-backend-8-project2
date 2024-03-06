# 중고거래 중개하기

기본적인 CRUD 이지만, 상황에 따라 특정 `Entity`를 누가 어떻게 갱신할 수 있는지가
바뀌는 요구사항이다. 각 `Entity`에는 상태를 나타내기 위한 `enum`을 내부에 정의하였다.

```java
@Entity
public class TradeItem extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;
    private String title;
    private String content;
    private String img;
    private Integer minPrice;
    
    @Enumerated(EnumType.STRING)
    private Status status = Status.ON_SALE;
    public enum Status {
        ON_SALE, SOLD
    }
}
```

```java
@Entity
public class TradeOffer extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;
    @ManyToOne(fetch = FetchType.LAZY)
    private TradeItem item;
    
    private Integer offerPrice;
    @Enumerated(EnumType.STRING)
    private Status status = Status.OFFERED;

    public enum Status {
        OFFERED,
        ACCEPTED,
        DECLINED,
        CONFIRMED
    }
}
```

물품 등록의 과정은 간단한 CRUD이다. 이후 구매 제안 등의 상황에 따라 `Status`가 변경되기도 하지만,
직접적은 물품의 상태 변화로는 변경되지 않는다. 누가 등록하는지에 대한 내용은 `AuthenticationFacade`를
이용해 받아올 수 있다.

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class TradeItemService {
    private final AuthenticationFacade authFacade;
    private final TradeItemRepo tradeItemRepo;

    public TradeItemDto create(TradeItemDto dto) {
        UserEntity user = authFacade.extractUser();
        if (dto.getTitle() == null || dto.getContent() == null || dto.getMinPrice() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        return TradeItemDto.fromEntity(tradeItemRepo.save(TradeItem.builder()
                .user(user)
                .title(dto.getTitle())
                .content(dto.getContent())
                .minPrice(dto.getMinPrice())
                .build()));
    }
    // ...
}

```

## 구매 제안

구매 제안을 조회할 때는 사용자가 누구인지에 따라 결과가 다르게 나오게 해야한다.
물품을 등록한 사용자라면 구분없이 모든 제안이 나오게 되며, 그게 아닐 경우 내가 등록한 제안만 나온다.

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class OfferService {
    private final TradeItemRepo itemRepo;
    private final TradeOfferRepo offerRepo;
    private final AuthenticationFacade authFacade;

    // ...
    public Page<TradeOfferDto> readOffers(Long itemId, Pageable pageable) {
        TradeItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity user = authFacade.extractUser();
        if (item.getUser().getId().equals(user.getId()))
            return offerRepo.findAll(pageable)
                    .map(TradeOfferDto::fromEntity);
        else return offerRepo.findAllByItemIdAndUserId(item.getId(), user.getId(), pageable)
                .map(TradeOfferDto::fromEntity);
    }
}
```

제안 갱신 기능에는 수락과 거절등의 기능이 포함되어 있는데, 이를 DTO의 값과 사용자, 현재 제안의 상태 등을 바탕으로 
`switch`문을 활용해 기능을 정의하였다.

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class OfferService {
    private final TradeItemRepo itemRepo;
    private final TradeOfferRepo offerRepo;
    private final AuthenticationFacade authFacade;

    // ...
    @Transactional
    public TradeOfferDto updateOffer(
            Long itemId,
            Long offerId,
            TradeOfferDto dto
    ) {
        TradeItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        TradeOffer offer = offerRepo.findById(offerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!item.getId().equals(offer.getItem().getId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        UserEntity user = authFacade.extractUser();
        TradeOffer.Status status = dto.getStatus();
        switch (status) {
            // 제안된 상태로는 변경 불가능
            case OFFERED -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            case ACCEPTED, DECLINED -> {
                // 물품을 등록한 사용자가 아니면 수락 또는 거절 불가
                if (!user.getId().equals(item.getUser().getId()))
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                // 제안된 제안이 아니면 수락 또는 거절 불가
                if (offer.getStatus() != TradeOffer.Status.OFFERED)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
            case CONFIRMED -> {
                if (!offer.getUser().getId().equals(user.getId()))
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                // 확정시 다른 제안은 전부 거절
                offerRepo.findAllByIdNot(offerId)
                        .forEach(other -> other.setStatus(TradeOffer.Status.DECLINED));
                // 물품은 판매 완료
                item.setStatus(TradeItem.Status.SOLD);
            }
        }
        offer.setStatus(status);
        return TradeOfferDto.fromEntity(offer);
    }
}
```
