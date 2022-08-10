package com.rpc.protocol.serialization.kryo;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.rpc.protocol.serialization.RpcSerialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @Author: LaiLai
 * @Date: 2022/08/10/16:26
 */
public class kryoSerialization implements RpcSerialization {
    private static Logger logger = LoggerFactory.getLogger(kryoSerialization.class);
    /**
     * 由于kryo不是线程安全的，所以每个线程都使用独立的kryo
     * withInitial为threadlocal静态内部类中的SuppliedThreadLocal中的成员变量supplier赋予值，并且重写了父类中的initialValue
     * supplier接口上面加了函数时注解@FunctionalInterface那么就用lambda来表达其中的一个实现
     * 调用get方法后会调用setInitialValue()然后会调用initialValue()方法从而获取到lambda中的值
     */
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            // Object->byte:将对象序列化为byte数组
            kryo.writeObject(output, obj);
            kryoThreadLocal.remove();
            return output.toBytes();
        } catch (Exception e) {
            logger.error("kryo序列化异常"+e);
        }
        return new byte[0];
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            // byte->Object:从byte数组中反序列化出对对象
            Object o = kryo.readObject(input, clazz);
            //threadMap中key为threadLocal的弱引用使用完后需要remove()
            kryoThreadLocal.remove();
            return clazz.cast(o);
        } catch (Exception e) {
            logger.error("kryo反序列化异常"+e);
        }
        return null;
    }
}
