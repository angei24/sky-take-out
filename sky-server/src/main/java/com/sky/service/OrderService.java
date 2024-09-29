package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    //提交订单
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    //订单支付
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    //支付成功，修改订单状态
    void paySuccess(String outTradeNo);

    //用户历史订单查询
    PageResult pageQuery(Integer page, Integer pageSize, Integer status);

    //根据id查询订单详细信息
    OrderVO getDetails(Long id);

    //取消订单
    void userCancelOrder(Long id) throws Exception;

    //再来一单
    void repetition(Long id);

    //订单查询
    PageResult orderSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    //统计各个订单数量
    OrderStatisticsVO orderStatistics();

    //接单
    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    //拒单
    void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception;

    //取消订单
    void adminCancelOrder(OrdersCancelDTO ordersCancelDTO) throws Exception;

    //派送订单
    void delivery(Long id);

    //完成订单
    void complete(Long id);

    //用户催单
    void reminder(Long id);
}
