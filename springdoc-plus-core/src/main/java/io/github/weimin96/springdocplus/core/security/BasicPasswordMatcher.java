package io.github.weimin96.springdocplus.core.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Basic Auth 密码匹配器。
 * <p>
 * 明文配置用于兼容已有项目，{@code {bcrypt}} 前缀用于生产环境避免把明文密码写入配置文件。
 *
 * @author pwm
 */
public final class BasicPasswordMatcher {

    private static final String BCRYPT_PREFIX = "{bcrypt}";

    private static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();

    private BasicPasswordMatcher() {
    }

    /**
     * 校验用户输入的密码是否匹配配置值。
     *
     * @param rawPassword     用户输入的明文密码
     * @param configuredValue 配置中的密码或带前缀哈希值
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String configuredValue) {
        if (rawPassword == null || configuredValue == null) {
            return false;
        }
        if (configuredValue.startsWith(BCRYPT_PREFIX)) {
            String encoded = configuredValue.substring(BCRYPT_PREFIX.length());
            return BCRYPT.matches(rawPassword, encoded);
        }
        return MessageDigest.isEqual(
                rawPassword.getBytes(StandardCharsets.UTF_8),
                configuredValue.getBytes(StandardCharsets.UTF_8)
        );
    }
}
