package com.liuhu.socket.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import com.liuhu.socket.entity.MarketInfoNew;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
public class SockerSouhuImportEntity implements Serializable {

    private String  status;
    private List hq;
    private String code;
    private List  stat;

    private List<MarketInfoNew> list;
}
