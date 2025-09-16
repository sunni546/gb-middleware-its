package com.middleware.grpc.exceptions;

import io.grpc.Status.Code;

public class GrpcEdgeNotExistException extends GrpcResponseException {

  public GrpcEdgeNotExistException(String message) {
    super(message);
  }

  public GrpcEdgeNotExistException(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public Code getCode() {
    return Code.NOT_FOUND;
  }
}
