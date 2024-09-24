package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    //新增套餐
    void save(SetmealDTO setmealDTO);

    //分页查询
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    //批量删除套餐
    void deleteByIds(List<Long> ids);

    //根据id查询套餐
    SetmealVO getById(Long id);

    //修改套餐
    void update(SetmealDTO setmealDTO);

    //修改套餐状态
    void updateStatus(Integer status, Long id);

    //根据分类id查询套餐
    List<Setmeal> list(Setmeal setmeal);

    //根据id查询套餐中的菜品
    List<DishItemVO> getDishItemById(Long id);
}
