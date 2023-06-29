package com.liuhu.socket.enums;


import com.liuhu.socket.common.ResponseService;

public enum ResponseEnum  implements ResponseService {
    SUCCESS("BIMS100","请求成功"),
    ILLEGAL_ARGUMENT("BIMS101","非法参数"),
    FAILED("BIMS140","请求失败"),
    EXCEPTION("BIMS150","请求异常"),
    USER_NOT_LOGIN("BIMS2000","请先登录"),
    NO_VALID_REQUEST("BIMS160","无效的请求"),
    REQ_FREQUENTLY("BIMS163","操作频繁，请稍后再试"),
    INTERNAL_SYSTEM_ERROR("BIMS9000", "系统繁忙，请稍后再试"),
    PARAMETER_IS_NULL("BIMS168", "校验失败, 参数不能为空"),
    QUERY_GOODS_LIST_FAIL_SPUNO_EMPTY("BIMS182", "商品spuNo不能为空"),
    DATA_OVER_SUPPORT_EXPORT_MAX("BIMS185", "数据量超出一次支持最大导出量"),
    IMPORT_EXCEL_NO_MATCH("BIMS187", "Excel模版不匹配"),
    IMPORT_EXCEL_TOO_MUCH("BIMS188", "Excel导入数据不能超过1000条")
    ;

    private String code;
    private String desc;

    ResponseEnum(String code, String desc) {
        this.code = code;
        this.desc =desc;
    }
    public String getCode() {
        return code;
    }

    public String getValue() {
        return desc;
    }

    @Override
    public String getResponseCode() {
        return code;
    }

    @Override
    public String getResponseMessage() {
        return desc;
    }
}
