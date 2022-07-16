package team.javaee.common.config;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import io.swagger.annotations.ApiModelProperty;
import team.javaee.common.enums.ReturnStatus;
import team.javaee.common.enums.ReturnWebStatus;

import java.io.Serializable;

public class ReturnResponse<T> implements Serializable {
    private static final long serialVersionUID = -7318467237446066728L;

    @ApiModelProperty(value = "错误码")
    private int code;
    @ApiModelProperty(value = "错误信息描述")
    private String msg;
    /**
     * @JsonInclude(Include.NON_NULL) 这个注解放在类头上就可以解决。 实体类与json互转的时候 属性值为null的不参与序列化
     */
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
        return "{" +
                "\"code\":" + code +
                ", \"msg\":\"" + msg + '\"' +
                ", \"data\":" + (data == null) +
                '}';
    }

    /**
     * 用于只返回处理状态的数据（状态码：0）
     *
     * @return 响应结果
     */
    public static ReturnResponse<String> OK() {
        return packageObject(StringUtils.EMPTY, ReturnStatus.SUCCESS);
    }

    /**
     * 正常返回数据（状态码：0）
     *
     * @param data 返回的数据
     * @param <T>  返回的数据类型
     * @return 响应结果
     */
    public static <T> ReturnResponse<T> OK(T data) {
        return packageObject(data, ReturnStatus.SUCCESS);
    }

    public static ReturnResponse FAIL(String message) {
        ReturnResponse responseResult = new ReturnResponse<>();
        responseResult.setCode(ReturnStatus.FAILURE.getCode());
        responseResult.setMsg(message);
        return responseResult;
    }

    /**
     * 对返回的消息进行打包
     *
     * @param data           返回的数据
     * @param returnStatus 自定义的返回码枚举类型
     * @param <T>            返回的数据类型
     * @return 响应结果
     */
    public static <T> ReturnResponse<T> packageObject(T data, ReturnStatus returnStatus) {
        ReturnResponse<T> responseResult = new ReturnResponse<>();
        responseResult.setCode(returnStatus.getCode());
        responseResult.setMsg(returnStatus.getMsg());
        responseResult.setData(data);
        return responseResult;
    }

    /**
     * 对返回的消息进行打包
     *
     * @param data    返回的数据
     * @param code    返回的状态码
     * @param message 返回的消息
     * @param <T>     返回的数据类型
     * @return 响应结果
     */
    public static <T> ReturnResponse<T> packageObject(T data, Integer code, String message) {
        ReturnResponse<T> responseResult = new ReturnResponse<>();
        responseResult.setCode(code);
        responseResult.setMsg(message);
        responseResult.setData(data);
        return responseResult;
    }

    /**
     * 校验入参有误，不满足接口入参要求
     *
     * @param returnStatus 入参有误的返回码枚举类型
     * @param <T>            返回的数据类型
     * @return 响应结果
     */
    public static <T> ReturnResponse<T> paramsError(ReturnStatus returnStatus) {
        return packageObject(null, returnStatus);
    }



    /**
     * 系统服务不可用
     *
     * @param returnStatus Feign依赖服务不可用的返回码枚举类型
     * @param <T>            返回的数据类型
     * @return 响应结果
     */
    public static <T> ReturnResponse<T> systemError(ReturnStatus returnStatus) {
        return packageObject(null, returnStatus);
    }

    /**
     * 系统异常
     *
     * @param returnStatus 异常返回码枚举类型
     * @param <T>            返回的数据类型
     * @return 响应结果
     */
    public static <T> ReturnResponse<T> systemException(ReturnStatus returnStatus) {
        return packageObject(null, returnStatus);
    }



    /**
     * 自定义系统异常信息
     *
     * @param returnStatus 异常返回码枚举类型
     * @param message        自定义消息
     * @param <T>            返回的数据类型
     * @return 响应结果
     */
    public static <T> ReturnResponse<T> systemException(ReturnStatus returnStatus, String message) {
        return packageObject(null, returnStatus.getCode(), Strings.isNullOrEmpty(message) ? returnStatus.getMsg() : message);
    }
}

