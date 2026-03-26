package io.github.weimin96.springdocplus.core.model;

/**
 * 网关 Basic 认证配置模型。
 * <p>
 * 用于配置网关文档的 HTTP Basic 认证保护，
 * 启用后访问文档页面需要进行身份验证。
 *
 * @author pwm
 */
public class SpringdocPlusGatewayHttpBasic {
    /**
     * 是否启用 Basic 认证
     */
    private boolean enabled = false;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 无参构造器。
     * <p>
     * Javadoc 要求必须存在默认构造器的注释说明。
     */
    public SpringdocPlusGatewayHttpBasic() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
