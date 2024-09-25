package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService {

    @Autowired
    private AddressBookMapper addressBookMapper;

    //查询当前用户所有地址信息
    public List<AddressBook> list() {
        AddressBook addressBook = new AddressBook();
        addressBook.setUserId(BaseContext.getCurrentId());
        return addressBookMapper.list(addressBook);
    }

    //新增地址信息
    public void add(AddressBook addressBook) {
        addressBook.setIsDefault(0);
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookMapper.insert(addressBook);
    }

    //根据id查询地址信息
    public AddressBook getById(Long id) {
        return addressBookMapper.getById(id);
    }

    //修改地址信息
    public void update(AddressBook addressBook) {
        addressBookMapper.update(addressBook);
    }

    //设置默认地址
    public void setDefault(AddressBook addressBook) {
        //设置该用户所有地址为非默认地址
        addressBook.setIsDefault(0);
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookMapper.updateByUserId(addressBook);
        //设置改地址为默认地址
        addressBook.setIsDefault(1);
        addressBookMapper.update(addressBook);
    }

    //根据id删除地址信息
    public void deleteById(Long id) {
        addressBookMapper.deleteById(id);
    }
}
