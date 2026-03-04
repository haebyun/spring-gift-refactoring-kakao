package gift.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gift.product.Product;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class KakaoMessageClient {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public KakaoMessageClient(RestClient.Builder builder, ObjectMapper objectMapper) {
        this.restClient = builder.build();
        this.objectMapper = objectMapper;
    }

    public void sendToMe(String accessToken, Order order, Product product) {
        var templateObject = buildTemplate(order, product);

        var params = new LinkedMultiValueMap<String, String>();
        params.add("template_object", templateObject);

        restClient
                .post()
                .uri("https://kapi.kakao.com/v2/api/talk/memo/default/send")
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(params)
                .retrieve()
                .toBodilessEntity();
    }

    private String buildTemplate(Order order, Product product) {
        var totalPrice = String.format("%,d", order.getOption().calculatePrice(order.getQuantity()));
        var messageSuffix =
                order.getMessage() != null && !order.getMessage().isBlank() ? "\n\n💌 " + order.getMessage() : "";
        var text = "🎁 선물이 도착했어요!\n\n%s (%s)\n수량: %d개\n금액: %s원%s"
                .formatted(
                        product.getName(), order.getOption().getName(), order.getQuantity(), totalPrice, messageSuffix);

        try {
            return objectMapper.writeValueAsString(
                    Map.of("object_type", "text", "text", text, "link", Map.of(), "button_title", "선물 확인하기"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("카카오 메시지 템플릿 직렬화 실패", e);
        }
    }
}
