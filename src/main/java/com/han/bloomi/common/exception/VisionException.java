package com.han.bloomi.common.exception;

import com.han.bloomi.common.error.ErrorCode;

public class VisionException extends BusinessException {
    public VisionException(ErrorCode errorCode) {
        super(errorCode);
    }

    public VisionException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public VisionException(ErrorCode errorCode, String detail, Throwable cause) {
        super(errorCode, detail, cause);
    }
}