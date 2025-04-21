package com.gobongbob.festamate.domain.image.presentation;

import com.gobongbob.festamate.domain.image.application.OcrService;
import com.gobongbob.festamate.domain.image.dto.response.StudentInfoResponse;
import com.gobongbob.festamate.global.response.SuccessResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping()
public class OcrController implements OcrApi {

    private final OcrService ocrService;

    @Override
    @PostMapping("/api/check/student-card")
    public SuccessResponse<StudentInfoResponse> checkStudentCard(
            @RequestParam(name = "file") MultipartFile file
    ) throws IOException {
        return new SuccessResponse<>(ocrService.checkStudentCard(file));
    }
}