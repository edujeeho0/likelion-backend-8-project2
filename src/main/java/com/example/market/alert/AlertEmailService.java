package com.example.market.alert;

import com.example.market.auth.entity.UserEntity;
import com.example.market.auth.repo.UserRepo;
import com.example.market.shop.entity.ShopItemOrder;
import com.example.market.shop.repo.ShopItemOrderRepo;
import com.example.market.trade.entity.TradeItem;
import com.example.market.trade.entity.TradeOffer;
import com.example.market.trade.repo.TradeItemRepo;
import com.example.market.trade.repo.TradeOfferRepo;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertEmailService implements AlertService {
    private final UserRepo userRepo;
    private final TradeItemRepo itemRepo;
    private final JavaMailSender emailSender;
    private final TradeOfferRepo offerRepo;
    private final ShopItemOrderRepo orderRepo;

    @Override
    public void sendValidation(UserEntity user, String validationCode) {
        if (!user.getRoles().equals("ROLE_INACTIVE"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        sendMail(
                user.getEmail(),
                "이메일 인증 요청",
                String.format("인증번호: %s", validationCode)
        );
    }

    @Override
    public void sendOfferAlert(Long itemId, Integer price) {
        TradeItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity user = item.getUser();
        sendMail(
                user.getEmail(),
                "구매 제안 등록",
                String.format("물품: %s, 가격: %s", item.getTitle(), price)
        );
    }

    @Override
    public void sendOfferAcceptedAlert(Long offerId) {
        TradeOffer offer = offerRepo.findById(offerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        sendMail(
                offer.getUser().getEmail(),
                "구매 제안 수락",
                String.format("%s에 대한 구매 제안이 수락되었습니다.", offer.getItem().getTitle())
        );
    }

    @Override
    public void sendTradeEndAlert(Long offerId) {
        TradeOffer offer = offerRepo.findById(offerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        sendMail(
                offer.getUser().getEmail(),
                "거래 실패",
                String.format("%s 물품이 다른 사람에 의해 구매되었습니다.", offer.getItem().getTitle())
        );
    }

    @Override
    public void sendPurchaseAcceptAlert(Long orderId) {
        ShopItemOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        sendMail(
                order.getOrderUser().getEmail(),
                "구매가 확정되었습니다.",
                String.format("%s의 구매가 확정되었습니다.", order.getItem().getName())
        );
    }

    private void sendMail(
            String to,
            String subject,
            String text
    ) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(to);
        mailMessage.setSubject(subject);
        mailMessage.setText(text);
        emailSender.send(mailMessage);
    }
}
