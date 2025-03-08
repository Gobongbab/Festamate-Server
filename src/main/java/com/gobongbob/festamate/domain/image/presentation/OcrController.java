package com.gobongbob.festamate.domain.image.presentation;

import com.gobongbob.festamate.domain.image.application.OcrService;
import com.gobongbob.festamate.domain.member.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping()
public class OcrController {

    private final OcrService ocrService;

    @PostMapping("/api/check/student-card")
    public ResponseEntity<Void> checkStudentCard(@RequestParam("file") MultipartFile file) throws IOException {
        Long memberId = 1L; // 추후 Spring Security를 활용하여 사용자 정보를 가져오도록 변경 필요
        Member member = ocrService.checkStudentCard(file,memberId);

        return ResponseEntity.ok().build();
    }
}
