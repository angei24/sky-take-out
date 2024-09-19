package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DishMapper {
    //根据分类id查询菜品数量
    @Select("select count(*) from dish where category_id = #{id}")
    int getCount(Long id);

    //新增菜品
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);
}
