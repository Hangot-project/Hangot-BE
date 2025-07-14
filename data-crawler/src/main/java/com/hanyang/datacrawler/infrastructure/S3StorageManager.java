package com.hanyang.datacrawler.infrastructure;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class S3StorageManager {
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    private final S3Client s3Client;


    public String uploadAndGetUrl(String folderName, String fileName, byte[] fileContent) {
        String s3ObjectPath = folderName + "/" + fileName;
        String type = getFileExtension(fileName);

        PutObjectRequest.Builder putObjectRequestBuilder = PutObjectRequest.builder()
                .bucket(bucket)
                .key(s3ObjectPath);

        if ("pdf".equalsIgnoreCase(type)) {
            putObjectRequestBuilder.contentType("application/pdf");
        } else if ("csv".equalsIgnoreCase(type)) {
            putObjectRequestBuilder.contentType("text/csv; charset=UTF-8");
        } else if ("json".equalsIgnoreCase(type)) {
            putObjectRequestBuilder.contentType("application/json; charset=UTF-8");
        } else if ("xml".equalsIgnoreCase(type)) {
            putObjectRequestBuilder.contentType("application/xml; charset=UTF-8");
        } else if ("xlsx".equalsIgnoreCase(type) || "xls".equalsIgnoreCase(type)) {
            putObjectRequestBuilder.contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }

        s3Client.putObject(putObjectRequestBuilder.build(), RequestBody.fromBytes(fileContent));

        GetUrlRequest getUrlRequest = GetUrlRequest.builder()
                .bucket(bucket)
                .key(s3ObjectPath)
                .build();

        return String.valueOf(s3Client.utilities().getUrl(getUrlRequest));
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    public void deleteFolder(String folderName,Long id) {
        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(folderName+"/"+id+"/")
                .build();
        ListObjectsV2Response listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);

        if (listObjectsResponse.contents().isEmpty()) {
            return;
        }

        List<ObjectIdentifier> objectIdentifiers = new ArrayList<>();
        for (S3Object s3Object : listObjectsResponse.contents()) {
            objectIdentifiers.add(ObjectIdentifier.builder().key(s3Object.key()).build());
        }

        Delete delete = Delete.builder().objects(objectIdentifiers).build();
        DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
                .bucket(bucket)
                .delete(delete)
                .build();

        s3Client.deleteObjects(deleteObjectsRequest);

    }
}
