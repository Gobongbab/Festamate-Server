package com.gobongbob.festamate.domain.image.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Image {

	private String uploadName;

	@Column(unique = true)
	private String storeName;

	private String url;
}
