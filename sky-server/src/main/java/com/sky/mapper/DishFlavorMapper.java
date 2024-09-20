package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    //新增菜品口味信息
    @AutoFill(value = OperationType.INSERT)
    void saveBatch(List<DishFlavor> flavors);

    //根据菜品id批量删除菜品口味数据
    void deleteByDishIds(List<Long> DishIds);
}
