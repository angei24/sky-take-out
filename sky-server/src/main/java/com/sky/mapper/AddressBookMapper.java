package com.sky.mapper;

import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AddressBookMapper {
    //查询当前用户所有地址信息
    List<AddressBook> list(AddressBook addressBook);

    //新增地址信息
    void insert(AddressBook addressBook);

    //根据id查询地址信息
    @Select("select * from address_book where id = #{id}")
    AddressBook getById(Long id);

    //修改地址信息
    void update(AddressBook addressBook);

    //设置用户所有的地址为非默认地址
    @Update("update address_book set is_default = #{isDefault} where user_id = #{userId}")
    void updateByUserId(AddressBook addressBook);

    //根据id删除地址信息
    @Delete("delete from address_book where id = #{id}")
    void deleteById(Long id);
}
