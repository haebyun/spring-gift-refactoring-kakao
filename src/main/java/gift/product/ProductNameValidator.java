package gift.product;

import gift.validation.NameValidator;
import java.util.ArrayList;
import java.util.List;

public class ProductNameValidator {
    private static final int MAX_LENGTH = 15;
    private static final Pattern ALLOWED_PATTERN = Pattern.compile("^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ ()\\[\\]+\\-&/_]*$");

    private ProductNameValidator() {}

    public static List<String> validate(String name) {
        return validate(name, false);
    }

    public static List<String> validate(String name, boolean allowKakao) {
        List<String> errors = new ArrayList<>(NameValidator.validate(name, MAX_LENGTH, LABEL));

        if (!allowKakao && name != null && name.contains("카카오")) {
            errors.add("\"카카오\"가 포함된 상품명은 담당 MD와 협의한 경우에만 사용할 수 있습니다.");
        }

        return errors;
    }

    public static void validateOrThrow(String name, boolean allowKakao) {
        List<String> errors = validate(name, allowKakao);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errors));
        }
    }
}
