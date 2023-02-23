package com.icplaza.mapping.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Slf4j
@ApiModel("返回对象")
public class ResponseCommon<T> {
    public static final Integer STATUS_SUCCESS = HttpStatus.OK.value();

    public static final Integer STATUS_FAILURE = HttpStatus.INTERNAL_SERVER_ERROR.value();

    public static final String CODE_SUCCESS = "SUCCESS";

    public static final String CODE_FAILURE = "FAILURE";
    /**
     * 用于兼容http状态码, 一般在业务上不使用
     */
    @ApiModelProperty("状态码")
    protected Integer status;

    /**
     * 业务操作完成后的返回代码
     */
    @ApiModelProperty("代码")
    protected String code = CODE_SUCCESS;

    /**
     * 业务操作完成后的返回信息
     */
    @ApiModelProperty("信息")
    protected String message;

    /**
     * 携带的负载返回对象
     */
    @ApiModelProperty("消息体")
    protected T payload;


    public ResponseCommon(Integer status, String code, T payload, String message) {
        this.status = status;
        this.code = code;
        this.payload = payload;
        this.message = message;
    }

    public static <T> ResponseCommon<T> success(T payload) {
        return new ResponseCommon<T>(STATUS_SUCCESS, CODE_SUCCESS, payload, null);
    }

    public static <T> ResponseCommon<T> success(T payload, String message) {
        return new ResponseCommon<T>(STATUS_SUCCESS, CODE_SUCCESS, payload, message);
    }

    public static <T> ResponseCommon<T> fail(String message) {
        return new ResponseCommon<T>(STATUS_FAILURE, CODE_FAILURE, null, message);
    }

    public static <T> ResponseCommon<T> fail(String message, T payload) {
        return new ResponseCommon<T>(STATUS_FAILURE, CODE_FAILURE, payload, message);
    }
}