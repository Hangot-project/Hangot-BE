package com.hanyang.dataingestor.infrastructure;

import com.hanyang.dataingestor.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class S3StorageManager {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    
    private final S3Client s3Client;
    private static final String FOLDER_NAME = "Resource";

    public InputStream getFile(String datasetId) {
        try {
            String key = findFirstFileKey(datasetId);
            
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            return s3Client.getObject(getObjectRequest);
            
        } catch (S3Exception e) {
            throw new ResourceNotFoundException("S3에서 파일을 찾을 수 없습니다: " + datasetId);
        }
    }

    private String findFirstFileKey(String datasetId) {
        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(FOLDER_NAME + "/" + datasetId + "/")
                .maxKeys(1) // 첫 번째 파일만 필요
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(listObjectsRequest);
        List<S3Object> s3ObjectsList = response.contents();

        if (s3ObjectsList.isEmpty()) {
            throw new ResourceNotFoundException("데이터셋에 해당하는 파일이 없습니다: " + datasetId);
        }

        return s3ObjectsList.get(0).key();
    }
}
