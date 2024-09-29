package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(tags = "订单相关接口")
@RequestMapping("/admin/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @ApiOperation("订单搜索")
    @GetMapping("/conditionSearch")
    public Result<PageResult> orderSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageResult page = orderService.orderSearch(ordersPageQueryDTO);
        return Result.success(page);
    }

    @GetMapping("/statistics")
    @ApiOperation("统计各个状态的订单数量")
    public Result<OrderStatisticsVO> orderStatistics() {
        OrderStatisticsVO statisticsVO = orderService.orderStatistics();
        return Result.success(statisticsVO);
    }

    @ApiOperation("查询订单详细信息")
    @GetMapping("/details/{id}")
    public Result<OrderVO> getDetails(@PathVariable Long id) {
        OrderVO details = orderService.getDetails(id);
        return Result.success(details);
    }

    @ApiOperation("接单")
    @PutMapping("/confirm")
    public Result confirmOrder(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    @ApiOperation("拒单")
    @PutMapping("/rejection")
    public Result rejectOrder(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }

    @ApiOperation("取消订单")
    @PutMapping("/cancel")
    public Result cancelOrder(@RequestBody OrdersCancelDTO ordersCancelDTO) throws Exception {
        orderService.adminCancelOrder(ordersCancelDTO);
        return Result.success();
    }

    @ApiOperation("派送订单")
    @PutMapping("/delivery/{id}")
    public Result deliveryOrder(@PathVariable Long id) {
        orderService.delivery(id);
        return Result.success();
    }

    @ApiOperation("完成订单")
    @PutMapping("/complete/{id}")
    public Result completeOrder(@PathVariable Long id) {
        orderService.complete(id);
        return Result.success();
    }
}
