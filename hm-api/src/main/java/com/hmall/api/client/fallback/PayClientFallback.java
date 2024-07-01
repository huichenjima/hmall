package com.hmall.api.client.fallback;

import com.hmall.api.client.PayClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
public class PayClientFallback implements FallbackFactory<PayClient> {
    @Override
    public PayClient create(Throwable cause) {
        return new PayClient() {
            @Override
            public boolean checkPayStatus( Long orderId) {
                return false;
            }
        };
    }
}