package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    //统计制定日期区间的销售额
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> list = getDateList(begin, end);
        //查询每天的营业额
        List<Double> amounts = new ArrayList<>();
        for (LocalDate date : list) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map<String, Object> map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double amount = orderMapper.getSumByMao(map);
            if (amount == null)
                amount = 0.0;
            amounts.add(amount);
        }
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(list, ","))
                .turnoverList(StringUtils.join(amounts, ","))
                .build();
    }

    //统计指定日期区间的用户
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //日期区间
        List<LocalDate> dateList = getDateList(begin, end);
        //新增用户
        List<Integer> newUserList = new ArrayList<>();
        //总用户
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map<String, Object> map = new HashMap<>();
            map.put("end", endTime);
            Integer totalUser = userMapper.getUserByMap(map);
            if (totalUser == null)
                totalUser = 0;
            totalUserList.add(totalUser);
            map.put("begin", beginTime);
            Integer newUser = userMapper.getUserByMap(map);
            if (newUser == null)
                newUser = 0;
            newUserList.add(newUser);
        }
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    private List<LocalDate> getDateList(LocalDate begin, LocalDate end) {
        //构造查询日期
        List<LocalDate> list = new ArrayList<>();
        list.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            list.add(begin);
        }
        return list;
    }
}
