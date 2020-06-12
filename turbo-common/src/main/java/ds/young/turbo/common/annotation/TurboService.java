package ds.young.turbo.common.annotation;

import java.lang.annotation.*;

/**
 * @program: turbo-netty
 * @description
 * @author: zhangds
 * <p>
 * 有道无术，术尚可求，有术无道，止于术
 * @create: 2020-05-25 16:15
 **/

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface TurboService {

    Class<?> interfaceClass() default void.class;

    String version() default "";

    String serviceDesc() default "turbo service";

    String serviceName() default "turbo_service";

    String timeout() default "60000";
}
