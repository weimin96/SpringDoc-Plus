package io.github.weimin96.springdocplus.samples.user.bean;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 分页结
 * @param <T> 泛型
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

    /**
     * 无参构造器
     */
    public PageResult() {
    }

    // Getters and Setters

    /**
     * 获取数据列表
     *
     * @return 数据列表
     */
    public List<T> getItems() {
        return items;
    }

    /**
     * 设置数据列表
     *
     * @param items 数据列表
     */
    public void setItems(List<T> items) {
        this.items = items;
    }

    /**
     * 获取总数
     *
     * @return 总数
     */
    public Long getTotal() {
        return total;
    }

    /**
     * 设置总数
     *
     * @param total 总数
     */
    public void setTotal(Long total) {
        this.total = total;
    }

    /**
     * 获取当前页
     *
     * @return 当前页
     */
    public Integer getPage() {
        return page;
    }

    /**
     * 设置当前页
     *
     * @param page 当前页
     */
    public void setPage(Integer page) {
        this.page = page;
    }

    /**
     * 获取每页数量
     *
     * @return 每页数量
     */
    public Integer getSize() {
        return size;
    }

    /**
     * 设置每页数量
     *
     * @param size 每页数量
     */
    public void setSize(Integer size) {
        this.size = size;
    }
}