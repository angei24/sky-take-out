package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    //插入订单数据
    void insert(Orders orders);

    //根据订单号查询订单
    @Select("select * from orders where number =  #{orderNumber}")
    Orders getByNumber(String orderNumber);

    //更新订单信息
    void update(Orders orders);

    //用户历史订单分页查询
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    //管理端订单查询
    Page<Orders> adminPageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    //根据id查询订单
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    //统计各个状态订单数量
    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer status);

    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndTime(Integer status, LocalDateTime orderTime);

    //通过map查询每日营业额
    Double getSumByMao(Map<String, Object> map);
}
