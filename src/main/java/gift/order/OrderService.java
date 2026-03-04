package gift.order;

import gift.member.Member;
import gift.member.MemberRepository;
import gift.option.Option;
import gift.option.OptionRepository;
import gift.wish.WishRepository;
import java.util.NoSuchElementException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OptionRepository optionRepository;
    private final MemberRepository memberRepository;
    private final WishRepository wishRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(
            OrderRepository orderRepository,
            OptionRepository optionRepository,
            MemberRepository memberRepository,
            WishRepository wishRepository,
            ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.optionRepository = optionRepository;
        this.memberRepository = memberRepository;
        this.wishRepository = wishRepository;
        this.eventPublisher = eventPublisher;
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
        publishOrderCompletedEvent(member, saved, option);

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
        member.deductPoint(option.calculatePrice(quantity));
        memberRepository.save(member);
    }

    private void cleanupWish(Long memberId, Option option) {
        wishRepository
                .findByMemberIdAndProductId(memberId, option.getProductId())
                .ifPresent(wishRepository::delete);
    }

    private void publishOrderCompletedEvent(Member member, Order order, Option option) {
        if (!member.hasKakaoIntegration()) {
            return;
        }
        eventPublisher.publishEvent(new OrderCompletedEvent(member.getKakaoAccessToken(), order, option.getProduct()));
    }
}
