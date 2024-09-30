package com.sky.service;

import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;

public interface ReportService {
    //统计制定日期区间的销售额
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

    //统计制定日期区间的用户
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);
}
