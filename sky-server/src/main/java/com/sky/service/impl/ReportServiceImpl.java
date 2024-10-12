package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

    //统计指定日期区间的销售额
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
            Double amount = orderMapper.getSumByMap(map);
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

    //统计指定日期区间的订单数
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        //日期区间
        List<LocalDate> dateList = getDateList(begin, end);
        //每日订单数
        List<Integer> orderCountList = new ArrayList<>();
        //每日有效订单数
        List<Integer> validOrderCountList = new ArrayList<>();
        //订单总数和有效订单总数
        Integer totalOrderCount = 0;
        Integer validOrderCount = 0;
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map<String, Object> map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            Integer orderCount = orderMapper.getOrdersCount(map);
            if (orderCount == null)
                orderCount = 0;
            totalOrderCount += orderCount;
            orderCountList.add(orderCount);
            map.put("status", Orders.COMPLETED);
            Integer validOrder =  orderMapper.getOrdersCount(map);
            if (validOrder == null)
                validOrder = 0;
            validOrderCount += validOrder;
            validOrderCountList.add(validOrder);
        }
//        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
//        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0)
            orderCompletionRate = validOrderCount.doubleValue()/totalOrderCount.doubleValue();

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    //统计指定日期区间销量前10
    public SalesTop10ReportVO getTop10Statistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
        //取出name和numbers
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        //封装
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(names, ","))
                .numberList(StringUtils.join(numbers, ","))
                .build();
    }

    //导出统计报表
    public void export(HttpServletResponse response) throws IOException {
        //统计的时间
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        //调用
        BusinessDataVO businessData = workspaceService.getBusinessData(beginTime, endTime);
        //创建输入流
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        //POI操作excel
        XSSFWorkbook excel = new XSSFWorkbook(in);
        //获取标签页
        XSSFSheet sheet = excel.getSheetAt(0);
        //设置单元格的值
        sheet.getRow(1).getCell(1).setCellValue("时间："+begin+"至"+end);
        XSSFRow row = sheet.getRow(3);
        row.getCell(2).setCellValue(businessData.getTurnover());
        row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
        row.getCell(6).setCellValue(businessData.getNewUsers());
        row = sheet.getRow(4);
        row.getCell(2).setCellValue(businessData.getValidOrderCount());
        row.getCell(4).setCellValue(businessData.getUnitPrice());
        //循环写入数据
        for (int i = 0; i < 30; i++) {
            LocalDate date = begin.plusDays(i);
            //查询每天的数据
            BusinessDataVO dataVO = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
            row = sheet.getRow(i+7);
            row.getCell(1).setCellValue(date.toString());
            row.getCell(2).setCellValue(dataVO.getTurnover());
            row.getCell(3).setCellValue(dataVO.getValidOrderCount());
            row.getCell(4).setCellValue(dataVO.getOrderCompletionRate());
            row.getCell(5).setCellValue(dataVO.getUnitPrice());
            row.getCell(6).setCellValue(dataVO.getNewUsers());
        }
        //输出流保存文件
        ServletOutputStream out = response.getOutputStream();
        excel.write(out);
        //关闭资源
        out.close();
        excel.close();
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
