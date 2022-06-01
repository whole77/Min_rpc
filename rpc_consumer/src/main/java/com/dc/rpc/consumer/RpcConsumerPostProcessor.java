package com.dc.rpc.consumer;

import com.dc.rpc.core.RpcConstants;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
public class RpcConsumerPostProcessor implements ApplicationContextAware, BeanClassLoaderAware, BeanFactoryPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcConsumerPostProcessor.class);
    private ApplicationContext context;

    private ClassLoader classLoader;
    //key:服务名称 value:RpcReferenceBean的定义信息
    private final Map<String, BeanDefinition> rpcRefBeanDefinitions = new LinkedHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
       bean扩展点: 在bean定义后实例化前
       该扩展点将注解修饰的成员变量变为能被rpc调用的自定义bean对象并注入容器中去
     **/
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        //获取所有bean的定义名称
        for (String beanDefinitionName : beanFactory.getBeanDefinitionNames()) {
            //通过名称获取bean的定义
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);
            //获取bean的类名
            String beanClassName = beanDefinition.getBeanClassName();
            if (beanClassName != null) {
                //通过类名和类的加载器活得Class对象
                Class<?> clazz = ClassUtils.resolveClassName(beanClassName, this.classLoader);
                //将该类的所有字段作为parseRpcReference参数调用
                ReflectionUtils.doWithFields(clazz, this::parseRpcReference);
            }
        }
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        //将bean的定义注册到spring容器中
        this.rpcRefBeanDefinitions.forEach((beanName, beanDefinition) -> {
            if (context.containsBean(beanName)) {
                throw new IllegalArgumentException("spring context already has a bean named " + beanName);
            }
            registry.registerBeanDefinition(beanName, rpcRefBeanDefinitions.get(beanName));
            log.info("registered RpcReferenceBean {} success.", beanName);
        });
    }

    /**
     * addPropertyValue为bean实例添加字段
     * getAnnotation 获取指定类、方法、字段、构造等上的注解列表
     * @param field = 类中的实例字段
     */
    private void parseRpcReference(Field field) {
        //获取RpcReference注解信息
        com.mini.rpc.consumer.annotation.RpcReference annotation = AnnotationUtils.getAnnotation(field, com.mini.rpc.consumer.annotation.RpcReference.class);
        if (annotation != null) {
            //创建一个用于构造泛型的新定义
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RpcReferenceBean.class);
            //设置bean的初始化方法
            builder.setInitMethodName(RpcConstants.INIT_METHOD_NAME);
            //服务类型(接口类型)
//            LOGGER.warn(field.getType().toString());
//            Class<?>[] classes = {field.getType()};
//            LOGGER.warn(new Class<?>[]{field.getType()}.toString());
            builder.addPropertyValue("interfaceClass", field.getType());
            //添加版本号
            builder.addPropertyValue("serviceVersion", annotation.serviceVersion());
            //添加注册中心类型
            builder.addPropertyValue("registryType", annotation.registryType());
            //添加注册中心地址
            builder.addPropertyValue("registryAddr", annotation.registryAddress());
            //添加超时时间
            builder.addPropertyValue("timeout", annotation.timeout());
            //构建出对应的bean定义信息
            BeanDefinition beanDefinition = builder.getBeanDefinition();
            //key:服务名称 value：bean的定义信息
            rpcRefBeanDefinitions.put(field.getName(), beanDefinition);
        }
    }

}
