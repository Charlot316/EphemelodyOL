package team.javaee.common.config;

import io.swagger.annotations.ApiModelProperty;
import team.javaee.common.enums.ReturnStatus;
import team.javaee.common.enums.ReturnWebStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.io.Serializable;

public class ReturnResponse<T> implements Serializable {
    private static final long serialVersionUID = -7318467237446066728L;

    @ApiModelProperty(value = "错误码")
    private int code;
    @ApiModelProperty(value = "错误信息描述")
    private String msg;
    @ApiModelProperty(value = "数据对象")
    private T data;

    public ReturnResponse() {
        this(ReturnStatus.SUCCESS, null);
    }

    public ReturnResponse(T data) {
        this(ReturnStatus.SUCCESS, data);
    }

    public ReturnResponse(ReturnStatus returnStatus) {
        this(returnStatus, null);
    }

    public ReturnResponse(ReturnWebStatus returnWebStatus, T data) {
        this.code = returnWebStatus.getCode();
        this.msg = returnWebStatus.getMsg();
        this.data = data;
    }

    public ReturnResponse(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @JsonIgnore
    public boolean isSuccess() {
        return this.code == ReturnStatus.SUCCESS.getCode();
    }

    @Override
    public String toString() {
        return "{\"code\":" + code + ", \"msg\":\"" + msg + "\", \"data\":" + (data == null) + "}";
    }

    public static ReturnResponse<String> OK() {
        return packageObject("", ReturnStatus.SUCCESS);
    }

    public static <T> ReturnResponse<T> OK(T data) {
        return packageObject(data, ReturnStatus.SUCCESS);
    }

    public static <T> ReturnResponse<T> FAIL(String message) {
        ReturnResponse<T> responseResult = new ReturnResponse<>();
        responseResult.setCode(ReturnStatus.FAILURE.getCode());
        responseResult.setMsg(message);
        return responseResult;
    }

    public static <T> ReturnResponse<T> packageObject(T data, ReturnStatus returnStatus) {
        ReturnResponse<T> responseResult = new ReturnResponse<>();
        responseResult.setCode(returnStatus.getCode());
        responseResult.setMsg(returnStatus.getMsg());
        responseResult.setData(data);
        return responseResult;
    }

    public static <T> ReturnResponse<T> packageObject(T data, Integer code, String message) {
        ReturnResponse<T> responseResult = new ReturnResponse<>();
        responseResult.setCode(code);
        responseResult.setMsg(message);
        responseResult.setData(data);
        return responseResult;
    }

    public static <T> ReturnResponse<T> paramsError(ReturnStatus returnStatus) {
        return packageObject(null, returnStatus);
    }

    public static <T> ReturnResponse<T> systemError(ReturnStatus returnStatus) {
        return packageObject(null, returnStatus);
    }

    public static <T> ReturnResponse<T> systemException(ReturnStatus returnStatus) {
        return packageObject(null, returnStatus);
    }

    public static <T> ReturnResponse<T> systemException(ReturnStatus returnStatus, String message) {
        return packageObject(null, returnStatus.getCode(),
                Strings.isNullOrEmpty(message) ? returnStatus.getMsg() : message);
    }
}
