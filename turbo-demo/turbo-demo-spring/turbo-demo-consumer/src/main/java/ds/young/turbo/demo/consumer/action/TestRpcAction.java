package ds.young.turbo.demo.consumer.action;

import ds.young.turbo.common.annotation.Turbo;
import ds.young.turbo.demo.spring.interfaces.ITestService;
import ds.young.turbo.demo.spring.interfaces.ITestServiceDemo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: turbo
 * @description
 * @author: admin
 * @create: 2020-06-10 17:19
 **/
@RestController
public class TestRpcAction {

    @Turbo
    ITestService testService;

    @Turbo
    ITestServiceDemo testServiceDemo;

    @RequestMapping(value = "/mcd")
    public String helloRpc(String params) throws Exception {

        return testService.sayHaHa() + testServiceDemo.sayHello();

    }
}
