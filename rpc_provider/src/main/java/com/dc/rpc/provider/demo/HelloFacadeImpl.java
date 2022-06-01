package com.dc.rpc.provider.demo;

import com.dc.rpc.provider.annotation.RpcService;

import com.rpc.Providesinterface.HelloFacade;

@RpcService(serviceInterface = HelloFacade.class, serviceVersion = "1.0.0")
public class HelloFacadeImpl implements HelloFacade {
    @Override
    public String hello(String name) {
        return "你好rpc框架"+name;
    }
}
