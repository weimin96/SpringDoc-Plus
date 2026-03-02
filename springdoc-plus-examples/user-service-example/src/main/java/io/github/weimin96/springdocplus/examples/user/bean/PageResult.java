package io.github.weimin96.springdocplus.examples.user.bean;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 分页结果
 *
 * @author pwm
 * @since 2026/3/2 16:19
 */
@Schema(description = "分页结果")
public class PageResult<T> {
    @Schema(description = "数据列表")
    private List<T> items;

    @Schema(description = "总数")
    private Long total;

    @Schema(description = "当前页")
    private Integer page;

    @Schema(description = "每页数量")
    private Integer size;

    // Getters and Setters
    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
