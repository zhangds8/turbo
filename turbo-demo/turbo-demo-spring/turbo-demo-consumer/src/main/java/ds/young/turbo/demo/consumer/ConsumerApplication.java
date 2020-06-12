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
