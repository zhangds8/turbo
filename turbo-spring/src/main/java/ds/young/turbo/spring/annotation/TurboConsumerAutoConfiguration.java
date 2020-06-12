package ds.young.turbo.spring.annotation;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: turbo
 * @description
 * @author: admin
 * @create: 2020-06-10 16:31
 **/
@Configuration
@ConditionalOnProperty(
        name = {"spring.turbo.enabled"},
        matchIfMissing = true
)
public class TurboConsumerAutoConfiguration {
    public TurboConsumerAutoConfiguration() {

    }

    @Bean
    public static BeanFactoryPostProcessor hsfConsumerPostProcessor() {
        return new TurboReferenceAnnotationBeanPostProcessor();
    }

}
