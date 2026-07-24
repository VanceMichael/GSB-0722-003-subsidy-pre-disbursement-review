package com.gov.subsidy.review.exception;

/** 业务规则冲突异常，映射为 HTTP 409。 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
