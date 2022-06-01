package com.dc.rpc.provider;

import com.dc.rpc.core.RpcProperties;
import com.dc.rpc.registry.RegistryFactory;
import com.dc.rpc.registry.RegistryService;
import com.dc.rpc.registry.RegistryType;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/*
配置类根据配置信息，构建出对应的RpcProvider
 */
@Configuration
@EnableConfigurationProperties(RpcProperties.class)
public class RpcProviderAutoConfiguration {
    //抽取出来的配置信息
    @Resource
    private RpcProperties rpcProperties;
    //将RpcProvider加入到springioc容器中
    @Bean
    public RpcProvider init() throws Exception {

        //获取对应的枚举对象
        RegistryType type = RegistryType.valueOf(rpcProperties.getRegistryType());
        //根据注册中心类型和地址创建出对应的注册中心(静态工厂模式)
        RegistryService serviceRegistry = RegistryFactory.getInstance(rpcProperties.getRegistryAddr(), type);
        //服务提供者暴露的端口     服务注册中心
        return new RpcProvider(rpcProperties.getServicePort(), serviceRegistry);
    }
}
