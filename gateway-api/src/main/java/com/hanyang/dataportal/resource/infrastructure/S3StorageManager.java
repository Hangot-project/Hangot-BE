package com.hanyang.dataportal.resource.infrastructure;

import com.hanyang.dataportal.core.exception.FileException;
import com.hanyang.dataportal.dataset.domain.vo.Type;
import com.hanyang.dataportal.resource.infrastructure.dto.FileInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class S3StorageManager {
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    private final S3Client s3Client;

    public FileInfoDto uploadFile(String folderName,Long id, MultipartFile multipartFile){

        String fileName = multipartFile.getOriginalFilename();

        assert fileName != null;
        String s3ObjectPath = folderName + "/" + id + "/" + fileName;
        String type = fileName.split("\\.")[1];

        PutObjectRequest.Builder putObjectRequestBuilder = PutObjectRequest.builder()
                .bucket(bucket)
                .key(s3ObjectPath);

        if ("pdf".equalsIgnoreCase(type)) {
            putObjectRequestBuilder.contentType(multipartFile.getContentType());
        }

        try {
            s3Client.putObject(putObjectRequestBuilder.build(), RequestBody.fromBytes(multipartFile.getBytes()));
        } catch (IOException e) {
            throw new FileException("s3 서버 오류");
        }

        GetUrlRequest getUrlRequest = GetUrlRequest.builder()
                .bucket(bucket)
                .key(s3ObjectPath)
                .build();

        FileInfoDto fileInfoDto = new FileInfoDto();
        fileInfoDto.setUrl(String.valueOf(s3Client.utilities().getUrl(getUrlRequest)));
        fileInfoDto.setType(Type.findByType(type));

        return fileInfoDto;
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
