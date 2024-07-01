package com.hmall.api.client;

import com.hmall.api.client.fallback.PayClientFallback;
import com.hmall.api.config.DefaultFeignConfig;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;

/**
 * ClassName: CartClient
 * Package: com.hmall.api.client
 * Description:
 *
 * @Author 何琛
 * @Create 2024/6/9 17:40
 * @Version 1.0
 */
@FeignClient(value="pay-service",configuration = DefaultFeignConfig.class,fallbackFactory = PayClientFallback.class)
public interface PayClient {


    @GetMapping("pay-orders")
    boolean checkPayStatus(@RequestParam("orderId") Long orderId);


}
