package com.gov.subsidy.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gov.subsidy.review.domain.entity.SubsidyApplication;
import com.gov.subsidy.review.domain.enums.ApplicationStatus;
import com.gov.subsidy.review.dto.CreateApplicationRequest;
import com.gov.subsidy.review.dto.DecideReviewRequest;
import com.gov.subsidy.review.dto.DisburseRequest;
import com.gov.subsidy.review.dto.ReReviewRequest;
import com.gov.subsidy.review.dto.StartReviewRequest;
import com.gov.subsidy.review.domain.enums.ReviewConclusion;
import com.gov.subsidy.review.domain.enums.RiskLevel;
import com.gov.subsidy.review.repository.SubsidyApplicationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 接口层集成测试，覆盖登记、发起复核、退回及非法状态的 HTTP 行为。
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SubsidyApplicationRepository applicationRepository;

    private Long persist(ApplicationStatus status) {
        LocalDateTime now = LocalDateTime.now();
        return applicationRepository.save(SubsidyApplication.builder()
                .employerName("接口测试单位")
                .subsidyPeriod("2026-Q1")
                .appliedAmount(new BigDecimal("88888.88"))
                .status(status)
                .createdAt(now)
                .updatedAt(now)
                .build()).getId();
    }

    @Test
    void register_thenGet_shouldReturnCreatedAndRegisteredStatus() throws Exception {
        var body = objectMapper.writeValueAsString(
                new CreateApplicationRequest("新登记单位", "2026-Q2", new BigDecimal("66000.00")));

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REGISTERED"))
                .andExpect(jsonPath("$.employerName").value("新登记单位"));
    }

    @Test
    void register_shouldReturn400_whenAmountInvalid() throws Exception {
        var body = objectMapper.writeValueAsString(
                new CreateApplicationRequest("非法金额单位", "2026-Q2", new BigDecimal("0")));

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void startReview_shouldReturn201_whenReReviewPassed() throws Exception {
        Long appId = persist(ApplicationStatus.RE_REVIEW_PASSED);
        var body = objectMapper.writeValueAsString(
                new StartReviewRequest(RiskLevel.MEDIUM, "抽查发现疑点", "复核员王五"));

        mockMvc.perform(post("/api/applications/" + appId + "/reviews")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reviewStatus").value("OPEN"));
    }

    @Test
    void startReview_shouldReturn409_whenDisbursed() throws Exception {
        Long appId = persist(ApplicationStatus.DISBURSED);
        var body = objectMapper.writeValueAsString(
                new StartReviewRequest(RiskLevel.LOW, "已拨付不应可复核", "复核员王五"));

        mockMvc.perform(post("/api/applications/" + appId + "/reviews")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void fullReturnFlow_shouldEndInCorrectableWithFlowRecords() throws Exception {
        Long appId = persist(ApplicationStatus.RE_REVIEW_PASSED);
        var startBody = objectMapper.writeValueAsString(
                new StartReviewRequest(RiskLevel.HIGH, "材料缺失", "复核员王五"));

        var startResult = mockMvc.perform(post("/api/applications/" + appId + "/reviews")
                        .contentType(MediaType.APPLICATION_JSON).content(startBody))
                .andExpect(status().isCreated())
                .andReturn();
        Long reviewId = objectMapper.readTree(startResult.getResponse().getContentAsString()).get("id").asLong();

        var decideBody = objectMapper.writeValueAsString(
                new DecideReviewRequest(ReviewConclusion.RETURNED, "复核员王五", "退回补充材料"));
        mockMvc.perform(post("/api/reviews/" + reviewId + "/decision")
                        .contentType(MediaType.APPLICATION_JSON).content(decideBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conclusion").value("RETURNED"));

        mockMvc.perform(get("/api/applications/" + appId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CORRECTABLE"));

        mockMvc.perform(get("/api/applications/" + appId + "/flow-records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void endToEnd_registerThroughDisbursement_viaHttp() throws Exception {
        // 1. 登记
        var createBody = objectMapper.writeValueAsString(
                new CreateApplicationRequest("端到端单位", "2026-Q3", new BigDecimal("99000.00")));
        var createResult = mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON).content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REGISTERED"))
                .andReturn();
        Long appId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // 2. 复审通过
        mockMvc.perform(post("/api/applications/" + appId + "/re-review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReReviewRequest(true, "复审员赵六", "初审无误"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RE_REVIEW_PASSED"));

        // 3. 发起复核
        var startResult = mockMvc.perform(post("/api/applications/" + appId + "/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new StartReviewRequest(RiskLevel.MEDIUM, "抽查核对", "复核员王五"))))
                .andExpect(status().isCreated())
                .andReturn();
        Long reviewId = objectMapper.readTree(startResult.getResponse().getContentAsString()).get("id").asLong();

        // 4. 复核通过
        mockMvc.perform(post("/api/reviews/" + reviewId + "/decision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new DecideReviewRequest(ReviewConclusion.APPROVED, "复核员王五", "核查无异常"))))
                .andExpect(status().isOk());

        // 5. 拨付
        mockMvc.perform(post("/api/applications/" + appId + "/disbursement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DisburseRequest("出纳王七", "已拨付"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISBURSED"));

        // 完整流转记录：登记 + 复审通过 + 发起复核 + 复核通过 + 拨付 = 5
        mockMvc.perform(get("/api/applications/" + appId + "/flow-records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void reReview_shouldReturn409_whenApplicationAlreadyDisbursed() throws Exception {
        Long appId = persist(ApplicationStatus.DISBURSED);
        mockMvc.perform(post("/api/applications/" + appId + "/re-review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReReviewRequest(true, "复审员赵六", "x"))))
                .andExpect(status().isConflict());
    }
}
