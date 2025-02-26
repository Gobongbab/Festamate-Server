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
@RequestMapping("/")
@Slf4j
public class Test {


  @GetMapping("/health")
  public SuccessResponse<String> healthCheck() {

    return ResponseEntity.ok().build();
  }
}
