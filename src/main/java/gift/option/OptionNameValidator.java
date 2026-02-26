package gift.option;

import gift.validation.NameValidator;
import java.util.List;

public class OptionNameValidator {
    private static final int MAX_LENGTH = 50;
    private static final Pattern ALLOWED_PATTERN = Pattern.compile("^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ ()\\[\\]+\\-&/_]*$");

    private OptionNameValidator() {}

    public static List<String> validate(String name) {
        return NameValidator.validate(name, MAX_LENGTH, LABEL);
    }

    public static void validateOrThrow(String name) {
        NameValidator.validateOrThrow(name, MAX_LENGTH, LABEL);
    }
}
