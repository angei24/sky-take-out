package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {
    //根据分类id查询套餐数量
    @Select("select count(*) from setmeal where category_id = #{id}")
    int getCount(Long id);

    //新增套餐
    @AutoFill(value = OperationType.INSERT)
    void save(Setmeal setmeal);

    //分页查询
    Page<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    //查询是否有起售中的套餐，根据id查询套餐
    @Select("select * from setmeal where id = #{id}")
    Setmeal getById(Long id);

    //删除套餐表信息
    void deleteBatch(List<Long> ids);

    //修改套餐基本信息
    @AutoFill(value = OperationType.UPDATE)
    void update(Setmeal setmeal);

    //根据分类id查询套餐
    List<Setmeal> list(Setmeal setmeal);

    //根据套餐id查询套餐菜品
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);
}
