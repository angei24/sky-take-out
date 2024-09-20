package com.sky.service;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.enumeration.OperationType;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    //新增菜品
    void saveWithFlavor(DishDTO dishDTO);

    //菜品分页查询
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    //批量删除菜品
    void deleteBatch(List<Long> ids);

    //更新菜品状态
    void updateStatus(Integer status, Long id);

    //根据id查询菜品
    DishVO getDishById(Long id);

    //修改菜品
    void updateDish(DishDTO dishDTO);
}
