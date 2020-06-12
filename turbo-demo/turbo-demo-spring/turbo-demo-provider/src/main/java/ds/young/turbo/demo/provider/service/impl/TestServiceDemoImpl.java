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
