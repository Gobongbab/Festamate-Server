package com.gobongbob.festamate.domain.image.dto.response;

import com.gobongbob.festamate.domain.image.domain.Image;

public record ImageResponse(
        String name,
        String url
) {

    public static ImageResponse fromEntity(Image image) {
        return new ImageResponse(image.getStoreName(), image.getUrl());
    }
}
