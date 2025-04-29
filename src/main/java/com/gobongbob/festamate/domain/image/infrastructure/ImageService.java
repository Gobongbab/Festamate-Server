package com.gobongbob.festamate.domain.image.infrastructure;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.gobongbob.festamate.domain.image.domain.Image;
import com.gobongbob.festamate.domain.image.dto.StoreImageDto;
import com.gobongbob.festamate.domain.image.persistence.ImageStoreProcessor;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.StreamUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageStoreProcessor imageStoreProcessor;
    private final AmazonS3 amazonS3Client;

    @Value("${cloud.aws.s3}")
    private String bucket;

    public List<Image> uploadImages(List<MultipartFile> imageFiles) {
        List<StoreImageDto> storeImageDtos = imageStoreProcessor.storeImageFiles(imageFiles);

        return StreamUtils.zip(imageFiles.stream(), storeImageDtos.stream(), this::uploadImageFile)
                .toList();
    }

    private Image uploadImageFile(MultipartFile multipartFile, StoreImageDto storeImageDto) {
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(multipartFile.getContentType());
            objectMetadata.setContentLength(multipartFile.getSize());
            objectMetadata.setContentDisposition("inline");

            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucket,
                    storeImageDto.storeName(),
                    multipartFile.getInputStream(),
                    objectMetadata
            );
            amazonS3Client.putObject(putObjectRequest);

            String storeName = storeImageDto.storeName();
            String uploadUrl = amazonS3Client.getUrl(bucket, storeName).toString();

            return Image.builder()
                    .storeName(storeName)
                    .uploadName(storeImageDto.uploadName())
                    .url(uploadUrl)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }
    }

    @Transactional
    public void delete(Image image) {
        String storeName = image.getStoreName();
        amazonS3Client.deleteObject(bucket, storeName);
    }

    // 단일 사진 업로드
    public Image uploadImage(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("업로드할 이미지 파일이 비어있습니다.");
        }
        StoreImageDto storeImageDto;
        try {
            List<StoreImageDto> dtos = imageStoreProcessor.storeImageFiles(
                    Collections.singletonList(imageFile));
            if (dtos == null || dtos.isEmpty()) {
                throw new RuntimeException("이미지 파일 정보 생성에 실패했습니다.");
            }
            storeImageDto = dtos.get(0);
        } catch (Exception e) {
            throw new RuntimeException("이미지 파일 정보 처리 중 오류 발생", e);
        }

        return this.uploadImageFile(imageFile, storeImageDto);
    }
}
