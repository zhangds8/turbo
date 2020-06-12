package ds.young.turbo.spring.utils;


import org.springframework.boot.bind.PropertySourcesPropertyValues;
import org.springframework.boot.bind.RelaxedDataBinder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;

/**
 * @program: edas-service-demo
 * @description
 * @author: zhangds
 * <p>
 * 有道无术，术尚可求，有术无道，止于术
 * @create: 2020-05-14 18:26
 **/
public class BinderUtils {
    private static boolean existBinder = false;
    private static boolean existRelaxedDataBinder = false;
    private static Method Binder_get_Method;
    private static Method Binder_bind_Method;
    private static Method Bindable_of_Method;
    private static Method BindResult_orElse_Method;

    public BinderUtils() {
    }

    public static <T> T bind(ConfigurableEnvironment environment, String prefix, Class<T> type) {
        if (existRelaxedDataBinder) {
            return relaxedDataBinderBind(environment, prefix, type);
        } else if (existBinder) {
            return binderBind(environment, prefix, type);
        } else {
            throw new IllegalStateException("Can not find class org.springframework.boot.context.properties.bind.Binder or org.springframework.boot.bind.RelaxedDataBinder");
        }
    }

    private static <T> T binderBind(ConfigurableEnvironment environment, String prefix, Class<T> type) {
        try {
            if (Binder_get_Method == null) {
                Class<?> binderClass = Class.forName("org.springframework.boot.context.properties.bind.Binder");
                Class<?> bindableClass = Class.forName("org.springframework.boot.context.properties.bind.Bindable");
                Binder_get_Method = binderClass.getMethod("get", Environment.class);
                Binder_bind_Method = binderClass.getMethod("bind", String.class, bindableClass);
            }

            if (Bindable_of_Method == null) {
                Bindable_of_Method = Class.forName("org.springframework.boot.context.properties.bind.Bindable").getMethod("of", Class.class);
            }

            if (BindResult_orElse_Method == null) {
                BindResult_orElse_Method = Class.forName("org.springframework.boot.context.properties.bind.BindResult").getMethod("orElse", Object.class);
            }

            Object binder = Binder_get_Method.invoke((Object)null, environment);
            Object bindable = Bindable_of_Method.invoke((Object)null, type);
            Object bindResult = Binder_bind_Method.invoke(binder, prefix, bindable);
            return (T) BindResult_orElse_Method.invoke(bindResult, type.newInstance());
        } catch (Throwable var6) {
            throw new IllegalArgumentException(var6);
        }
    }

    private static <T> T relaxedDataBinderBind(ConfigurableEnvironment environment, String prefix, Class<T> type) {
        Object instance;
        try {
            instance = type.newInstance();
        } catch (Throwable var5) {
            throw new IllegalArgumentException(var5);
        }

        (new RelaxedDataBinder(instance, prefix)).bind(new PropertySourcesPropertyValues(environment.getPropertySources()));
        return (T) instance;
    }

    static {
        try {
            Class.forName("org.springframework.boot.context.properties.bind.Binder");
            existBinder = true;
        } catch (ClassNotFoundException var2) {
        }

        try {
            Class.forName("org.springframework.boot.bind.RelaxedDataBinder");
            existRelaxedDataBinder = true;
        } catch (ClassNotFoundException var1) {
        }

    }
}
