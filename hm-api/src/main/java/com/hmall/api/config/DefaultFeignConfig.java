package com.hmall.api.config;

import com.hmall.api.client.fallback.ItemClientFallback;
import com.hmall.api.client.fallback.PayClientFallback;
import com.hmall.common.utils.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;

/**
 * ClassName: DefaultFeignConfig
 * Package: com.hmall.api.config
 * Description:
 *
 * @Author 何琛
 * @Create 2024/6/9 16:03
 * @Version 1.0
 */
public class DefaultFeignConfig {
    @Bean
    public Logger.Level feignLoggerLevel(){
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor userInfoRequestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                Long userId = UserContext.getUser();
                if (userId!=null)
                    requestTemplate.header("user-info",userId.toString());


            }
        };
    }
    @Bean
    public ItemClientFallback itemClientFallback(){
        return new ItemClientFallback();
    }

    @Bean
    public PayClientFallback payClientFallback(){
        return new PayClientFallback();
    }
}
