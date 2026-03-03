package gift.fixture;

import gift.member.Member;
import org.springframework.security.crypto.password.PasswordEncoder;

public class MemberFixture {

    public static final String RAW_PASSWORD = "password";

    public static Member 주문회원(PasswordEncoder passwordEncoder) {
        return 회원(10_000_000, passwordEncoder);
    }

    public static Member 회원(int point, PasswordEncoder passwordEncoder) {
        Member member = new Member("member@test.com", RAW_PASSWORD, passwordEncoder);
        member.chargePoint(point);
        return member;
    }
}
