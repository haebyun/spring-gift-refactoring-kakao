package gift.support;

import gift.category.Category;
import gift.option.Option;
import gift.product.Product;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class TestDataInitializer {

    private final SimpleJdbcInsert memberInsert;
    private final SimpleJdbcInsert categoryInsert;
    private final SimpleJdbcInsert productInsert;
    private final SimpleJdbcInsert optionInsert;
    private final PasswordEncoder passwordEncoder;

    public TestDataInitializer(DataSource dataSource, PasswordEncoder passwordEncoder) {
        this.memberInsert =
                new SimpleJdbcInsert(dataSource).withTableName("member").usingGeneratedKeyColumns("id");
        this.categoryInsert =
                new SimpleJdbcInsert(dataSource).withTableName("category").usingGeneratedKeyColumns("id");
        this.productInsert =
                new SimpleJdbcInsert(dataSource).withTableName("product").usingGeneratedKeyColumns("id");
        this.optionInsert =
                new SimpleJdbcInsert(dataSource).withTableName("options").usingGeneratedKeyColumns("id");
        this.passwordEncoder = passwordEncoder;
    }

    public Long saveMember(String email, String rawPassword, int point) {
        Map<String, Object> params = Map.of(
                "email", email,
                "password", passwordEncoder.encode(rawPassword),
                "point", point);
        return memberInsert.executeAndReturnKey(params).longValue();
    }

    public Long saveCategory(Category category) {
        Map<String, Object> params = Map.of(
                "name", category.getName(),
                "color", category.getColor(),
                "image_url", category.getImageUrl());
        return categoryInsert.executeAndReturnKey(params).longValue();
    }

    public Long saveProduct(Product product, Long categoryId) {
        Map<String, Object> params = Map.of(
                "name", product.getName(),
                "price", product.getPrice(),
                "image_url", product.getImageUrl(),
                "category_id", categoryId);
        return productInsert.executeAndReturnKey(params).longValue();
    }

    public Long saveOption(Option option, Long productId) {
        Map<String, Object> params = Map.of(
                "name", option.getName(),
                "quantity", option.getQuantity(),
                "product_id", productId);
        return optionInsert.executeAndReturnKey(params).longValue();
    }
}
