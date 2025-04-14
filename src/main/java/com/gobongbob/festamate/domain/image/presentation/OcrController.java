package com.gobongbob.festamate.domain.image.presentation;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.image.application.OcrService;
import com.gobongbob.festamate.domain.image.dto.response.StudentInfoResponse;
import java.io.IOException;

import com.gobongbob.festamate.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping()
@Tag(name = "OCR", description = "OCR 관련 API")
public class OcrController {

    private final OcrService ocrService;

    @Operation(summary = "학생증 인증", description = "학생증 이미지를 업로드하여 학생 정보를 인증합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청에 성공하였습니다.")
    })
    @PostMapping("/api/check/student-card")
    public SuccessResponse<StudentInfoResponse> checkStudentCard(
            @Parameter(description = "인증된 사용자 정보", hidden = true)
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @Parameter(name = "file", description = "학생증 이미지 파일", required = true)
            @RequestParam(name = "file") MultipartFile file
    ) throws IOException {
        return new SuccessResponse<>(ocrService.checkStudentCard(file, memberDetails.getMember()));
    }
}