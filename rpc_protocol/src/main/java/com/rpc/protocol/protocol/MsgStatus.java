package com.rpc.protocol.protocol;

import lombok.Getter;

/**
 * 自定义消息的状态
 */
public enum MsgStatus {
    SUCCESS(0),
    FAIL(1);

    @Getter
    private final int code;

    MsgStatus(int code) {
        this.code = code;
    }

}
