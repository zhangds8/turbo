package ds.young.turbo.spring.schema;

import ds.young.turbo.spring.TurboBean;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author &#xFF1A;zhangds5
 * @date &#xFF1A;Created in 2020/5/24 20:53
 * @description&#xFF1A;
 * @modified By&#xFF1A;
 * @version: $
 */

public class TurboNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("turboService", new TurboBeanDefinitionParser(TurboBean.class, true));
    }
}
