package io.github.weimin96.springdocplus.examples.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 用户控制器 - 演示各种HTTP方法和Content-Type
 *
 * @author pwm
 */
@Tag(name = "用户管理", description = "用户相关接口，包含各种HTTP方法和Content-Type示例")
@RestController
@RequestMapping("/users")
public class UserController {

    // ==================== GET 请求示例 ====================

    @Operation(summary = "根据ID获取用户", description = "通过路径参数获取单个用户信息")
    @ApiResponse(responseCode = "200", description = "成功获取用户")
    @ApiResponse(responseCode = "404", description = "用户不存在")
    @GetMapping("/{id}")
    public ResponseEntity<User> getById(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long id) {
        User user = new User();
        user.setId(id);
        user.setName("用户" + id);
        user.setEmail("user" + id + "@example.com");
        user.setAge(25);
        user.setBirthday(LocalDate.of(1999, 1, 1));
        user.setCreatedAt(LocalDateTime.now());
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "分页查询用户列表", description = "支持多条件筛选和分页")
    @GetMapping
    public ResponseEntity<PageResult<User>> list(
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10")
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户名，支持模糊查询")
            @RequestParam(required = false) String name,
            @Parameter(description = "最小年龄")
            @RequestParam(required = false) Integer minAge,
            @Parameter(description = "最大年龄")
            @RequestParam(required = false) Integer maxAge,
            @Parameter(description = "排序字段")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向", example = "asc 或 desc")
            @RequestParam(defaultValue = "desc") String sortOrder) {

        List<User> users = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            User user = new User();
            user.setId((long) (page * size + i));
            user.setName("用户" + i);
            user.setEmail("user" + i + "@example.com");
            user.setAge(20 + i % 30);
            users.add(user);
        }

        PageResult<User> result = new PageResult<>();
        result.setItems(users);
        result.setTotal(100L);
        result.setPage(page);
        result.setSize(size);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "搜索用户", description = "通过多个查询参数搜索用户")
    @GetMapping("/search")
    public ResponseEntity<List<User>> search(
            @Parameter(description = "关键词，匹配用户名或邮箱")
            @RequestParam String keyword,
            @Parameter(description = "状态：ACTIVE-激活, INACTIVE-未激活, BANNED-封禁")
            @RequestParam(defaultValue = "ACTIVE") String status,
            @Parameter(description = "部门ID列表，多个用逗号分隔")
            @RequestParam(required = false) List<Long> deptIds,
            @Parameter(description = "注册开始日期")
            @RequestParam(required = false) @Schema(type = "string", format = "date") LocalDate startDate,
            @Parameter(description = "注册结束日期")
            @RequestParam(required = false) @Schema(type = "string", format = "date") LocalDate endDate) {

        List<User> users = new ArrayList<>();
        User user = new User();
        user.setId(1L);
        user.setName("搜索结果-" + keyword);
        user.setStatus(UserStatus.valueOf(status));
        users.add(user);
        return ResponseEntity.ok(users);
    }

    // ==================== POST 请求示例 ====================

    @Operation(summary = "创建用户（JSON）", description = "使用application/json创建新用户")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "用户信息",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = User.class)
            )
    )
    @ApiResponse(responseCode = "201", description = "创建成功")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> createJson(@org.springframework.web.bind.annotation.RequestBody User user) {
        user.setId(System.currentTimeMillis());
        user.setCreatedAt(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @Operation(summary = "创建用户（XML）", description = "使用application/xml创建新用户")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "用户信息（XML格式）",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_XML_VALUE,
                    schema = @Schema(implementation = User.class)
            )
    )
    @PostMapping(path = "/xml", consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<User> createXml(@org.springframework.web.bind.annotation.RequestBody User user) {
        user.setId(System.currentTimeMillis());
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "创建用户（表单）", description = "使用application/x-www-form-urlencoded提交用户信息")
    @PostMapping(path = "/form", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<User> createForm(
            @Parameter(description = "用户名", required = true)
            @RequestParam String name,
            @Parameter(description = "邮箱", required = true)
            @RequestParam String email,
            @Parameter(description = "年龄")
            @RequestParam(required = false) Integer age,
            @Parameter(description = "简介")
            @RequestParam(required = false) String bio) {

        User user = new User();
        user.setId(System.currentTimeMillis());
        user.setName(name);
        user.setEmail(email);
        user.setAge(age);
        user.setBio(bio);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "批量创建用户", description = "一次创建多个用户")
    @PostMapping("/batch")
    public ResponseEntity<BatchResult> batchCreate(
            @org.springframework.web.bind.annotation.RequestBody List<User> users) {
        BatchResult result = new BatchResult();
        result.setSuccessCount(users.size());
        result.setFailCount(0);
        result.setFailDetails(Collections.emptyList());
        return ResponseEntity.ok(result);
    }

    // ==================== PUT 请求示例 ====================

    @Operation(summary = "更新用户（全量）", description = "全量更新用户信息，未传递的字段会被置空")
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> update(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestBody User user) {
        user.setId(id);
        user.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "更新用户头像", description = "使用multipart/form-data上传头像并更新")
    @PutMapping(path = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> updateAvatar(
            @PathVariable Long id,
            @Parameter(description = "头像文件")
            @RequestPart("avatar") MultipartFile avatar,
            @Parameter(description = "头像类型：square-方形, circle-圆形")
            @RequestParam(defaultValue = "circle") String avatarType) throws IOException {

        Map<String, Object> result = new HashMap<>();
        result.put("userId", id);
        result.put("fileName", avatar.getOriginalFilename());
        result.put("fileSize", avatar.getSize());
        result.put("contentType", avatar.getContentType());
        result.put("avatarType", avatarType);
        result.put("url", "/uploads/avatars/" + id + "_" + avatar.getOriginalFilename());
        return ResponseEntity.ok(result);
    }

    // ==================== PATCH 请求示例 ====================

    @Operation(summary = "部分更新用户", description = "只更新传递的字段，未传递的字段保持不变")
    @PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> partialUpdate(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestBody Map<String, Object> updates) {
        User user = new User();
        user.setId(id);
        user.setName((String) updates.getOrDefault("name", "未修改"));
        user.setEmail((String) updates.getOrDefault("email", "未修改"));
        user.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(user);
    }

    // ==================== DELETE 请求示例 ====================

    @Operation(summary = "删除单个用户", description = "根据ID删除用户")
    @ApiResponse(responseCode = "204", description = "删除成功")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "批量删除用户", description = "根据ID列表批量删除用户")
    @DeleteMapping("/batch")
    public ResponseEntity<BatchResult> batchDelete(
            @Parameter(description = "用户ID列表")
            @RequestBody List<Long> ids) {
        BatchResult result = new BatchResult();
        result.setSuccessCount(ids.size());
        result.setFailCount(0);
        return ResponseEntity.ok(result);
    }

    // ==================== 文件上传示例 ====================

    @Operation(summary = "上传单个文件", description = "上传单个文件，支持任意类型")
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResult> uploadFile(
            @Parameter(description = "文件")
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "文件分类")
            @RequestParam(defaultValue = "general") String category,
            @Parameter(description = "是否公开")
            @RequestParam(defaultValue = "false") Boolean isPublic) throws IOException {

        FileUploadResult result = new FileUploadResult();
        result.setFileName(file.getOriginalFilename());
        result.setFileSize(file.getSize());
        result.setContentType(file.getContentType());
        result.setCategory(category);
        result.setPublic(isPublic);
        result.setUrl("/files/" + UUID.randomUUID() + "_" + file.getOriginalFilename());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "上传多个文件", description = "同时上传多个文件")
    @PostMapping(path = "/upload/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<FileUploadResult>> uploadMultiple(
            @Parameter(description = "文件列表")
            @RequestPart("files") MultipartFile[] files,
            @Parameter(description = "文件分类")
            @RequestParam(defaultValue = "general") String category) throws IOException {

        List<FileUploadResult> results = new ArrayList<>();
        for (MultipartFile file : files) {
            FileUploadResult result = new FileUploadResult();
            result.setFileName(file.getOriginalFilename());
            result.setFileSize(file.getSize());
            result.setContentType(file.getContentType());
            result.setCategory(category);
            result.setUrl("/files/" + UUID.randomUUID() + "_" + file.getOriginalFilename());
            results.add(result);
        }
        return ResponseEntity.ok(results);
    }

    // ==================== 文件下载示例 ====================

    @Operation(summary = "下载文件", description = "根据文件ID下载文件")
    @ApiResponse(
            responseCode = "200",
            description = "文件内容",
            content = @Content(mediaType = "application/octet-stream"),
            headers = {
                    @Header(name = HttpHeaders.CONTENT_DISPOSITION,
                            description = "附件文件名",
                            schema = @Schema(type = "string"))
            }
    )
    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileId) {
        String content = "这是一个示例文件内容，文件ID: " + fileId;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"example.txt\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content.getBytes(StandardCharsets.UTF_8));
    }

    // ==================== 请求头示例 ====================

    @Operation(summary = "获取请求头信息", description = "演示获取各种请求头")
    @GetMapping("/headers")
    public ResponseEntity<Map<String, Object>> getHeaders(
            @Parameter(description = "授权令牌")
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Parameter(description = "客户端类型")
            @RequestHeader(value = "X-Client-Type", defaultValue = "web") String clientType,
            @Parameter(description = "API版本")
            @RequestHeader(value = "X-API-Version", defaultValue = "1.0") String apiVersion,
            @Parameter(description = "语言")
            @RequestHeader(value = "Accept-Language", defaultValue = "zh-CN") String acceptLanguage,
            @Parameter(description = "用户代理")
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {

        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("authorization", authorization != null ? authorization.substring(0, Math.min(20, authorization.length())) + "..." : null);
        headers.put("clientType", clientType);
        headers.put("apiVersion", apiVersion);
        headers.put("acceptLanguage", acceptLanguage);
        headers.put("userAgent", userAgent);
        return ResponseEntity.ok(headers);
    }

    // ==================== Cookie 示例 ====================

    @Operation(summary = "获取Cookie信息", description = "演示获取Cookie值")
    @GetMapping("/cookie")
    public ResponseEntity<Map<String, Object>> getCookie(
            @Parameter(description = "会话ID")
            @CookieValue(value = "SESSION_ID", required = false) String sessionId,
            @Parameter(description = "用户偏好设置")
            @CookieValue(value = "preferences", required = false) String preferences) {

        Map<String, Object> cookies = new LinkedHashMap<>();
        cookies.put("sessionId", sessionId);
        cookies.put("preferences", preferences);
        return ResponseEntity.ok(cookies);
    }

    @Operation(summary = "设置Cookie", description = "演示设置Cookie值")
    @PostMapping("/cookie")
    public ResponseEntity<Void> setCookie() {
        return ResponseEntity.ok()
                .header("Set-Cookie", "SESSION_ID=" + UUID.randomUUID() + "; Path=/; HttpOnly")
                .header("Set-Cookie", "preferences=dark_theme; Path=/")
                .build();
    }

    // ==================== 状态码示例 ====================

    @Operation(summary = "获取不同状态码响应", description = "根据参数返回不同的HTTP状态码")
    @GetMapping("/status/{code}")
    public ResponseEntity<Map<String, Object>> getStatus(
            @Parameter(description = "状态码", example = "200, 201, 400, 401, 403, 404, 500")
            @PathVariable Integer code) {

        Map<String, Object> body = new HashMap<>();
        body.put("requestedStatus", code);
        body.put("message", "返回请求的状态码");

        return ResponseEntity.status(HttpStatus.valueOf(code)).body(body);
    }

    // ==================== 纯文本和二进制示例 ====================

    @Operation(summary = "返回纯文本", description = "返回text/plain格式的内容")
    @GetMapping(path = "/text", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getText() {
        return ResponseEntity.ok("这是一段纯文本内容，用于演示text/plain响应。");
    }

    @Operation(summary = "接收纯文本", description = "接收text/plain格式的请求体")
    @PostMapping(path = "/text", consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> postText(
            @org.springframework.web.bind.annotation.RequestBody String text) {
        return ResponseEntity.ok("收到文本: " + text);
    }

    @Operation(summary = "返回HTML", description = "返回text/html格式的内容")
    @GetMapping(path = "/html", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getHtml() {
        String html = "<!DOCTYPE html><html><head><title>示例</title></head>" +
                "<body><h1>用户服务</h1><p>这是一个HTML响应示例</p></body></html>";
        return ResponseEntity.ok(html);
    }

    // ==================== 复杂数据类型示例 ====================

    @Operation(summary = "处理嵌套对象", description = "演示嵌套JSON对象的处理")
    @PostMapping("/nested")
    public ResponseEntity<Order> createOrder(
            @org.springframework.web.bind.annotation.RequestBody Order order) {
        order.setId(System.currentTimeMillis());
        order.setCreatedAt(LocalDateTime.now());
        return ResponseEntity.ok(order);
    }

    @Operation(summary = "处理Map数据", description = "演示动态结构的Map数据处理")
    @PostMapping("/map")
    public ResponseEntity<Map<String, Object>> processMap(
            @org.springframework.web.bind.annotation.RequestBody Map<String, Object> data) {
        data.put("processed", true);
        data.put("processedAt", LocalDateTime.now().toString());
        return ResponseEntity.ok(data);
    }

    // ==================== 枚举参数示例 ====================

    @Operation(summary = "按状态查询用户", description = "演示枚举类型参数")
    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<User>> getByStatus(
            @Parameter(description = "用户状态", schema = @Schema(implementation = UserStatus.class))
            @PathVariable UserStatus status) {
        User user = new User();
        user.setId(1L);
        user.setName("状态用户");
        user.setStatus(status);
        return ResponseEntity.ok(List.of(user));
    }

    // ==================== 响应头示例 ====================

    @Operation(summary = "自定义响应头", description = "演示添加自定义响应头")
    @GetMapping("/custom-headers")
    public ResponseEntity<Map<String, Object>> withCustomHeaders() {
        return ResponseEntity.ok()
                .header("X-Custom-Header", "自定义值")
                .header("X-Request-Id", UUID.randomUUID().toString())
                .header("X-Rate-Limit-Remaining", "99")
                .body(Map.of("message", "响应包含自定义头"));
    }

    // ==================== 异步示例 ====================

    @Operation(summary = "异步处理请求", description = "演示异步处理模式，返回处理任务ID")
    @PostMapping("/async")
    public ResponseEntity<Map<String, Object>> asyncProcess(
            @org.springframework.web.bind.annotation.RequestBody Map<String, Object> task) {
        String taskId = UUID.randomUUID().toString();
        Map<String, Object> result = new HashMap<>();
        result.put("taskId", taskId);
        result.put("status", "PROCESSING");
        result.put("message", "任务已提交，请通过taskId查询结果");
        result.put("resultUrl", "/users/async/" + taskId);
        return ResponseEntity.accepted().body(result);
    }

    @Operation(summary = "查询异步任务结果", description = "根据任务ID查询异步处理结果")
    @GetMapping("/async/{taskId}")
    public ResponseEntity<Map<String, Object>> getAsyncResult(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();
        result.put("taskId", taskId);
        result.put("status", "COMPLETED");
        result.put("result", Map.of("processed", true, "count", 100));
        return ResponseEntity.ok(result);
    }

    // ==================== 条件请求示例 ====================

    @Operation(summary = "条件GET请求", description = "使用If-None-Match头实现条件请求")
    @GetMapping("/conditional/{id}")
    public ResponseEntity<User> conditionalGet(
            @PathVariable Long id,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        String currentEtag = "\"v1-" + id + "\"";

        if (currentEtag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(currentEtag)
                    .build();
        }

        User user = new User();
        user.setId(id);
        user.setName("条件用户");
        return ResponseEntity.ok()
                .eTag(currentEtag)
                .body(user);
    }

    // ==================== 内部数据模型 ====================

    /**
     * 用户实体
     */
    @Schema(description = "用户信息")
    public static class User {
        @Schema(description = "用户ID", example = "1")
        private Long id;

        @Schema(description = "用户名", example = "张三", required = true)
        private String name;

        @Schema(description = "邮箱", example = "zhangsan@example.com", required = true)
        private String email;

        @Schema(description = "年龄", example = "25", minimum = "0", maximum = "150")
        private Integer age;

        @Schema(description = "生日", example = "1999-01-01")
        private LocalDate birthday;

        @Schema(description = "用户状态")
        private UserStatus status = UserStatus.ACTIVE;

        @Schema(description = "个人简介")
        private String bio;

        @Schema(description = "创建时间")
        private LocalDateTime createdAt;

        @Schema(description = "更新时间")
        private LocalDateTime updatedAt;

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public LocalDate getBirthday() {
            return birthday;
        }

        public void setBirthday(LocalDate birthday) {
            this.birthday = birthday;
        }

        public UserStatus getStatus() {
            return status;
        }

        public void setStatus(UserStatus status) {
            this.status = status;
        }

        public String getBio() {
            return bio;
        }

        public void setBio(String bio) {
            this.bio = bio;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }
    }

    /**
     * 用户状态枚举
     */
    @Schema(description = "用户状态")
    public enum UserStatus {
        @Schema(description = "激活")
        ACTIVE,
        @Schema(description = "未激活")
        INACTIVE,
        @Schema(description = "封禁")
        BANNED,
        @Schema(description = "待审核")
        PENDING
    }

    /**
     * 分页结果
     */
    @Schema(description = "分页结果")
    public static class PageResult<T> {
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

    /**
     * 批量操作结果
     */
    @Schema(description = "批量操作结果")
    public static class BatchResult {
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

    /**
     * 文件上传结果
     */
    @Schema(description = "文件上传结果")
    public static class FileUploadResult {
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

        // Getters and Setters
        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public Long getFileSize() {
            return fileSize;
        }

        public void setFileSize(Long fileSize) {
            this.fileSize = fileSize;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public Boolean getPublic() {
            return isPublic;
        }

        public void setPublic(Boolean aPublic) {
            isPublic = aPublic;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    /**
     * 订单实体（用于演示嵌套对象）
     */
    @Schema(description = "订单信息")
    public static class Order {
        @Schema(description = "订单ID")
        private Long id;

        @Schema(description = "订单编号")
        private String orderNo;

        @Schema(description = "用户信息")
        private User user;

        @Schema(description = "订单项列表")
        private List<OrderItem> items;

        @Schema(description = "创建时间")
        private LocalDateTime createdAt;

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getOrderNo() {
            return orderNo;
        }

        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public List<OrderItem> getItems() {
            return items;
        }

        public void setItems(List<OrderItem> items) {
            this.items = items;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }

    /**
     * 订单项
     */
    @Schema(description = "订单项")
    public static class OrderItem {
        @Schema(description = "商品ID")
        private Long productId;

        @Schema(description = "商品名称")
        private String productName;

        @Schema(description = "数量")
        private Integer quantity;

        @Schema(description = "单价")
        private Double price;

        // Getters and Setters
        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }
    }
}