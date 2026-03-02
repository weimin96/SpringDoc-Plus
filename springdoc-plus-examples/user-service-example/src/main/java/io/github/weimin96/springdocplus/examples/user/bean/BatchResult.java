package io.github.weimin96.springdocplus.examples.user.bean;

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

    // Getters and Setters
    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailCount() {
        return failCount;
    }

    public void setFailCount(Integer failCount) {
        this.failCount = failCount;
    }

    public List<String> getFailDetails() {
        return failDetails;
    }

    public void setFailDetails(List<String> failDetails) {
        this.failDetails = failDetails;
    }
}
