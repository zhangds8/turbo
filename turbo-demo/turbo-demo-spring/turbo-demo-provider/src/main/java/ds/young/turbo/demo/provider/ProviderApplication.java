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
