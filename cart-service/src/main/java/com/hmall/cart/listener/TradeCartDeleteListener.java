package com.hmall.cart.listener;

import com.hmall.cart.service.ICartService;
import com.hmall.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ClassName: CartDeleteListener
 * Package: com.hmall.cart.listener
 * Description:
 *
 * @Author 何琛
 * @Create 2024/6/14 21:10
 * @Version 1.0
 */
@Component
@RequiredArgsConstructor
public class TradeCartDeleteListener {
    private final ICartService cartService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("cart.clear.queue"),
            exchange = @Exchange(name = "trade.topic",type = "topic"),
            key="order.create"
    ))
    public void ListenTradeCartDelete(HashMap map){
//        Set<Long> itemIds = (Set<Long>)map.get("itemIds");
//        Set<Object> itemIds = (Set<Object>)map.get("itemIds");
        Collection<?> rawItemIds = (Collection<?>) map.get("itemIds");
        if (rawItemIds != null) {
            // 使用stream将其转换为Long列表
            List<Long> itemIds = rawItemIds.stream()
                    .map(Object::toString)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            // 处理用户ID
            Object userId = map.get("userId");
            UserContext.setUser(((Integer) userId).longValue());
            // 调用cartService处理这些ID
            cartService.removeByItemIds(itemIds);
        }
        else
            throw new RuntimeException("购物车异常");


    }

}
