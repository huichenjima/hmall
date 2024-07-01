package com.hmall.trade.listener;

import cn.hutool.core.bean.BeanUtil;
import com.hmall.api.client.ItemClient;
import com.hmall.api.client.PayClient;
import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.trade.constants.MQConstants;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.domain.po.OrderDetail;
import com.hmall.trade.service.IOrderDetailService;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ClassName: PayStatusListener
 * Package: com.hmall.trade.llistener
 * Description:
 *
 * @Author 何琛
 * @Create 2024/6/14 12:32
 * @Version 1.0
 */
@Component
@RequiredArgsConstructor
public class PayStatusListener {
    private final IOrderService   orderService;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "trade.pay.success.queue",durable = "true"),
            exchange = @Exchange(name = "pay.direct",type = "direct"),
            key = "pay.success"
    ))
    public void ListenPaySuccess(Long orderId){

        Order order = orderService.getById(orderId);
        if(order==null||order.getStatus()!=1){
            return;
        }

        orderService.markOrderPaySuccess(orderId);


    }







}
