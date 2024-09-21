package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "店铺相关接口")
@RequestMapping("/admin/shop")
@RestController("adminShopController")
public class ShopController {

    private static final String KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

    @ApiOperation("设置店铺状态")
    @PutMapping("/{status}")
    public Result shopStatus(@PathVariable Integer status) {
        log.info("设置店铺状态{}", status);
        redisTemplate.opsForValue().set(KEY, status);
        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("获取店铺状态")
    public Result<Integer> getShopStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        log.info("店铺状态{}", status);
        return Result.success(status);
    }
}
