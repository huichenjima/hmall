package com.hmall.cart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ClassName: CartProperties
 * Package: com.hmall.cart.config
 * Description:
 *
 * @Author 何琛
 * @Create 2024/6/10 20:19
 * @Version 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "hm.cart")
public class CartProperties {
    private Integer maxItems;
}
