package com.gobongbob.festamate.domain.image.infrastructure;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.gobongbob.festamate.domain.image.domain.Image;
import com.gobongbob.festamate.domain.image.dto.StoreImageDto;
import com.gobongbob.festamate.domain.image.persistence.ImageStoreProcessor;
import java.io.IOException;
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
}
