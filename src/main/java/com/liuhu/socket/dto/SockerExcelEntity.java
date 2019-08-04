package com.liuhu.socket.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SockerExcelEntity extends BaseRowModel {
    @ExcelProperty(index = 0 , value = "日期")
    private String  date;
    @ExcelProperty(index = 1 , value = "股票代码")
    private String shareCode;
    @ExcelProperty(index = 2 , value = "名称")
    private String shareName;
    @ExcelProperty(index = 3 , value = "收盘价")
    private double endValue;
    @ExcelProperty(index = 4 , value = "最高价")
    private double highest;
    @ExcelProperty(index = 5 , value = "最低价")
    private double lowest;
    @ExcelProperty(index = 6 , value = "开盘价")
    private double openValue;
    @ExcelProperty(index = 7 , value = "前收盘")
    private double preEndValue;
    @ExcelProperty(index = 8 , value = "涨跌额")
    private double riseFall;
    @ExcelProperty(index = 9 , value = "涨跌幅")
    private double riseFallRatio;
}
