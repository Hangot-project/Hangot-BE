package com.hanyang.dataingestor.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageManager {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    
    private final S3Client s3Client;

    public void deleteDatasetFiles(String datasetId) {
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
                    log.debug("S3 파일 삭제 완료: {}", s3Object.key());
                }
            }
        } catch (S3Exception e) {
            log.error("S3 파일 삭제 실패: {} - {}", datasetId, e.getMessage());
        }
    }

    public List<InputStream> getAllFiles(String datasetId) {
        List<InputStream> files = new ArrayList<>();
        try {
            List<String> keys = getAllFileKeys(datasetId);
            for (String key : keys) {
                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build();
                files.add(s3Client.getObject(getObjectRequest));
            }
        } catch (Exception e) {
            log.error("S3 파일들 조회 실패: {} - {}", datasetId, e.getMessage());
        }
        return files;
    }

    public String getFirstFileName(String datasetId) {
        String key = Objects.requireNonNull(getFirstFileKey(datasetId));
        int lastSlashIndex = key.lastIndexOf('/');
        return lastSlashIndex == -1 ? key : key.substring(lastSlashIndex + 1);
    }

    private List<String> getAllFileKeys(String datasetId) {
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

    private String getFirstFileKey(String datasetId) {
        try {
            ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(datasetId + "/")
                    .delimiter("/")
                    .build();

            ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsRequest);
            List<S3Object> s3ObjectsList = response.contents();

            if (s3ObjectsList.isEmpty()) {
                log.error("데이터셋에 해당하는 파일이 없습니다: {}", datasetId);
                return null;
            }

            return s3ObjectsList.get(0).key();
        } catch (S3Exception e) {
            log.error("S3 파일 목록 조회 실패: {} - {}", datasetId, e.getMessage());
            return null;
        }
    }
}
