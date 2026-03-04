package gift.order;

import gift.member.Member;
import gift.member.MemberRepository;
import gift.option.Option;
import gift.option.OptionRepository;
import gift.wish.WishRepository;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OptionRepository optionRepository;
    private final MemberRepository memberRepository;
    private final WishRepository wishRepository;
    private final KakaoMessageClient kakaoMessageClient;

    public OrderService(
            OrderRepository orderRepository,
            OptionRepository optionRepository,
            MemberRepository memberRepository,
            WishRepository wishRepository,
            KakaoMessageClient kakaoMessageClient) {
        this.orderRepository = orderRepository;
        this.optionRepository = optionRepository;
        this.memberRepository = memberRepository;
        this.wishRepository = wishRepository;
        this.kakaoMessageClient = kakaoMessageClient;
    }

    public Page<Order> findByMemberId(Long memberId, Pageable pageable) {
        return orderRepository.findByMemberId(memberId, pageable);
    }

    @Transactional
    public Order createOrder(Long memberId, Long optionId, int quantity, String message) {
        Option option = findOption(optionId);
        Member member = findMember(memberId);

        subtractStock(option, quantity);
        deductPayment(member, option, quantity);

        Order saved = orderRepository.save(new Order(option, memberId, quantity, message));
        cleanupWish(memberId, option);
        sendKakaoMessageIfPossible(member, saved, option);

        return saved;
    }

    private Option findOption(Long optionId) {
        return optionRepository
                .findById(optionId)
                .orElseThrow(() -> new NoSuchElementException("옵션이 존재하지 않습니다. id=" + optionId));
    }

    private Member findMember(Long memberId) {
        return memberRepository
                .findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("회원이 존재하지 않습니다. id=" + memberId));
    }

    private void subtractStock(Option option, int quantity) {
        option.subtractQuantity(quantity);
        optionRepository.save(option);
    }

    private void deductPayment(Member member, Option option, int quantity) {
        int price = option.getProduct().getPrice() * quantity;
        member.deductPoint(price);
        memberRepository.save(member);
    }

    private void cleanupWish(Long memberId, Option option) {
        wishRepository
                .findByMemberIdAndProductId(memberId, option.getProduct().getId())
                .ifPresent(wishRepository::delete);
    }

    private void sendKakaoMessageIfPossible(Member member, Order order, Option option) {
        if (member.getKakaoAccessToken() == null) {
            return;
        }
        try {
            var product = option.getProduct();
            kakaoMessageClient.sendToMe(member.getKakaoAccessToken(), order, product);
        } catch (Exception e) {
            log.warn("카카오 메시지 전송 실패: memberId={}, orderId={}", member.getId(), order.getId(), e);
        }
    }
}
