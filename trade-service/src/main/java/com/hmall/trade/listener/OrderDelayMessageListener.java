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
public class OrderDelayMessageListener {
    private final IOrderService   orderService;
    private final PayClient payClient;
    private final IOrderDetailService orderDetailService;
    private final ItemClient itemClient;



    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.DELAY_ORDER_QUEUE_NAME,durable = "true"),
            exchange = @Exchange(name = MQConstants.DELAY_EXCHANGE_NAME,type = "direct",delayed = "true"),
            key = MQConstants.DELAY_ORDER_KEY
    ))
    public void ListenOrderDelayMessage(Long orderId){

        Order order = orderService.getById(orderId);
        boolean flag = payClient.checkPayStatus(orderId);
        if(order==null||order.getStatus()!=1)
            return;
        // 4.1.已支付，标记订单状态为已支付
        if (flag)
        {
            orderService.markOrderPaySuccess(orderId);
            return;
        }
        //取消订单
        boolean update = orderService.lambdaUpdate().set(Order::getStatus, 5).eq(Order::getId, orderId).update();
        List<OrderDetail> orderDetails = orderDetailService.lambdaQuery().eq(OrderDetail::getOrderId, orderId).list();
        orderDetails.forEach(orderDetail -> {
            orderDetail.setNum(-orderDetail.getNum());


        });
        //恢复库存
        itemClient.deductStock(BeanUtil.copyToList(orderDetails, OrderDetailDTO.class));




    }





}
