package com.hmall.api.client;

import com.hmall.api.dto.OrderDTO;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.*;

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
@FeignClient("trade-service")
public interface TradeClient {
    @PutMapping("/orders")
    public void updateByOrder(@RequestBody OrderDTO orderDTO);


    @PutMapping("/orders/{orderId}")
    void markOrderPaySuccess(@PathVariable("orderId") Long orderId) ;



}
