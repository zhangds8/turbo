package ds.young.turbo.demo.provider.service.impl;

import ds.young.turbo.common.annotation.TurboService;
import ds.young.turbo.demo.spring.interfaces.ITestService;

/**
 * @program: turbo
 * @description
 * @author: admin
 * @create: 2020-06-02 18:17
 **/
@TurboService(interfaceClass = ITestService.class)
public class TestServiceImpl implements ITestService {
    public String sayHaHa() throws Exception {
        return "rpc haha";
    }
}
