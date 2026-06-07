package com.minibill.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应体
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;
    private String msg;
    private T data;

    private Result() {
    }

    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(200, msg, data);
    }

    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }

    public static <T> Result<T> forbidden(String msg) {
        return new Result<>(403, msg, null);
    }

    public static <T> Result<T> unauthorized() {
        return new Result<>(401, "未登录或token已过期", null);
    }

    /**
     * 分页结果
     */
    public static <T> Result<PageResult<T>> page(long total, long pageSize, long pageNum, T records) {
        PageResult<T> pageResult = new PageResult<>();
        pageResult.setTotal(total);
        pageResult.setPageSize(pageSize);
        pageResult.setPageNum(pageNum);
        pageResult.setRecords(records);
        return success(pageResult);
    }

    @Data
    public static class PageResult<T> {
        private long total;
        private long pageSize;
        private long pageNum;
        private T records;
    }
}
