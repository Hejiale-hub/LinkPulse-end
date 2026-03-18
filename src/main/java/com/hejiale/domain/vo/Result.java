package com.hejiale.domain.vo;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    // 静态工厂方法便于快速创建,带数据的成功结果
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.message = "success";
        result.data = data;
        return result;
    }

    // 静态工厂方法便于快速创建,不带数据的成功结果
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = 200;
        result.message = "success";
        return result;
    }

    // 静态工厂方法便于快速创建,带数据的失败结果
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        return result;
    }

    // 静态工厂方法便于快速创建,不带数据的失败结果
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.code = 0;
        result.message = message;
        return result;
    }
}
