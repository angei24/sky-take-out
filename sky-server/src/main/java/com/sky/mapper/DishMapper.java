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
import java.util.Map;

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

    //更新菜品信息
    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    //根据分类id查询菜品
    @Select("select * from dish where category_id = #{categoryId} and status = #{status} order by create_time desc")
    List<Dish> list(Dish dish);

    //获取套餐中的菜品
    @Select("select d.* from dish d left join setmeal_dish sd on d.id = sd.dish_id where sd.setmeal_id = #{setmealId}")
    List<Dish> getBySetmealId(Long setmealId);
    /**
     * 根据条件统计菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
