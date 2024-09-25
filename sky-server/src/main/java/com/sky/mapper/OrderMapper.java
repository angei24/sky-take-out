package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper {
    //插入订单数据
    void insert(Orders orders);

    //根据订单号查询订单
    @Select("select * from orders where number =  #{orderNumber}")
    Orders getByNumber(String orderNumber);

    //更新订单信息
    void update(Orders orders);
}
