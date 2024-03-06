package com.example.market.alert;

public interface AlertService {
    void sendValidation(Long userId, String validationCode);
    void sendOfferAlert(Long itemId, Integer price);
    void sendOfferAcceptedAlert(Long offerId);
    void sendTradeEndAlert(Long offerId);
    void sendPurchaseAcceptAlert(Long orderId);
}
