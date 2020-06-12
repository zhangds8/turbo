package ds.young.turbo.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @program: turbo-netty
 * @description
 * @author: zhangds
 * <p>
 * 有道无术，术尚可求，有术无道，止于术
 * @create: 2020-05-26 10:38
 **/

@ConfigurationProperties(
        prefix = "spring.turbo"
)
public class TurboProperties {

    // 是否启用
    private boolean enabled;
    // 协议类型
    private String protocol;
    // 超时时间
    private Integer timeout;
    // 组名
    private String group;
    // 服务版本号
    private String version;
    // 注册中心地址
    private String zkServer;
    // 序列化类型
    private String serializeType;
    // netty 端口
    private Integer nettyPort;
    // 角色性质 消费者or生产者
    private String roleType;
    // 路由策略
    private String stragety;

    public String getStragety() {
        return stragety;
    }

    public void setStragety(String stragety) {
        this.stragety = stragety;
    }

    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }

    public Integer getNettyPort() {
        return nettyPort;
    }

    public void setNettyPort(Integer nettyPort) {
        this.nettyPort = nettyPort;
    }

    public String getSerializeType() {
        return serializeType;
    }

    public void setSerializeType(String serializeType) {
        this.serializeType = serializeType;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public String getZkServer() {
        return zkServer;
    }

    public void setZkServer(String zkServer) {
        this.zkServer = zkServer;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
