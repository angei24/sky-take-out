package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface EmployeeService {

    //员工登录
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    //新增员工
    void save(EmployeeDTO employeeDTO);

    //员工分页查询
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    //修改员工状态
    void employeeStatus(Integer status, Long id);

    //根据id查询员工信息
    Employee getEmployeeById(Long id);

    //更新员工信息
    void updateEmployee(EmployeeDTO employeeDTO);
}
