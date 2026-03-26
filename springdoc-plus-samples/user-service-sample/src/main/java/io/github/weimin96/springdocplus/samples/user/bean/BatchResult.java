package io.github.weimin96.springdocplus.samples.user.bean;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 批量操作结果
 *
 * @author pwm
 * @since 2026/3/2 16:20
 */
@Schema(description = "批量操作结果")
public class BatchResult {
    @Schema(description = "成功数量")
    private Integer successCount;

    @Schema(description = "失败数量")
    private Integer failCount;

    @Schema(description = "失败详情")
    private List<String> failDetails;

    /**
     * 无参构造器
     */
    public BatchResult() {
    }

    // Getters and Setters

    /**
     * 获取成功数量
     *
     * @return 成功数量
     */
    public Integer getSuccessCount() {
        return successCount;
    }

    /**
     * 设置成功数量
     *
     * @param successCount 成功数量
     */
    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    /**
     * 获取失败数量
     *
     * @return 失败数量
     */
    public Integer getFailCount() {
        return failCount;
    }

    /**
     * 设置失败数量
     *
     * @param failCount 失败数量
     */
    public void setFailCount(Integer failCount) {
        this.failCount = failCount;
    }

    /**
     * 获取失败详情
     *
     * @return 失败详情
     */
    public List<String> getFailDetails() {
        return failDetails;
    }

    /**
     * 设置失败详情
     *
     * @param failDetails 失败详情
     */
    public void setFailDetails(List<String> failDetails) {
        this.failDetails = failDetails;
    }
}