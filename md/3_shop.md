# 쇼핑몰 운영하기

## 쇼핑몰 개설 & 폐쇄

사업자 사용자는 자신의 쇼핑몰을 개설해서 운영할 수 있다. 이때 쇼핑몰 자체는
사업자 사용자로 업그레이드 되면서 자동으로 생성되며, 이후 주인이 정보와 상품을 추가한 뒤
관리자에게 개설 신청을 할 수 있다.

```java
@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepo userRepo;
    private final UserUpgradeRepo userUpgradeRepo;
    private final ShopRepo shopRepo;
    private final ShopOpenReqRepo openRepo;

    // ...
    @Transactional
    public UserUpgradeDto approveUpgrade(Long id) {
        UserUpgrade upgrade = userUpgradeRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        upgrade.setApproved(true);
        upgrade.getTarget().setRoles("ROLE_ACTIVE,ROLE_OWNER");
        shopRepo.save(Shop.builder()
                .owner(upgrade.getTarget())
                .build());
        return UserUpgradeDto.fromEntity(upgrade);
    }
}
```

개설 신청은 따로 `Entity`를 준비하지만, 폐쇄 신청의 경우 한번만 이뤄지는 만큼
따로 `Entity`를 만들지 않고 `Shop`에 추가한다.

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
    private String closeReason;

    @OneToMany(mappedBy = "shop", fetch = FetchType.LAZY)
    private final List<ShopItem> items = new ArrayList<>();
    
    public enum Category {
        FOOD, FASHION, DIGITAL, SPORTS, FURNISHING
    }

    public enum Status {
        PREPARING, OPEN, CLOSED
    }
}
```

## 쇼핑몰 관리

쇼핑몰 관리이지만 실제로는 쇼핑몰의 상품을 관리하는 기능이다. 이는 상품의 CRUD라고 볼 수 있다.
쇼핑몰 상품의 관리는 CRUD이지만, 기본적으로 해당 쇼핑몰의 주인인지를 판단하는 기능이 포함되어야 한다.

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {
    private final AuthenticationFacade authFacade;
    private final ShopRepo shopRepo;
    private final ShopItemRepo itemRepo;
    private final FileHandlerUtils fileHandlerUtils;

    public ShopItemDto create(
            Long shopId,
            ShopItemDto dto
    ) {
        Shop shop = checkOwner(shopId);
        return ShopItemDto.fromEntity(itemRepo.save(ShopItem.builder()
                .shop(shop)
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .build()));
    }

    // ...
    
    private Shop checkOwner(Long id) {
        Shop shop = shopRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity user = authFacade.extractUser();
        if (!shop.getOwner().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return shop;
    }
}
```

또한 데이터 조회시에 쇼핑몰 상태에 따라 데이터가 조회되느냐 안되느냐를 결정할 수 있어야 한다.

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {
    private final AuthenticationFacade authFacade;
    private final ShopRepo shopRepo;
    private final ShopItemRepo itemRepo;
    private final FileHandlerUtils fileHandlerUtils;
    
    // ...

    public Page<ShopItemDto> readPage(
            Long shopId,
            Pageable pageable
    ) {
        checkShopStatus(shopId);
        return itemRepo.findAllByShopId(shopId, pageable)
                .map(ShopItemDto::fromEntity);
    }

    public ShopItemDto readOne(
            Long shopId,
            Long itemId
    ) {
        checkShopStatus(shopId);
        return ShopItemDto.fromEntity(getItem(shopId, itemId));
    }
    
    // ...

    private void checkShopStatus(Long shopId) {
        Shop shop = shopRepo.findById(shopId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity user = authFacade.extractUser();
        if (
                !shop.getStatus().equals(Shop.Status.OPEN) &&
                !shop.getOwner().getId().equals(user.getId())
        )
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    // ...
}
```

## 쇼핑몰 조회

쇼핑몰의 조회는 특정 조건이 붙어있는데, 이를 위해 간단한 JPQL을 작성하였다.

```java
public interface ShopRepo extends JpaRepository<Shop, Long> {
    @Query("SELECT s " +
            "FROM Shop s JOIN s.items i JOIN i.orders o " +
            "WHERE s.status = :status " +
            "ORDER BY o.createdAt DESC ")
    Page<Shop> findAllByStatus(Shop.Status status, Pageable pageable);
}
```

`Shop`을 조회하되 `Shop.items.orders` 중 가장 나중에 만들어진 `Order`를 기준으로 정렬한다.

만약 조건을 추가해서 검색을 한다면, 해당 기능 대신 Querydsl을 사용한 `QShopRepoImpl`을 사용하도록 조정한다.

```java
@Slf4j
@Repository
@RequiredArgsConstructor
public class QShopRepoImpl implements QShopRepo {
    private final JPAQueryFactory queryFactory;
    private final QShop shop = new QShop("s");
    private final QShopItem item = new QShopItem("i");
    private final QShopItemOrder order = new QShopItemOrder("o");


    @Override
    public Page<Shop> searchShops(ShopSearchParams params, Pageable pageable) {
        List<Shop> content = queryFactory
                .selectFrom(shop)
                .join(shop.items, item)
                .leftJoin(item.orders, order)
                .where(
                        nameContains(params.getName()),
                        categoryEquals(params.getCategory())
                )
                .orderBy(order.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        JPAQuery<Long> countQuery = queryFactory
                .select(shop.count())
                .from(shop)
                .where();
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression nameContains(String name) {
        return name == null ? null : shop.name.containsIgnoreCase(name);
    }

    private BooleanExpression categoryEquals(Shop.Category category) {
        return category == null ? null : shop.category.eq(category);
    }
}
```

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {
    private final QItemRepo qItemRepo;
    private final QShopRepo qShopRepo;
    private final ShopService shopService;


    public Page<ShopDto> searchShops(ShopSearchParams params, Pageable pageable) {
        if (params.getName() == null && params.getCategory() == null)
            return shopService.readPage(pageable);
        return qShopRepo.searchShops(params, pageable)
                .map(ShopDto::fromEntity);
    }
    // ...
}
```

## 쇼핑몰 상품 검색

상품 검색은 전체 상품을 기준으로 작동하며, 마찬가지로 Querydsl을 이용해 구현한다.

```java
@Slf4j
@Repository
@RequiredArgsConstructor
public class QItemRepoImpl implements QItemRepo {
    private final JPAQueryFactory queryFactory;
    private final QShopItem item = new QShopItem("i");

    @Override
    public Page<ShopItem> searchItems(ItemSearchParams params, Pageable pageable) {
        List<ShopItem> content = queryFactory
                .selectFrom(item)
                .join(item.shop)
                .fetchJoin()
                .where(
                        nameContains(params.getName()),
                        priceBetween(params.getPriceFloor(), params.getPriceCeil())
                )
                .orderBy(item.price.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        JPAQuery<Long> countQuery = queryFactory
                .select(item.count())
                .from(item)
                .where(

                        nameContains(params.getName()),
                        priceBetween(params.getPriceFloor(), params.getPriceCeil())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression nameContains(String name) {
        return name == null ? null : item.name.containsIgnoreCase(name);
    }

    private BooleanExpression priceBetween(Integer floor, Integer ceil) {
        if (floor == null && ceil == null) return null;
        if (floor == null) return priceCeil(ceil);
        if (ceil == null) return priceFloor(floor);
        return item.price.between(floor, ceil);
    }

    private BooleanExpression priceFloor(Integer value) {
        return value == null ? null : item.price.goe(value);
    }

    private BooleanExpression priceCeil(Integer value) {
        return value == null ? null : item.price.loe(value);
    }
}
```

## 쇼핑몰 상품 구매

구매를 하는 행위는 주문서를 작성하는 CRUD라고 생각할 수 있다. 주문서를 나타내는 `Entity`를 만들고,

```java
@Entity
public class ShopItemOrder extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private ShopItem item;
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity orderUser;
    private String address;
    private Integer count;
    private Integer totalPrice;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Status status = Status.ORDERED;
    private String reason;

    public enum Status {
        ORDERED, ACCEPTED, DECLINED, CANCELED
    }
}

```

이를 위한 CRUD를 진행한다. 단, Update의 경우 중고 거래 상태 갱신 때와 마찬가지로
전달된 상태를 바탕으로 갱신 가능 여부를 판단하여 적용한다.

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    //...
    @Transactional
    public ItemOrderDto updateState(Long orderId, ItemOrderDto dto) {
        ShopItemOrder order = getOrder(orderId);
        if (!order.getStatus().equals(ShopItemOrder.Status.ORDERED))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        UserEntity user = authFacade.extractUser();
        switch (dto.getStatus()) {
            case ORDERED -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            case DECLINED -> {
                if (!order.getItem()
                        .getShop()
                        .getOwner()
                        .getId().equals(user.getId()))
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                if (dto.getReason() == null)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                order.setReason(dto.getReason());
                order.setStatus(dto.getStatus());
            }
            case ACCEPTED -> {
                if (!order.getItem()
                        .getShop()
                        .getOwner()
                        .getId().equals(user.getId()))
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                order.setStatus(dto.getStatus());
                order.getItem().decreaseStock(order.getCount());
            }
            case CANCELED -> {
                if (!order.getOrderUser().getId().equals(user.getId()))
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                if (order.getStatus().equals(ShopItemOrder.Status.ACCEPTED))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                order.setStatus(dto.getStatus());
            }
        }
        return ItemOrderDto.fromEntity(orderRepo.save(order));
    }
}
```
