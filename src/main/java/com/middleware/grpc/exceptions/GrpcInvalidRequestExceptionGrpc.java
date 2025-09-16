package com.middleware.grpc.exceptions;

import io.grpc.Status.Code;

public class GrpcInvalidRequestExceptionGrpc extends GrpcResponseException {

  private final Code code = Code.INVALID_ARGUMENT;

  public GrpcInvalidRequestExceptionGrpc(String message) {
    super(message);
  }

  @Override
  public Code getCode() {
    return code;
  }
}
