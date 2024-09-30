package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {
    //通过openid查询用户
    @Select("select * from user where openid = #{openid}")
    User getByOpenId(String openid);

    //保存新用户
    void save(User user);

    //根据用户id获取用户信息
    @Select("select * from user where id = #{Id}")
    User getById(Long Id);

    //用户统计
    Integer getUserByMap(Map<String, Object> map);
}
