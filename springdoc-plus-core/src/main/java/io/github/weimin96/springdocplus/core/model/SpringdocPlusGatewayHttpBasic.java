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

    /**
     * 获取是否启用 Basic 认证
     *
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用 Basic 认证
     *
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 获取用户名
     *
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名
     *
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取密码
     *
     * @return 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置密码
     *
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
