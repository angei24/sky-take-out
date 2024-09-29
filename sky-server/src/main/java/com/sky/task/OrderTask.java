package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

//自定义定时任务类
@Slf4j
@Component
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    //每5分钟执行一次，处理超时订单
    @Scheduled(cron = "0 0/5 * * * ? ")
    public void processTimeoutOrder() {
        log.info("处理时间{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> TimeoutOrders = orderMapper.getByStatusAndTime(Orders.PENDING_PAYMENT, time);
        if (!TimeoutOrders.isEmpty())
            for (Orders order : TimeoutOrders) {
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("订单超时，自动取消");
                orderMapper.update(order);
            }
    }

    //每天凌晨1点处理昨天派送中的订单
    @Scheduled(cron = "0 0 1 * * ? ")
    public void processDeliveryOrder() {
        log.info("处理时间{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusHours(-1);
        List<Orders> DeliveryOrders = orderMapper.getByStatusAndTime(Orders.DELIVERY_IN_PROGRESS, time);
        if (!DeliveryOrders.isEmpty())
            for (Orders order : DeliveryOrders) {
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            }
    }
}
