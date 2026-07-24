package com.gov.subsidy.review.exception;

/** 资源不存在异常，映射为 HTTP 404。 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
