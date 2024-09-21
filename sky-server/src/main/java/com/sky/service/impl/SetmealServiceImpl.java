package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    //新增套餐
    @Transactional
    public void save(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //存储套餐基本信息
        setmealMapper.save(setmeal);
        Long setmealId = setmeal.getId();
        //为套餐中的菜品设置关联的套餐id
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(item -> {
            item.setSetmealId(setmealId);
        });
        //保存套餐中的菜品关系
        setmealDishMapper.saveBatch(setmealDishes);
    }

    //分页查询
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        //构建分页查询
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    //批量删除套餐
    @Transactional
    public void deleteByIds(List<Long> ids) {
        //查询是否有起售中的套餐
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            if (setmeal.getStatus() == StatusConstant.ENABLE)
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }
        //删除套餐表中的数据
        setmealMapper.deleteBatch(ids);
        //删除套餐菜品关系表数据
        setmealDishMapper.deleteBySetmealIds(ids);
    }

    //根据id查询套餐
    public SetmealVO getById(Long id) {
        //获取套餐基本信息和套餐菜品信息
        Setmeal setmeal = setmealMapper.getById(id);
        List<SetmealDish> dishes = setmealDishMapper.getSetmealDish(id);
        //为VO对象赋值
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(dishes);
        return setmealVO;
    }

    //修改套餐
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        //更新套餐基本信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);
        //更新套餐菜品信息
        Long id = setmealDTO.getId();
        setmealDishMapper.deleteBySetmealId(id);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(item -> {
            item.setSetmealId(id);
        });
        setmealDishMapper.saveBatch(setmealDishes);
    }

    //修改套餐状态
    public void updateStatus(Integer status, Long id) {
        //起售时判断是否包含停售的菜品
        if (status == StatusConstant.ENABLE) {
            List<Dish> lists = dishMapper.getBySetmealId(id);
            if (lists != null && lists.size() > 0) {
                lists.forEach(item -> {
                    if (item.getStatus() == StatusConstant.DISABLE)
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                });
            }
        }
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
    }
}
