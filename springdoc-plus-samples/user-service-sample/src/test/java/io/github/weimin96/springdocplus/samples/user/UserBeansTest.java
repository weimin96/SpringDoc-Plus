package io.github.weimin96.springdocplus.samples.user;

import io.github.weimin96.springdocplus.samples.user.bean.BatchResult;
import io.github.weimin96.springdocplus.samples.user.bean.FileUploadResult;
import io.github.weimin96.springdocplus.samples.user.bean.Order;
import io.github.weimin96.springdocplus.samples.user.bean.OrderItem;
import io.github.weimin96.springdocplus.samples.user.bean.PageResult;
import io.github.weimin96.springdocplus.samples.user.bean.User;
import io.github.weimin96.springdocplus.samples.user.bean.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserBeansTest {

    @Test
    void batchResultStoresFields() {
        BatchResult result = new BatchResult();
        result.setSuccessCount(2);
        result.setFailCount(1);
        result.setFailDetails(List.of("x"));

        assertThat(result.getSuccessCount()).isEqualTo(2);
        assertThat(result.getFailCount()).isEqualTo(1);
        assertThat(result.getFailDetails()).containsExactly("x");
    }

    @Test
    void fileUploadResultStoresFields() {
        FileUploadResult result = new FileUploadResult();
        result.setFileName("a.txt");
        result.setFileSize(12L);
        result.setContentType("text/plain");
        result.setCategory("docs");
        result.setPublic(true);
        result.setUrl("/files/a.txt");

        assertThat(result.getFileName()).isEqualTo("a.txt");
        assertThat(result.getFileSize()).isEqualTo(12L);
        assertThat(result.getContentType()).isEqualTo("text/plain");
        assertThat(result.getCategory()).isEqualTo("docs");
        assertThat(result.getPublic()).isTrue();
        assertThat(result.getUrl()).isEqualTo("/files/a.txt");
    }

    @Test
    void orderAndOrderItemStoreFields() {
        User user = new User();
        user.setId(1L);
        OrderItem item = new OrderItem();
        item.setProductId(10L);
        item.setProductName("book");
        item.setQuantity(2);
        item.setPrice(9.9);
        LocalDateTime now = LocalDateTime.now();

        Order order = new Order();
        order.setId(100L);
        order.setOrderNo("NO-1");
        order.setUser(user);
        order.setItems(List.of(item));
        order.setCreatedAt(now);

        assertThat(order.getId()).isEqualTo(100L);
        assertThat(order.getOrderNo()).isEqualTo("NO-1");
        assertThat(order.getUser()).isSameAs(user);
        assertThat(order.getItems()).containsExactly(item);
        assertThat(order.getCreatedAt()).isEqualTo(now);
        assertThat(item.getProductId()).isEqualTo(10L);
        assertThat(item.getProductName()).isEqualTo("book");
        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(item.getPrice()).isEqualTo(9.9);
    }

    @Test
    void pageResultAndUserStoreFields() {
        User user = new User();
        LocalDate birthday = LocalDate.of(2000, 1, 1);
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = createdAt.plusDays(1);
        user.setId(1L);
        user.setName("alice");
        user.setEmail("alice@example.com");
        user.setAge(18);
        user.setBirthday(birthday);
        user.setStatus(UserStatus.BANNED);
        user.setBio("bio");
        user.setCreatedAt(createdAt);
        user.setUpdatedAt(updatedAt);

        PageResult<User> pageResult = new PageResult<>();
        pageResult.setItems(List.of(user));
        pageResult.setTotal(1L);
        pageResult.setPage(1);
        pageResult.setSize(10);

        assertThat(pageResult.getItems()).containsExactly(user);
        assertThat(pageResult.getTotal()).isEqualTo(1L);
        assertThat(pageResult.getPage()).isEqualTo(1);
        assertThat(pageResult.getSize()).isEqualTo(10);
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("alice");
        assertThat(user.getEmail()).isEqualTo("alice@example.com");
        assertThat(user.getAge()).isEqualTo(18);
        assertThat(user.getBirthday()).isEqualTo(birthday);
        assertThat(user.getStatus()).isEqualTo(UserStatus.BANNED);
        assertThat(user.getBio()).isEqualTo("bio");
        assertThat(user.getCreatedAt()).isEqualTo(createdAt);
        assertThat(user.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(UserStatus.values()).containsExactly(UserStatus.ACTIVE, UserStatus.INACTIVE, UserStatus.BANNED, UserStatus.PENDING);
    }
}
