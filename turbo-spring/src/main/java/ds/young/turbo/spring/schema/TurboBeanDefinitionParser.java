package ds.young.turbo.spring.schema;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.env.Environment;
import org.w3c.dom.Element;

/**
 * @author ：zhangds5
 * @date ：Created in 2020/5/24 20:56
 * @description：
 * @modified By：
 * @version: $
 */

public class TurboBeanDefinitionParser  implements BeanDefinitionParser {

    private final Class<?> beanClass;
    private final boolean required;

    public TurboBeanDefinitionParser(Class<?> beanClass, boolean required) {
        this.beanClass = beanClass;
        this.required = required;
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(beanClass);
        beanDefinition.setLazyInit(false);
        String id = resolveAttribute(element, "id", parserContext);
        String serviceInterface = resolveAttribute(element, "serviceInterface", parserContext);
        String ref = resolveAttribute(element, "ref", parserContext);
        String version = resolveAttribute(element, "version", parserContext);

        if(StringUtils.isEmpty(id)  && required){
            String generatedBeanName = resolveAttribute(element, "name", parserContext);
            if (StringUtils.isEmpty(generatedBeanName)) {
                    generatedBeanName = resolveAttribute(element, "serviceInterface", parserContext);
            }
            if (StringUtils.isEmpty(generatedBeanName)) {
                generatedBeanName = beanClass.getName();
            }
            id = generatedBeanName;
            int counter = 2;
            while (parserContext.getRegistry().containsBeanDefinition(id)) {
                id = generatedBeanName + (counter++);
            }
        }

        if(StringUtils.isNotEmpty(id)){
            if (parserContext.getRegistry().containsBeanDefinition(id)) {
                throw new IllegalStateException("beanId 已存在！" + id);
            }
            parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);

            beanDefinition.getPropertyValues().addPropertyValue("id", id);
        }

        if(StringUtils.isNotEmpty(ref)){
            BeanDefinition refBean = parserContext.getRegistry().getBeanDefinition(ref);
            if(!refBean.isSingleton()){
                throw new IllegalStateException("The exported service ref " + ref + " must be singleton! Please set the " + ref + " bean scope to singleton, eg: <bean id=\"" + ref + "\" scope=\"singleton\" ...>");
            }
            RuntimeBeanReference runtimeBeanReference = new RuntimeBeanReference(ref);
            beanDefinition.getPropertyValues().addPropertyValue("ref", ref);
        }else {
            throw new IllegalStateException("引用实现为空！");
        }

        beanDefinition.getPropertyValues().addPropertyValue("serviceInterface", serviceInterface);
        beanDefinition.getPropertyValues().addPropertyValue("version", version);
        return beanDefinition;
    }

    private static String resolveAttribute(Element element, String attributeName, ParserContext parserContext) {
        String attributeValue = element.getAttribute(attributeName);
        Environment environment = parserContext.getReaderContext().getEnvironment();
        return environment.resolvePlaceholders(attributeValue);
    }
}
