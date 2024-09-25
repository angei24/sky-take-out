package com.sky.service;

import com.sky.entity.AddressBook;

import java.util.List;

public interface AddressBookService {
    //查询当前用户所有地址信息
    List<AddressBook> list();

    //新增地址信息
    void add(AddressBook addressBook);

    //根据id查询地址信息
    AddressBook getById(Long id);

    //修改地址信息
    void update(AddressBook addressBook);

    //设置默认地址
    void setDefault(AddressBook addressBook);

    //根据id删除地址信息
    void deleteById(Long id);
}
