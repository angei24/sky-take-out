package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

public interface CategoryService {
    //分类分页查询
    PageResult categoryPage(CategoryPageQueryDTO categoryPageQueryDTO);

    //新增分类
    void save(CategoryDTO categoryDTO);

    //更新分类状态
    void updateStatus(Integer status, Long id);

    //根据id删除分类
    void deleteById(Long id);

    //修改分类
    void updateCategory(CategoryDTO categoryDTO);

    //根据类型查询分类
    List<Category> list(Integer type);
}
