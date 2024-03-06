package com.example.market.alert;

import com.example.market.auth.entity.UserEntity;

public interface AlertService {
    void sendValidation(UserEntity user, String validationCode);
    void sendOfferAlert(Long itemId, Integer price);
    void sendOfferAcceptedAlert(Long offerId);
    void sendTradeEndAlert(Long offerId);
    void sendPurchaseAcceptAlert(Long orderId);
}
