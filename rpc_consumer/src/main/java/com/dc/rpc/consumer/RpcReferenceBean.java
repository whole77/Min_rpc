package com.dc.rpc.consumer;


import com.dc.rpc.registry.RegistryFactory;
import com.dc.rpc.registry.RegistryService;
import com.dc.rpc.registry.RegistryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

/**
 * 根据被注解RpcRefererence修饰的成员变量，构造出能够被Rpc调用的自定义bean注册到spring容器中
 * @author lailai
 *
 */
public class RpcReferenceBean implements FactoryBean<Object> {
    private static final Logger LOOGER = LoggerFactory.getLogger(RpcReferenceBean.class);
    //服务类型名称(接口类型)
    private Class<?> interfaceClass;
    //服务版本
    private String serviceVersion;
    //注册中心类型
    private String registryType;
    //注册中心地址
    private String registryAddr;
    //超时时间
    private long timeout;
    //生成的动态代理类
    private Object object;

    @Override
    public Object getObject() throws Exception {
        return object;
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

    public void init() throws Exception {
        //工厂模式根据类型返回注册中心实现类
        RegistryService registryService = RegistryFactory.getInstance(this.registryAddr, RegistryType.valueOf(this.registryType));
        //JDK动态代理1:要么用类实现接口InvocationHandler实现其方法
        //要么使用Proxy动态创建代理类的工厂类，第一个参数类的加载器，第二个参数代理类实现的接口，第三个接口实现的方法
        this.object = Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new RpcInvokerProxy(serviceVersion, timeout, registryService));
    }

    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public void setRegistryType(String registryType) {
        this.registryType = registryType;
    }

    public void setRegistryAddr(String registryAddr) {
        this.registryAddr = registryAddr;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
