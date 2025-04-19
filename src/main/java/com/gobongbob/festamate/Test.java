package com.gobongbob.festamate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping
@Slf4j
public class Test {

  @GetMapping("/healthcheck")
  public ResponseEntity<String> healthCheck() {
    return ResponseEntity.ok("Health Check");
  }

  @GetMapping("/sentry")
  public String testSentry() {
    throw new RuntimeException("Sentry 연동 테스트를 위한 고의적인 예외 발생!");
  }

}
