package com.example.subsidy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SubsidyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullLifecycle_shouldSucceed() throws Exception {
        Map<String, Object> createReq = new HashMap<>();
        createReq.put("unitName", "生命周期测试公司");
        createReq.put("subsidyPeriod", "2024年第三季度");
        createReq.put("amount", new BigDecimal("75000.00"));

        String createResp = mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.unitName").value("生命周期测试公司"))
                .andExpect(jsonPath("$.data.status").value("REGISTERED"))
                .andReturn().getResponse().getContentAsString();

        Long appId = objectMapper.readTree(createResp).path("data").path("id").asLong();

        Map<String, Object> opReq = new HashMap<>();
        opReq.put("operator", "张审核");
        mockMvc.perform(put("/api/applications/" + appId + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(opReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REVIEW_APPROVED"));

        Map<String, Object> reviewInit = new HashMap<>();
        reviewInit.put("initiatedBy", "陈复核");
        String reviewResp = mockMvc.perform(post("/api/reviews/application/" + appId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewInit)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andReturn().getResponse().getContentAsString();

        Long reviewId = objectMapper.readTree(reviewResp).path("data").path("id").asLong();

        mockMvc.perform(get("/api/applications/" + appId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PRE_REVIEWING"));

        Map<String, Object> completeReq = new HashMap<>();
        completeReq.put("riskLevel", "LOW");
        completeReq.put("conclusion", "PASS");
        completeReq.put("handler", "陈复核");
        mockMvc.perform(put("/api/reviews/" + reviewId + "/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conclusion").value("PASS"));

        mockMvc.perform(get("/api/applications/" + appId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REVIEW_APPROVED"));

        Map<String, Object> disburseReq = new HashMap<>();
        disburseReq.put("operator", "赵财务");
        mockMvc.perform(put("/api/applications/" + appId + "/disburse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(disburseReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DISBURSED"));

        mockMvc.perform(get("/api/applications/" + appId + "/flow-records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(5)));
    }

    @Test
    void reviewReturn_andCorrection_shouldSucceed() throws Exception {
        Map<String, Object> createReq = new HashMap<>();
        createReq.put("unitName", "退回修正测试公司");
        createReq.put("subsidyPeriod", "2024年第三季度");
        createReq.put("amount", new BigDecimal("45000.00"));

        String createResp = mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long appId = objectMapper.readTree(createResp).path("data").path("id").asLong();

        Map<String, Object> opReq = new HashMap<>();
        opReq.put("operator", "张审核");
        mockMvc.perform(put("/api/applications/" + appId + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(opReq)))
                .andExpect(status().isOk());

        Map<String, Object> reviewInit = new HashMap<>();
        reviewInit.put("initiatedBy", "陈复核");
        String reviewResp = mockMvc.perform(post("/api/reviews/application/" + appId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewInit)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long reviewId = objectMapper.readTree(reviewResp).path("data").path("id").asLong();

        Map<String, Object> completeReq = new HashMap<>();
        completeReq.put("riskLevel", "MEDIUM");
        completeReq.put("problemDescription", "人员名单与社保不一致");
        completeReq.put("conclusion", "RETURN");
        completeReq.put("handler", "陈复核");
        mockMvc.perform(put("/api/reviews/" + reviewId + "/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conclusion").value("RETURN"));

        mockMvc.perform(get("/api/applications/" + appId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING_CORRECTION"));

        Map<String, Object> correctReq = new HashMap<>();
        correctReq.put("operator", "经办人");
        correctReq.put("remark", "已修正人员名单");
        mockMvc.perform(put("/api/applications/" + appId + "/correct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REVIEW_APPROVED"));
    }

    @Test
    void initiateReview_onDisbursedApp_shouldReturn400() throws Exception {
        Map<String, Object> reviewInit = new HashMap<>();
        reviewInit.put("initiatedBy", "陈复核");
        mockMvc.perform(post("/api/reviews/application/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewInit)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void initiateReview_onRejectedApp_shouldReturn400() throws Exception {
        Map<String, Object> reviewInit = new HashMap<>();
        reviewInit.put("initiatedBy", "陈复核");
        mockMvc.perform(post("/api/reviews/application/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewInit)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("复审未通过")));
    }

    @Test
    void initiateReview_onAlreadyReviewingApp_shouldReturn400() throws Exception {
        Map<String, Object> reviewInit = new HashMap<>();
        reviewInit.put("initiatedBy", "陈复核");
        mockMvc.perform(post("/api/reviews/application/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewInit)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("只有复审通过状态")));
    }

    @Test
    void createApplication_missingUnitName_shouldReturn400() throws Exception {
        Map<String, Object> createReq = new HashMap<>();
        createReq.put("subsidyPeriod", "2024年第三季度");
        createReq.put("amount", new BigDecimal("10000.00"));

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllApplications_shouldReturnSampleData() throws Exception {
        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(6))));
    }

    @Test
    void getNonExistentApp_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/applications/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void getReviewsByApplication_shouldReturnRecords() throws Exception {
        mockMvc.perform(get("/api/reviews/application/6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].conclusion").value("RETURN"));
    }
}
