package io.github.weimin96.springdocplus.samples.order.bean;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 统一响应结果。
 * <p>
 * 该模型专门用于在样例服务中稳定生成 {@code CommonResult<T>} 的 OpenAPI Schema，
 * 这样前端文档页可以直接验证 data 字段在泛型包装场景下的引用展示与递归展开是否正确。
 *
 * @param <T> 业务数据类型
 * @author pwm
 */
@Schema(description = "统一响应结果")
public class CommonResult<T> {

    @Schema(description = "业务状态码")
    private Integer code;

    @Schema(description = "业务提示信息")
    private String msg;

    @Schema(description = "业务数据")
    private T data;

    @Schema(description = "链路追踪标识")
    private String traceId;

    /**
     * 无参构造器。
     */
    public CommonResult() {
    }

    /**
     * 获取业务状态码。
     *
     * @return 业务状态码
     */
    public Integer getCode() {
        return code;
    }

    /**
     * 设置业务状态码。
     *
     * @param code 业务状态码
     */
    public void setCode(Integer code) {
        this.code = code;
    }

    /**
     * 获取业务提示信息。
     *
     * @return 业务提示信息
     */
    public String getMsg() {
        return msg;
    }

    /**
     * 设置业务提示信息。
     *
     * @param msg 业务提示信息
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * 获取业务数据。
     *
     * @return 业务数据
     */
    public T getData() {
        return data;
    }

    /**
     * 设置业务数据。
     *
     * @param data 业务数据
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * 获取链路追踪标识。
     *
     * @return 链路追踪标识
     */
    public String getTraceId() {
        return traceId;
    }

    /**
     * 设置链路追踪标识。
     *
     * @param traceId 链路追踪标识
     */
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
