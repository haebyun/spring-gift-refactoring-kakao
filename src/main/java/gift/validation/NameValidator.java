package gift.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class NameValidator {
    private static final Pattern ALLOWED_PATTERN = Pattern.compile("^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ ()\\[\\]+\\-&/_]*$");

    private NameValidator() {}

    public static List<String> validate(String name, int maxLength, String label) {
        List<String> errors = new ArrayList<>();

        if (name == null || name.isBlank()) {
            errors.add(label + " 이름은 필수입니다.");
            return errors;
        }

        if (name.length() > maxLength) {
            errors.add(label + " 이름은 공백을 포함하여 최대 " + maxLength + "자까지 입력할 수 있습니다.");
        }

        if (!ALLOWED_PATTERN.matcher(name).matches()) {
            errors.add(label + " 이름에 허용되지 않는 특수 문자가 포함되어 있습니다. 사용 가능: ( ), [ ], +, -, &, /, _");
        }

        return errors;
    }

    public static void validateOrThrow(String name, int maxLength, String label) {
        List<String> errors = validate(name, maxLength, label);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errors));
        }
    }
}
