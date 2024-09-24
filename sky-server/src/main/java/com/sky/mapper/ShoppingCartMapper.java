package com.sky.mapper;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    //添加购物车
    @Insert("insert into shopping_cart(name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time)" +
            "values(#{name}, #{image}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{createTime})")
    void add(ShoppingCart shoppingCart);

    //查询购物车是否含有某个菜品或套餐
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    //修改购物车菜品或套餐的分数份数
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateById(ShoppingCart cart);
}
