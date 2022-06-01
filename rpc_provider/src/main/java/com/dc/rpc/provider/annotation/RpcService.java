package com.dc.rpc.provider.annotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @Author: LaiLai
 * @Date: 2022/05/03/20:32
 */
/*
  服务提供者几个步骤
        1：讲服务端口暴露，然后讲服务端口与注册中心配置解耦
        2：标识需要发布的服务列表，然后将bean进行初始化后处理，将servicemate发布在发布在注册中心
        3：等在客户端连接发送请求然后解析
        4：然后将请求放在线程池中处理返回结果给客户端
        使用Component为了将标识的服务类型与接口实现类相绑定然后一起构建出服务元数据信息
        消费者消费服务时需要完全相同的属性才能拿去到对应服务结点提供的服务
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface RpcService {//服务注册接口
    //服务类型(接口名称)
    Class<?> serviceInterface() default Object.class;
    //服务版本
    String serviceVersion() default "1.0";
}
