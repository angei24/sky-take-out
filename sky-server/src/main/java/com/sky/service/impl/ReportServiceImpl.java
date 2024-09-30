package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
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

    //统计制定日期区间的销售额
    public TurnoverReportVO getTurnoverStatistics(LocalDateTime begin, LocalDateTime end) {
        //构造查询日期
        List<LocalDateTime> list = new ArrayList<>();
        list.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            list.add(begin);
        }
        //查询每天的营业额
        List<Double> amounts = new ArrayList<>();
        for (LocalDateTime date : list) {
            LocalDateTime beginTime = LocalDateTime.of(LocalDate.from(date), LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(LocalDate.from(date), LocalTime.MAX);
            Map map = new HashMap();
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
}
