package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    //新增菜品口味信息
    //@AutoFill(value = OperationType.INSERT)
    void saveBatch(List<DishFlavor> flavors);

    //根据菜品id批量删除菜品口味数据
    void deleteByDishIds(List<Long> dishIds);

    //根据菜品id查询菜品口味信息
    @Select("select * from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> getByDishId(Long dishId);

    //根据菜品id删除口味信息
    @Delete("delete from dish_flavor where dish_id = #{dishId}")
    void deleteByDishId(Long dishId);
}
