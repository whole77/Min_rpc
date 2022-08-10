package com.rpc.protocol.serialization.protobuf;

import com.rpc.protocol.serialization.RpcSerialization;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: LaiLai
 * @Date: 2022/08/10/14:31
 * 使用protobuf的实现Protostuff从而不需要定义.protof文件
 */
@SuppressWarnings("unchecked")
public class ProtobufSerialization implements RpcSerialization {
    //缓存提高效率
    private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();
    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(cls);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clz) throws IOException {
        try {
            T message = clz.newInstance();
            Schema<T> schema = getSchema(clz);
            ProtostuffIOUtil.mergeFrom(data, message, schema);
            return message;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static <T> Schema<T> getSchema(Class<T> cls) {
        Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(cls);
            if (schema != null) {
                cachedSchema.put(cls, schema);
            }
        }
        return schema;
    }
}
