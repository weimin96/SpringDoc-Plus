package io.github.weimin96.springdocplus.samples.user.bean;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 文件上传结果
 *
 * @author pwm
 * @since 2026/3/2 16:20
 */
@Schema(description = "文件上传结果")
public class FileUploadResult {
    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "文件类型")
    private String contentType;

    @Schema(description = "文件分类")
    private String category;

    @Schema(description = "是否公开")
    private Boolean isPublic;

    @Schema(description = "访问URL")
    private String url;

    /**
     * 无参构造器
     */
    public FileUploadResult() {
    }

    // Getters and Setters

    /**
     * 获取文件名
     *
     * @return 文件名
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * 设置文件名
     *
     * @param fileName 文件名
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * 获取文件大小
     *
     * @return 文件大小
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * 设置文件大小
     *
     * @param fileSize 文件大小
     */
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * 获取文件类型
     *
     * @return 文件类型
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * 设置文件类型
     *
     * @param contentType 文件类型
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * 获取文件分类
     *
     * @return 文件分类
     */
    public String getCategory() {
        return category;
    }

    /**
     * 设置文件分类
     *
     * @param category 文件分类
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * 获取是否公开
     *
     * @return 是否公开
     */
    public Boolean getPublic() {
        return isPublic;
    }

    /**
     * 设置是否公开
     *
     * @param aPublic 是否公开
     */
    public void setPublic(Boolean aPublic) {
        isPublic = aPublic;
    }

    /**
     * 获取访问URL
     *
     * @return 访问URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置访问URL
     *
     * @param url 访问URL
     */
    public void setUrl(String url) {
        this.url = url;
    }
}