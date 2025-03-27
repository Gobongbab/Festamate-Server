package com.gobongbob.festamate.domain.image.presentation;

import com.gobongbob.festamate.domain.auth.jwt.domain.CustomMemberDetails;
import com.gobongbob.festamate.domain.image.application.OcrService;
import com.gobongbob.festamate.domain.image.dto.response.StudentInfoResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping()
public class OcrController {

    private final OcrService ocrService;

    @PostMapping("/api/check/student-card")
    public ResponseEntity<StudentInfoResponse> checkStudentCard(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return ResponseEntity.ok(ocrService.checkStudentCard(file, memberDetails.getMember()));
    }
}
