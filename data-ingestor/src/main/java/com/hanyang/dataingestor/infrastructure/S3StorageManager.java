package com.hanyang.dataingestor.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3StorageManager {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    
    private final S3Client s3Client;

    public void deleteFiles(String datasetId) {
        try {
            ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(datasetId + "/")
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsRequest);
            List<S3Object> s3ObjectsList = response.contents();

            if (!s3ObjectsList.isEmpty()) {
                for (S3Object s3Object : s3ObjectsList) {
                    DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(s3Object.key())
                            .build();
                    s3Client.deleteObject(deleteRequest);
                }
            }
        } catch (S3Exception e) {
            //파일 삭제 실패 해도 파일이 없는거니 괜찮다.
        }
    }


    public void processFiles(String datasetId, Consumer<InputStream> fileProcessor) {
        List<String> keys = getAllFilePath(datasetId);
        if (keys.isEmpty()) {
            return;
        }
        
        for (String key : keys) {
            InputStream fileStream = getFileStream(key);
            fileProcessor.accept(fileStream);
        }
    }

    private InputStream getFileStream(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return s3Client.getObject(getObjectRequest);
    }

    public String getFirstFileName(String datasetId) {
        String key = Objects.requireNonNull(getFirstFilePath(datasetId));
        int lastSlashIndex = key.lastIndexOf('/');
        return lastSlashIndex == -1 ? key : key.substring(lastSlashIndex + 1);
    }

    private List<String> getAllFilePath(String datasetId) {
        List<String> keys = new ArrayList<>();
        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(datasetId + "/")
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsRequest);
        List<S3Object> s3ObjectsList = response.contents();

        for (S3Object s3Object : s3ObjectsList) {
            keys.add(s3Object.key());
        }
        return keys;
    }

    private String getFirstFilePath(String datasetId) {
        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(datasetId + "/")
                .delimiter("/")
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsRequest);
        List<S3Object> s3ObjectsList = response.contents();

        return s3ObjectsList.get(0).key();
    }

}
