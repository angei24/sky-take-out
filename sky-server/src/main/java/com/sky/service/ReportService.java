package com.sky.service;

import com.sky.vo.TurnoverReportVO;

import java.time.LocalDateTime;

public interface ReportService {
    //统计制定日期区间的销售额
    TurnoverReportVO getTurnoverStatistics(LocalDateTime begin, LocalDateTime end);
}
