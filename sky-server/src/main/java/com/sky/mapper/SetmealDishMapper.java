package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    //通过菜品id获取套餐id
    List<Long> getSetmealIdByDishId(List<Long> dishIds);
}