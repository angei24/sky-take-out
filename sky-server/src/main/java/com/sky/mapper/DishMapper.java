package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {
    //根据分类id查询菜品数量
    @Select("select count(*) from dish where category_id = #{id}")
    int getCount(Long id);

    //新增菜品
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);

    //菜品分页查询
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    //根据id查询菜品信息
    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    //根据id批量删除菜品
    void deleteByIds(List<Long> ids);
}
