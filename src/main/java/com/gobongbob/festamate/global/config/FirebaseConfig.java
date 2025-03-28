package com.gobongbob.festamate.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @Value("${FIREBASE_KEY_PATH}")
    private String FIREBASE_KEY_PATH;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        // 프로젝트 루트 경로에서 serviceAccountKey.json 파일을 읽어옴
        FileInputStream serviceAccount = new FileInputStream(FIREBASE_KEY_PATH);

        // Firebase 옵션 설정
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        // 이미 초기화된 앱이 있는지 확인 후 중복 초기화 방지
        if (FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.initializeApp(options);
        } else {
            return FirebaseApp.getInstance();
        }
    }
}