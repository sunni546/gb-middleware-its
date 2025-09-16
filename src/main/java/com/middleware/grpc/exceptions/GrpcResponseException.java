package com.middleware.grpc.exceptions;

import com.middleware.common.exceptions.BaseException;
import io.grpc.Status.Code;

public abstract class GrpcResponseException extends BaseException {

  public GrpcResponseException(String message) {
    super(message);
  }

  public GrpcResponseException(String message, Throwable cause) {
    super(message, cause);
  }

  public abstract Code getCode();
}
