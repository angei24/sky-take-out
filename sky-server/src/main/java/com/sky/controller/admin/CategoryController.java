package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@Api(tags = "分类管理")
@RequestMapping("/admin/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分类分页查询")
    public Result<PageResult> categoryPage(CategoryPageQueryDTO categoryPageQueryDTO) {
        log.info("分类分页查询{}", categoryPageQueryDTO);
        PageResult page = categoryService.categoryPage(categoryPageQueryDTO);
        return Result.success(page);
    }

    /**
     * 新增分类
     * @param categoryDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增分类")
    public Result saveCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("新增分类{}", categoryDTO);
        categoryService.save(categoryDTO);
        return Result.success();
    }

    /**
     * 更新分类状态
     * @param status
     * @param id
     * @return
     */
    @ApiOperation("更新分类状态")
    @PostMapping("/status/{status}")
    public Result updateStatus(@PathVariable Integer status, Long id) {
        log.info("分类状态和id{},{}", status, id);
        categoryService.updateStatus(status, id);
        return Result.success();
    }

    /**
     * 根据id删除分类
     * @param id
     * @return
     */
    @DeleteMapping
    @ApiOperation("根据id删除分类")
    public Result deleteCategoryById(Long id) {
        log.info("删除分类id{}", id);
        categoryService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改分类
     * @param categoryDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改分类")
    public Result updateCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("修改分类{}", categoryDTO);
        categoryService.updateCategory(categoryDTO);
        return Result.success();
    }

    /**
     * 根据类型查询分类
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据类型查询分类")
    public Result<List<Category>> list(Integer type) {
        log.info("分类类型{}", type);
        List<Category> list = categoryService.list(type);
        return Result.success(list);
    }
}
