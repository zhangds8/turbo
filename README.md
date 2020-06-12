# 基于Netty Spring实现轻量级RPC框架

###  使用

#### 搭建服务提供者

使用SpringBoot搭建turbo-demo-provider服务提供者

完整pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>turbo-demo-spring</artifactId>
        <groupId>ds.young</groupId>
        <version>1.0.1</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>turbo-demo-provider</artifactId>
    <dependencies>
        <dependency>
            <groupId>ds.young</groupId>
            <artifactId>turbo-demo-api</artifactId>
            <version>1.0.1</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <exclusions>
                <exclusion>
                    <artifactId>tomcat-embed-websocket</artifactId>
                    <groupId>org.apache.tomcat.embed</groupId>
                </exclusion>
                <exclusion>
                    <groupId>org.apache.tomcat.embed</groupId>
                    <artifactId>tomcat-embed-core</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.apache.tomcat.embed</groupId>
                    <artifactId>tomcat-embed-el</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.apache.tomcat.embed</groupId>
                    <artifactId>tomcat-embed-websocket</artifactId>
                </exclusion>
                <exclusion>
                    <artifactId>spring-boot-starter-tomcat</artifactId>
                    <groupId>org.springframework.boot</groupId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>ds.young</groupId>
            <artifactId>turbo-all</artifactId>
            <version>1.0.1</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
        </dependency>
    </dependencies>
</project>
```

**此处使用Netty作为服务器，所以排除springBoot自带Tomcat**

#### @EnableTurbo注解

```java
package ds.young.turbo.demo.provider;

import ds.young.turbo.spring.annotation.EnableTurbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @program: turbo
 * @description
 * @author: zhangds
 * @create: 2020-06-02 16:55
 **/
@SpringBootApplication
@EnableTurbo(basePackages = {"ds.young"})
public class ProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }
}
```

#### 服务注册

```java
package ds.young.turbo.demo.provider.service.impl;

import ds.young.turbo.common.annotation.TurboService;
import ds.young.turbo.demo.spring.interfaces.ITestServiceDemo;

/**
 * @program: turbo
 * @description
 * @author: admin
 * @create: 2020-06-02 18:15
 **/

@TurboService(interfaceClass = ITestServiceDemo.class)
public class TestServiceDemoImpl implements ITestServiceDemo {
    public String sayHello() throws Exception {
        return "hello turbo";
    }
}

```

#### 配置文件

```properties
server.port=8011
# 允许使用Turbo框架
spring.turbo.enabled=true
# 角色 1提供者or2消费者
spring.turbo.role-type=1
# 组别
spring.turbo.group=ds.young
# 超时时间
spring.turbo.timeout=60000
# 服务版本
spring.turbo.version=1.0
# 注册中心
spring.turbo.zkServer=127.0.0.1:2181
# 启动netty端口
spring.turbo.netty-port=1111
# 使用协议 目前只有turbo（Netty）
spring.turbo.protocol=turbo
# 序列化（目前只有Java）
spring.turbo.serialize-type=java
```

#### 启动服务

```
[ TURBO ] zk服务已注册，服务名：/turbo-register/provider/ds.young.turbo.demo.spring.interfaces.ITestServiceDemo/127.0.0.1|1111|ds.young.turbo.demo.provider.service.impl.TestServiceDemoImpl 
2020-06-12 15:10:34 [main] INFO  d.y.t.s.a.TurboServiceAnnotationBeanPostProcessor - [ TURBO ] 已注册bean名称: serviceBean:ds.young.turbo.demo.provider.service.impl.TestServiceImpl, 实现类: class ds.young.turbo.demo.provider.service.impl.TestServiceImpl 
2020-06-12 15:10:34 [main] INFO  ds.young.turbo.register.zk.RegisterCenter - [ TURBO ] zk服务已注册，服务名：/turbo-register/provider/ds.young.turbo.demo.spring.interfaces.ITestService/127.0.0.1|1111|ds.young.turbo.demo.provider.service.impl.TestServiceImpl 
2020-06-12 15:10:34 [main] INFO  d.y.t.s.a.TurboServiceAnnotationBeanPostProcessor - [ TURBO ] 2 个注解 Turbo's @TurboService 被扫描注册，包名：ds.young 
2020-06-12 15:10:34 [main] INFO  ds.young.turbo.protocol.turbo.NettyServer - [ TURBO ] ||=============================|| 
2020-06-12 15:10:34 [main] INFO  ds.young.turbo.protocol.turbo.NettyServer - [ TURBO ] || netty已启动, 绑定端口：1111    || 
2020-06-12 15:10:34 [main] INFO  ds.young.turbo.protocol.turbo.NettyServer - [ TURBO ] ||=============================|| 
2020-06-12 15:10:34 [main] INFO  ds.young.turbo.register.zk.RegisterCenter - [ TURBO ] 服务提供者地址 :/turbo-register/provider 
2020-06-12 15:10:34 [main] INFO  ds.young.turbo.register.zk.RegisterCenter - [ds.young.turbo.demo.spring.interfaces.ITestServiceDemo, ds.young.turbo.demo.spring.interfaces.ITestService] 
2020-06-12 15:10:34 [main] INFO  ds.young.turbo.register.zk.RegisterCenter - [ TURBO ] 服务地址 :/turbo-register/provider/ds.young.turbo.demo.spring.interfaces.ITestServiceDemo 
2020-06-12 15:10:34 [main] INFO  ds.young.turbo.register.zk.RegisterCenter - [ TURBO ] IP地址集合 :[127.0.0.1|1111|ds.young.turbo.demo.provider.service.impl.TestServiceDemoImpl] 
2020-06-12 15:10:34 [main] INFO  ds.young.turbo.register.zk.RegisterCenter - [ TURBO ] 服务地址 :/turbo-register/provider/ds.young.turbo.demo.spring.interfaces.ITestService 
2020-06-12 15:10:34 [main] INFO  ds.young.turbo.register.zk.RegisterCenter - [ TURBO ] IP地址集合 :[127.0.0.1|1111|ds.young.turbo.demo.provider.service.impl.TestServiceImpl] 
```

#### 搭建服务消费者

完整pom

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>turbo-demo-spring</artifactId>
        <groupId>ds.young</groupId>
        <version>1.0.1</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>turbo-demo-consumer</artifactId>

    <dependencies>
        <dependency>
            <groupId>ds.young</groupId>
            <artifactId>turbo-demo-api</artifactId>
            <version>1.0.1</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>ds.young</groupId>
            <artifactId>turbo-all</artifactId>
            <version>1.0.1</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
        </dependency>
    </dependencies>
</project>
```

#### 编写启动类

```java
package ds.young.turbo.demo.consumer;

import ds.young.turbo.spring.annotation.EnableTurbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @program: turbo
 * @description
 * @author: admin
 * @create: 2020-06-02 16:55
 **/
@SpringBootApplication
@EnableTurbo(basePackages = {"ds.young"})
public class ConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }
}

```

#### 服务测试

```java
package ds.young.turbo.demo.consumer.action;

import ds.young.turbo.common.annotation.Turbo;
import ds.young.turbo.demo.spring.interfaces.ITestService;
import ds.young.turbo.demo.spring.interfaces.ITestServiceDemo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: turbo
 * @description
 * @author: admin
 * @create: 2020-06-10 17:19
 **/
@RestController
public class TestRpcAction {

    @Turbo
    ITestService testService;

    @Turbo
    ITestServiceDemo testServiceDemo;

    @RequestMapping(value = "/mcd")
    public String helloRpc(String params) throws Exception {

        return testService.sayHaHa() + testServiceDemo.sayHello();

    }
}

```

#### 配置文件

```properties
server.port=8011
# 允许使用Turbo框架
spring.turbo.enabled=true
# 角色 1提供者or2消费者
spring.turbo.role-type=1
# 组别
spring.turbo.group=ds.young
# 超时时间
spring.turbo.timeout=60000
# 服务版本
spring.turbo.version=1.0
# 注册中心
spring.turbo.zkServer=127.0.0.1:2181
# 启动netty端口
spring.turbo.netty-port=1111
# 使用协议 目前只有turbo（Netty）
spring.turbo.protocol=turbo
# 序列化（目前只有Java）
spring.turbo.serialize-type=java
```




echo "# turbo" >> README.md
git init
git add README.md
git commit -m "first commit"
git remote add origin https://github.com/zhangds8/turbo.git
git push -u origin master