package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    //批量插入订单详细数据
    void insertBatch(List<OrderDetail> detailList);

    //根据订单id查询详细信息
    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> getDetailByOrderId(Long orderId);
}
