# Toss Payments

사용자가 물품을 구매할 때 토스페이먼츠를 이용해 결제가 이뤄지도록 조정한다.
토스페이먼츠 결제를 위해 간단한 HTML이 필요한데, 여기서는 이전에 사용했던 `item.html`과 `success.html`을 이용한다.

`item.html`에는 Bearer Token을 넣기 위한 `input` 요소가 있으며,

```html
<!-- 추가 과제: 인증 붙이기 -->
<div class="mb-3">
  <label for="bearer-input" class="form-label">Bearer Token</label>
  <input type="text" class="form-control" id="bearer-input" value="No Token">
</div>
```

여기에 Bearer Token을 넣으면 결제 후 백엔드로 누가 결제했는지가 같이 전달된다.

```js
  paymentRequestButton.addEventListener('click', () => {
  paymentWidget.requestPayment({
    orderId: crypto.randomUUID().replaceAll('-', ''),
    orderName: `${itemId}-${document.getElementById('item-name').innerText}`,
  }).then(async data => {
    const response = await fetch('/toss/confirm-payment', {
      method: 'post',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + document.getElementById('bearer-input').value,
      }
      // ...
      })
    })
  })
```

일반적인 상품 조회 상황에서는 JWT가 필요하지만, `item.html`에서 직접 물품 정보를 조회할때는 JWT를 포함할 방법을 다루지 않았기 때문에,
결제 테스트를 위한 상품 조회 엔드포인트를 추가한다.

```java
@Slf4j
@Controller
@RequiredArgsConstructor
public class TossController {
    private final TossService service;
    private final ShopItemRepo itemRepo;

    // 토스 결제 전용 Endpoint
    @GetMapping("/items/{id}")
    @ResponseBody
    public ShopItemDto readOne(
            @PathVariable("id")
            Long id
    ) {
        return itemRepo.findById(id)
                .map(ShopItemDto::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
```

토스페이먼츠 API에 결제 승인 요청을 보내는 부분은 `RestClient`를 기반으로 하는 HTTP Interface Client를 만들어 사용한다.
```java
@HttpExchange("/payments")
public interface TossHttpService {
    @PostExchange("/confirm")
    PaymentDto confirmPayment(
            @RequestBody
            PaymentConfirmDto dto
    );

    @GetExchange("/{paymentKey}")
    PaymentDto getPayment(
            @PathVariable("paymentKey")
            String paymentKey
    );

    @PostExchange("/{paymentKey}/cancel")
    PaymentDto cancelPayment(
            @PathVariable("paymentKey")
            String paymentKey,
            @RequestBody
            PaymentCancelDto dto
    );
}
```

```java
@Slf4j
@Configuration
public class TossConfig {
    @Value("${toss.secret}")
    private String tossSecret;

    @Bean
    public RestClient tossClient() {
        String basicAuth = Base64.getEncoder().encodeToString((tossSecret + ":").getBytes());
        return RestClient
                .builder()
                .baseUrl("https://api.tosspayments.com/v1")
                .defaultHeader("Authorization", String.format("Basic %s", basicAuth))
                .defaultStatusHandler(
                        HttpStatusCode::isError,
                        (request, response) -> {
                            log.info("{}", response.getStatusCode());
                            log.info(new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8));
                })
                .build();
    }

    @Bean
    public TossHttpService httpService() {
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(tossClient()))
                .build()
                .createClient(TossHttpService.class);
    }
}
```

결제가 승인되는 시점에 `ShopItemOrder`를 생성한다. 이때, 주문의 상세한 정보는 `orderName`의 정보와 총 결제 금액을 바탕으로 유추한다.
실제 상황에서는 프런트에서 정보를 보내주는 것이 더 바람직하다.

```java

@Slf4j
@Service
@RequiredArgsConstructor
public class TossService {
    private final AuthenticationFacade authFacade;
    private final TossHttpService httpService;
    private final ShopItemRepo itemRepo;
    private final ShopItemOrderRepo orderRepo;

    @Transactional
    public Object confirmPayment(PaymentConfirmDto dto) {
        PaymentDto tossPayment = httpService.getPayment(dto.getPaymentKey());
        String orderName = tossPayment.getOrderName();
        Long itemId = Long.parseLong(orderName.split("-")[0]);
        ShopItem item = getItem(itemId);
        if (tossPayment.getTotalAmount() % item.getPrice() != 0)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        int count = tossPayment.getTotalAmount() / item.getPrice();
        UserEntity user = authFacade.extractUser();
        item.decreaseStock(count);
        ItemOrderDto response = ItemOrderDto.fromEntity(orderRepo.save(ShopItemOrder.builder()
                .item(item)
                .orderUser(user)
                .count(count)
                .totalPrice(tossPayment.getTotalAmount())
                .paymentKey(tossPayment.getPaymentKey())
                .build()));
        httpService.confirmPayment(dto);
        return response;
    }
}
```

이후 구매 요청 거절, 취소시에 결제를 취소하는 기능을 추가한다.

```java

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    
    // ...

    @Transactional
    public ItemOrderDto updateState(Long orderId, ItemOrderDto dto) {
        ShopItemOrder order = getOrder(orderId);
        if (!order.getStatus().equals(ShopItemOrder.Status.ORDERED))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        UserEntity user = authFacade.extractUser();
        switch (dto.getStatus()) {
            // ...
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
                tossHttpService.cancelPayment(
                        order.getPaymentKey(),
                        PaymentCancelDto.builder()
                                .cancelReason(String.format("DECLINED: %s", dto.getReason()))
                                .build()
                );
            }
            // ...
            case CANCELED -> {
                if (!order.getOrderUser().getId().equals(user.getId()))
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                if (order.getStatus().equals(ShopItemOrder.Status.ACCEPTED))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                order.setStatus(dto.getStatus());
                tossHttpService.cancelPayment(
                        order.getPaymentKey(),
                        PaymentCancelDto.builder()
                                .cancelReason(String.format("CANCELED: %s", dto.getReason()))
                                .build()
                );
            }
        }
        return ItemOrderDto.fromEntity(orderRepo.save(order));
    }


    private ShopItemOrder getOrder(Long orderId) {
        ShopItemOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity user = authFacade.extractUser();
        if (!order.getOrderUser()
                .getId().equals(user.getId()) &&
            !order.getItem()
                    .getShop()
                    .getOwner()
                    .getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return order;
    }
}

```
