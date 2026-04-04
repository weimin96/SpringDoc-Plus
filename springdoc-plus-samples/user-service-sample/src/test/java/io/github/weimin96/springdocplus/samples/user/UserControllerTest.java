package io.github.weimin96.springdocplus.samples.user;

import io.github.weimin96.springdocplus.samples.user.bean.Order;
import io.github.weimin96.springdocplus.samples.user.bean.OrderItem;
import io.github.weimin96.springdocplus.samples.user.bean.User;
import io.github.weimin96.springdocplus.samples.user.bean.UserStatus;
import io.github.weimin96.springdocplus.samples.user.controller.UserController;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UserControllerTest {

    private final UserController controller = new UserController();

    @Test
    void getByIdAndListReturnUsers() {
        var byId = controller.getById(5L);
        assertThat(byId.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(byId.getBody()).isNotNull();
        assertThat(byId.getBody().getId()).isEqualTo(5L);

        var list = controller.list(2, 3, "a", 1, 9, "createdAt", "desc");
        assertThat(list.getBody()).isNotNull();
        assertThat(list.getBody().getItems()).hasSize(3);
        assertThat(list.getBody().getPage()).isEqualTo(2);
    }

    @Test
    void searchAndStatusEndpointsReturnExpectedValues() {
        var search = controller.search("bob", "ACTIVE", List.of(1L, 2L), LocalDate.now(), LocalDate.now());
        assertThat(search.getBody()).hasSize(1);
        assertThat(search.getBody().getFirst().getName()).contains("bob");

        var status = controller.getStatus(201);
        assertThat(status.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(status.getBody()).containsEntry("requestedStatus", 201);
    }

    @Test
    void createAndUpdateEndpointsPopulateFields() {
        User user = new User();
        user.setName("alice");
        user.setEmail("alice@example.com");

        var created = controller.createJson(user);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody().getId()).isNotNull();
        assertThat(created.getBody().getCreatedAt()).isNotNull();

        var xmlCreated = controller.createXml(new User());
        assertThat(xmlCreated.getBody().getId()).isNotNull();

        var formCreated = controller.createForm("tom", "tom@example.com", 18, "hello");
        assertThat(formCreated.getBody().getName()).isEqualTo("tom");

        var updated = controller.update(9L, new User());
        assertThat(updated.getBody().getId()).isEqualTo(9L);
        assertThat(updated.getBody().getUpdatedAt()).isNotNull();

        var partial = controller.partialUpdate(10L, Map.of("name", "neo", "email", "neo@example.com"));
        assertThat(partial.getBody().getName()).isEqualTo("neo");
        assertThat(partial.getBody().getEmail()).isEqualTo("neo@example.com");
    }

    @Test
    void batchDeleteAndCookieEndpointsWork() {
        var batch = controller.batchCreate(List.of(new User(), new User()));
        assertThat(batch.getBody().getSuccessCount()).isEqualTo(2);
        assertThat(batch.getBody().getFailCount()).isZero();

        var deleted = controller.delete(1L);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        var batchDelete = controller.batchDelete(List.of(1L, 2L, 3L));
        assertThat(batchDelete.getBody().getSuccessCount()).isEqualTo(3);

        var cookies = controller.getCookie("sid", "dark");
        assertThat(cookies.getBody()).containsEntry("sessionId", "sid");
        assertThat(cookies.getBody()).containsEntry("preferences", "dark");

        var setCookie = controller.setCookie();
        assertThat(setCookie.getHeaders().get("Set-Cookie")).hasSize(2);
    }

    @Test
    void fileUploadAndAvatarEndpointsReturnMetadata() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "demo.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile avatar = new MockMultipartFile("avatar", "avatar.png", "image/png", new byte[] {1, 2});

        var upload = controller.uploadFile(file, "docs", true);
        assertThat(upload.getBody().getFileName()).isEqualTo("demo.txt");
        assertThat(upload.getBody().getPublic()).isTrue();

        var multiple = controller.uploadMultiple(new MockMultipartFile[] {file}, "docs");
        assertThat(multiple.getBody()).hasSize(1);

        var avatarResult = controller.updateAvatar(1L, avatar, "circle");
        assertThat(avatarResult.getBody()).containsEntry("userId", 1L);
        assertThat(avatarResult.getBody()).containsEntry("avatarType", "circle");
    }

    @Test
    void downloadHeadersAndCustomHeadersArePresent() {
        var download = controller.downloadFile("abc");
        assertThat(download.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains("example.txt");
        assertThat(new String(download.getBody(), StandardCharsets.UTF_8)).contains("abc");

        var headers = controller.getHeaders("Bearer 12345678901234567890rest", "mobile", "2.0", "en-US", "JUnit");
        assertThat(headers.getBody()).containsEntry("clientType", "mobile");
        assertThat((String) headers.getBody().get("authorization")).startsWith("Bearer 1234567890123");

        var custom = controller.withCustomHeaders();
        assertThat(custom.getHeaders().getFirst("X-Custom-Header")).isNotBlank();
        assertThat(custom.getHeaders().getFirst("X-Rate-Limit-Remaining")).isEqualTo("99");
    }

    @Test
    void textHtmlMapEnumAsyncAndConditionalEndpointsWork() {
        assertThat(controller.getText().getBody()).contains("text/plain");
        assertThat(controller.postText("hello").getBody()).contains("hello");
        assertThat(controller.getHtml().getBody()).contains("<html>");

        Order order = new Order();
        OrderItem item = new OrderItem();
        item.setProductId(1L);
        order.setItems(List.of(item));
        var orderResult = controller.createOrder(order);
        assertThat(orderResult.getBody().getId()).isNotNull();

        var mapResult = controller.processMap(new HashMap<>(Map.of("value", 1)));
        assertThat(mapResult.getBody()).containsEntry("processed", true);

        var byStatus = controller.getByStatus(UserStatus.PENDING);
        assertThat(byStatus.getBody().getFirst().getStatus()).isEqualTo(UserStatus.PENDING);

        var async = controller.asyncProcess(Map.of("task", "demo"));
        assertThat(async.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(async.getBody()).containsEntry("status", "PROCESSING");

        var asyncResult = controller.getAsyncResult("task-1");
        assertThat(asyncResult.getBody()).containsEntry("taskId", "task-1");

        var conditionalMiss = controller.conditionalGet(3L, null);
        assertThat(conditionalMiss.getStatusCode()).isEqualTo(HttpStatus.OK);
        String etag = conditionalMiss.getHeaders().getETag();
        var conditionalHit = controller.conditionalGet(3L, etag);
        assertThat(conditionalHit.getStatusCode()).isEqualTo(HttpStatus.NOT_MODIFIED);
    }
}
