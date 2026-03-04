package gift.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderCompletedEventListener {
    private static final Logger log = LoggerFactory.getLogger(OrderCompletedEventListener.class);

    private final OrderMessageClient kakaoMessageClient;

    public OrderCompletedEventListener(OrderMessageClient kakaoMessageClient) {
        this.kakaoMessageClient = kakaoMessageClient;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OrderCompletedEvent event) {
        try {
            kakaoMessageClient.sendToMe(event.kakaoAccessToken(), event.order(), event.product());
        } catch (Exception e) {
            log.warn("카카오 메시지 전송 실패: orderId={}", event.order().getId(), e);
        }
    }
}
