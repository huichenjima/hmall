package com.hmall.api.client;

import com.hmall.api.client.fallback.ItemClientFallback;
import com.hmall.api.config.DefaultFeignConfig;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.api.query.ItemPageQuery;
import com.hmall.common.domain.PageDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

/**
 * ClassName: ItemClient
 * Package: com.hmall.cart.client
 * Description:
 *
 * @Author 何琛
 * @Create 2024/6/9 14:49
 * @Version 1.0
 */
@FeignClient(value = "item-service",configuration = DefaultFeignConfig.class,fallbackFactory = ItemClientFallback.class)
public interface ItemClient {
    @GetMapping("/items")
    List<ItemDTO> queryItemById(@RequestParam("ids") Collection<Long> ids);

    @PutMapping("/items/stock/deduct")
    void deductStock(@RequestBody List<OrderDetailDTO> items);


    @GetMapping("/items/list")
    PageDTO<ItemDTO> search(ItemPageQuery query);
}
