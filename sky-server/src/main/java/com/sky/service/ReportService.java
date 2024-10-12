package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

public interface ReportService {
    //统计指定日期区间的销售额
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

    //统计指定日期区间的用户
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    //统计指定日期区间的订单
    OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end);

    //统计指定日期区间销量前10
    SalesTop10ReportVO getTop10Statistics(LocalDate begin, LocalDate end);

    //导出统计报表
    void export(HttpServletResponse response) throws IOException;
}
