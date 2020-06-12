package ds.young.turbo.spring.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @program: turbo-netty
 * @description
 * @author: zhangds
 * <p>
 * 有道无术，术尚可求，有术无道，止于术
 * @create: 2020-05-25 16:32
 **/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Import({TurboComponentScanRegistrar.class})
public @interface EnableTurbo {

    String[] value() default {};

    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};
}
