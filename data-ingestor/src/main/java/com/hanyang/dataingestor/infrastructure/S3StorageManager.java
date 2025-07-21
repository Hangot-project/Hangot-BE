package com.hanyang.dataingestor.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageManager {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    
    private final S3Client s3Client;

    public InputStream getFile(String datasetId) {
        try {
            String key = findFirstFileKey(datasetId);
            if (key == null) {
                return null;
            }
            
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            return s3Client.getObject(getObjectRequest);
            
        } catch (S3Exception e) {
            log.error("S3에서 파일을 찾을 수 없습니다: {} - {}", datasetId, e.getMessage());
            return null;
        }
    }
    
    public String getFirstFileName(String datasetId) {
        try {
            String key = findFirstFileKey(datasetId);
            if (key == null) {
                return null;
            }
            
            int lastSlashIndex = key.lastIndexOf('/');
            return lastSlashIndex == -1 ? key : key.substring(lastSlashIndex + 1);
            
        } catch (Exception e) {
            log.error("파일명 조회 실패: {} - {}", datasetId, e.getMessage());
            return null;
        }
    }

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
                log.info("데이터셋 파일 삭제 완료: {}", datasetId);
            }
        } catch (S3Exception e) {
            log.error("S3 파일 삭제 실패: {} - {}", datasetId, e.getMessage());
        }
    }

    private String findFirstFileKey(String datasetId) {
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
