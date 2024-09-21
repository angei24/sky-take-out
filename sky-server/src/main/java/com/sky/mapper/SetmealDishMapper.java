package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    //通过菜品id获取套餐id
    List<Long> getSetmealIdByDishId(List<Long> dishIds);

    //新增套餐中的菜品
    void saveBatch(List<SetmealDish> setmealDishes);

    //删除套餐菜品关系表
    void deleteBySetmealIds(List<Long> setmealIds);

    //获取套餐中的菜品
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getSetmealDish(Long setmealId);

    //删除套餐中关联的菜品信息
    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);
}
