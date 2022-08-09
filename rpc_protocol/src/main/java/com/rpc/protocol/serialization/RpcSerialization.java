package com.rpc.protocol.serialization;

import com.dc.rpc.core.annotation.SPI;

import java.io.IOException;

/**
 * @Author: LaiLai
 * @Date: 2022/05/04/9:00
 */
@SPI
public interface RpcSerialization {//序列化接口
    <T> byte[] serialize(T obj) throws IOException;

    <T> T deserialize(byte[] data, Class<T> clz) throws IOException;
}
