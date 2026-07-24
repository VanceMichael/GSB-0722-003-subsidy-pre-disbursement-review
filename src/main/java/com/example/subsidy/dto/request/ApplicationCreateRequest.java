package com.example.subsidy.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class ApplicationCreateRequest {

    @NotBlank(message = "单位名称不能为空")
    @Size(max = 200, message = "单位名称长度不能超过200")
    private String unitName;

    @NotBlank(message = "补贴周期不能为空")
    @Size(max = 50, message = "补贴周期长度不能超过50")
    private String subsidyPeriod;

    @NotNull(message = "申请金额不能为空")
    @DecimalMin(value = "0.01", message = "申请金额必须大于0")
    @Digits(integer = 13, fraction = 2, message = "金额格式不正确，最多13位整数2位小数")
    private BigDecimal amount;

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
    public String getSubsidyPeriod() { return subsidyPeriod; }
    public void setSubsidyPeriod(String subsidyPeriod) { this.subsidyPeriod = subsidyPeriod; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
