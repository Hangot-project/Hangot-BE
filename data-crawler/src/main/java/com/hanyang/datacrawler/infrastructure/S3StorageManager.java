package com.hanyang.datacrawler.infrastructure;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class S3StorageManager {
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    private final S3Client s3Client;


    public String uploadAndGetUrl(String folderName, String fileName, InputStream inputStream) {
        String s3ObjectPath = folderName + "/" + fileName;
        String extension = getFileExtension(fileName);
        FileType fileType = FileType.fromExtension(extension);

        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3ObjectPath)
                    .build();
            
            s3Client.headObject(headObjectRequest);
            
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3ObjectPath)
                    .build();
            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            // 파일이 존재하지 않는 경우 무시
        }

        try {
            uploadWithMultipart(s3ObjectPath, inputStream, fileType.getContentType());
        } catch (IOException e) {
            throw new RuntimeException("S3 멀티파트 업로드 실패", e);
        }

        GetUrlRequest getUrlRequest = GetUrlRequest.builder()
                .bucket(bucket)
                .key(s3ObjectPath)
                .build();

        return String.valueOf(s3Client.utilities().getUrl(getUrlRequest));
    }

    private void uploadWithMultipart(String s3ObjectPath, InputStream inputStream, String contentType) throws IOException {
        CreateMultipartUploadRequest createRequest = CreateMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(s3ObjectPath)
                .contentType(contentType)
                .build();

        CreateMultipartUploadResponse createResponse = s3Client.createMultipartUpload(createRequest);
        String uploadId = createResponse.uploadId();

        List<CompletedPart> completedParts = new ArrayList<>();
        byte[] buffer = new byte[5 * 1024 * 1024]; // 5MB chunks
        int partNumber = 1;

        try {
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] partData = new byte[bytesRead];
                System.arraycopy(buffer, 0, partData, 0, bytesRead);

                UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                        .bucket(bucket)
                        .key(s3ObjectPath)
                        .uploadId(uploadId)
                        .partNumber(partNumber)
                        .build();

                UploadPartResponse uploadPartResponse = s3Client.uploadPart(uploadPartRequest, 
                    RequestBody.fromBytes(partData));

                CompletedPart completedPart = CompletedPart.builder()
                        .partNumber(partNumber)
                        .eTag(uploadPartResponse.eTag())
                        .build();

                completedParts.add(completedPart);
                partNumber++;
            }

            CompletedMultipartUpload completedUpload = CompletedMultipartUpload.builder()
                    .parts(completedParts)
                    .build();

            CompleteMultipartUploadRequest completeRequest = CompleteMultipartUploadRequest.builder()
                    .bucket(bucket)
                    .key(s3ObjectPath)
                    .uploadId(uploadId)
                    .multipartUpload(completedUpload)
                    .build();

            s3Client.completeMultipartUpload(completeRequest);

        } catch (Exception e) {
            // 실패 시 멀티파트 업로드 중단
            AbortMultipartUploadRequest abortRequest = AbortMultipartUploadRequest.builder()
                    .bucket(bucket)
                    .key(s3ObjectPath)
                    .uploadId(uploadId)
                    .build();
            s3Client.abortMultipartUpload(abortRequest);
            throw new IOException("멀티파트 업로드 실패", e);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }
}
