package com.example.subsidy.controller;

import com.example.subsidy.common.BusinessException;
import com.example.subsidy.dto.ApplicationCreateRequest;
import com.example.subsidy.dto.ApplicationDTO;
import com.example.subsidy.dto.OperatorRequest;
import com.example.subsidy.dto.ReviewCloseRequest;
import com.example.subsidy.dto.ReviewCreateRequest;
import com.example.subsidy.dto.ReviewDTO;
import com.example.subsidy.enums.ApplicationStatus;
import com.example.subsidy.enums.ReviewConclusion;
import com.example.subsidy.enums.ReviewStatus;
import com.example.subsidy.enums.RiskLevel;
import com.example.subsidy.service.PreDisbursementReviewService;
import com.example.subsidy.service.SubsidyApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubsidyApplicationService applicationService;

    @MockBean
    private PreDisbursementReviewService reviewService;

    @Test
    void registerApplication_shouldReturnOk() throws Exception {
        ApplicationCreateRequest req = ApplicationCreateRequest.builder()
                .unitName("测试单位").subsidyPeriod("2024-Q1").amount(new BigDecimal("10000.00")).build();
        ApplicationDTO resp = ApplicationDTO.builder()
                .id(1L).unitName("测试单位").subsidyPeriod("2024-Q1")
                .amount(new BigDecimal("10000.00")).status(ApplicationStatus.REGISTERED)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        when(applicationService.register(any())).thenReturn(resp);

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("REGISTERED"));
    }

    @Test
    void registerApplication_validationError_shouldReturn400() throws Exception {
        ApplicationCreateRequest req = ApplicationCreateRequest.builder()
                .unitName("").subsidyPeriod("").amount(null).build();

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listApplications_shouldReturnList() throws Exception {
        when(applicationService.findAll()).thenReturn(List.of(
                ApplicationDTO.builder().id(1L).unitName("A").status(ApplicationStatus.REGISTERED).build(),
                ApplicationDTO.builder().id(2L).unitName("B").status(ApplicationStatus.REVIEW_APPROVED).build()
        ));

        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void businessException_shouldReturn400WithMessage() throws Exception {
        when(reviewService.initiateReview(eq(1L), any()))
                .thenThrow(new BusinessException("申请已拨付，不能发起复核"));

        ReviewCreateRequest req = ReviewCreateRequest.builder()
                .riskLevel(RiskLevel.LOW).processor("复核人").build();
        mockMvc.perform(post("/api/reviews/application/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("申请已拨付，不能发起复核"));
    }

    @Test
    void closeReview_shouldReturnUpdatedReview() throws Exception {
        ReviewDTO resp = ReviewDTO.builder()
                .id(1L).applicationId(1L).riskLevel(RiskLevel.HIGH)
                .conclusion(ReviewConclusion.RETURN).status(ReviewStatus.CLOSED)
                .processor("复核人").build();
        when(reviewService.closeReview(eq(1L), any())).thenReturn(resp);

        ReviewCloseRequest req = ReviewCloseRequest.builder()
                .conclusion(ReviewConclusion.RETURN).processor("复核人").build();
        mockMvc.perform(post("/api/reviews/1/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"))
                .andExpect(jsonPath("$.data.conclusion").value("RETURN"));
    }

    @Test
    void submitForReview_shouldReturnOk() throws Exception {
        ApplicationDTO resp = ApplicationDTO.builder()
                .id(1L).status(ApplicationStatus.UNDER_REVIEW).build();
        when(applicationService.submitForReview(eq(1L), any())).thenReturn(resp);

        mockMvc.perform(post("/api/applications/1/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(OperatorRequest.builder().operator("张三").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UNDER_REVIEW"));
    }
}
