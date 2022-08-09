package com.dc.rpc.consumer.controller;

import com.mini.rpc.consumer.annotation.RpcReference;
import com.rpc.Providesinterface.HelloFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class HelloController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloController.class);
    @SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection", "SpringJavaInjectionPointsAutowiringInspection"})
    @RpcReference(serviceVersion = "1.0.0", timeout = 5000)
    private HelloFacade helloFacade;
    @Autowired
    ApplicationContext applicationContext;

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String sayHello() throws IOException {
//        byte[] bytes = ProxyGenerator.generateProxyClass("$Proxy48",new Class[]{HelloFacade.class});
//        FileOutputStream os = new FileOutputStream("E://$Proxy48.class");
//        os.write(bytes);
//        os.close();
        return helloFacade.hello("mini rpc");
    }
}
