package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    //新增菜品
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        //保存数据到菜品表
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);
        //获取dish的id
        Long dishId = dish.getId();
        //保存数据到菜品口味表
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            //设置菜品口味的菜品id
            flavors.forEach(item -> {
                item.setDishId(dishId);
            });
            dishFlavorMapper.saveBatch(flavors);
        }
        //清理该分类的缓存
        String key = "dish_" + dishDTO.getCategoryId();
        deleteRedis(key);
    }

    //菜品分页查询
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    //批量删除菜品
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //菜品状态是否起售
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            //起售中的菜品不能删除
            if (dish.getStatus() == StatusConstant.ENABLE)
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
        }
        //菜品是否被套餐关联
        List<Long> setmealids = setmealDishMapper.getSetmealIdByDishId(ids);
        if (setmealids != null && setmealids.size() > 0)
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        //删除菜品
        dishMapper.deleteByIds(ids);
        //删除菜品关联的口味
        dishFlavorMapper.deleteByDishIds(ids);
        //清理所有菜品缓存
        deleteRedis("dish_*");
    }

    //更新菜品状态
    public void updateStatus(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);
        //清理菜品缓存
        deleteRedis("dish_*");
    }

    //根据id查询菜品
    public DishVO getDishById(Long id) {
        //查询菜品表
        Dish dish = dishMapper.getById(id);
        //查询菜品口味表
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
        //封装到VO
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    //修改菜品
    @Transactional
    public void updateDish(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //修改菜品基本信息
        dishMapper.update(dish);
        //修改菜品口味信息
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(item -> {
                item.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.saveBatch(flavors);
        }
        //清理菜品缓存
        deleteRedis("dish_*");
    }

    //根据分类id查询菜品
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        List<Dish> list = dishMapper.list(dish);
        return list;
    }

    //获取菜品口味信息
    public List<DishVO> listWithFlavor(Dish dish) {
        //构造key查询redis
        String key = "dish_" + dish.getCategoryId();
        List<DishVO> dishVOList = (List<DishVO>) redisTemplate.opsForValue().get(key);
        if (dishVOList != null && dishVOList.size() > 0) {
            return dishVOList;
        }
        List<Dish> list = dishMapper.list(dish);
        dishVOList = new ArrayList<DishVO>();
        for (Dish d : list) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d, dishVO);
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());
            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }
        redisTemplate.opsForValue().set(key, dishVOList);
        return dishVOList;
    }

    //清理缓存数据
    private void deleteRedis(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
