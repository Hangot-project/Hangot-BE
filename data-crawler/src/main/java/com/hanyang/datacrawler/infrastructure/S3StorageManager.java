package com.hanyang.datacrawler.infrastructure;


import lombok.RequiredArgsConstructor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class S3StorageManager {
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    
    @Value("${data.batch.size}")
    private int batchSize;
    
    private final S3Client s3Client;


    public String uploadAndGetUrl(String folderName, String fileName, InputStream inputStream) {
        String extension = getFileExtension(fileName);
        FileType fileType = FileType.fromExtension(extension);
        
        // Excel 파일인 경우 청킹 방식 사용
        if (isExcelFile(fileType)) {
            return uploadExcelInChunks(folderName, fileName, inputStream);
        }
        
        // 기존 방식 (non-Excel 파일)
        String s3ObjectPath = folderName + "/" + fileName;

        // 해당 폴더 내 모든 파일 삭제
        deleteAllFilesInFolder(folderName);

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

    private void deleteAllFilesInFolder(String folderName) {
        try {
            ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(folderName + "/")
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
            // 폴더가 존재하지 않거나 삭제 실패 시 무시
        }
    }

    public String uploadExcelInChunks(String folderName, String fileName, InputStream inputStream) {
        String extension = getFileExtension(fileName);
        FileType fileType = FileType.fromExtension(extension);
        
        // Excel 파일이 아닌 경우 기존 방식 사용
        if (!isExcelFile(fileType)) {
            return uploadAndGetUrl(folderName, fileName, inputStream);
        }
        
        // 해당 폴더 내 모든 파일 삭제
        deleteAllFilesInFolder(folderName);
        
        try {
            ExcelChunkHandler chunkHandler = new ExcelChunkHandler(folderName, batchSize);
            processExcelWithChunking(inputStream, chunkHandler);
            
            // 첫 번째 청크 파일의 URL 반환
            String firstChunkPath = folderName + "/chunk_0.csv";
            GetUrlRequest getUrlRequest = GetUrlRequest.builder()
                    .bucket(bucket)
                    .key(firstChunkPath)
                    .build();
            
            return String.valueOf(s3Client.utilities().getUrl(getUrlRequest));
            
        } catch (Exception e) {
            throw new RuntimeException("Excel 청킹 업로드 실패", e);
        }
    }
    
    private boolean isExcelFile(FileType fileType) {
        return fileType == FileType.XLS || fileType == FileType.XLSX;
    }
    
    private void processExcelWithChunking(InputStream inputStream, ExcelChunkHandler chunkHandler) throws Exception {
        try (OPCPackage opcPackage = OPCPackage.open(inputStream)) {
            XSSFReader xssfReader = new XSSFReader(opcPackage);
            StylesTable stylesTable = xssfReader.getStylesTable();
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(opcPackage);
            
            try (InputStream sheetStream = xssfReader.getSheetsData().next()) {
                InputSource sheetSource = new InputSource(sheetStream);
                
                ContentHandler handler = new XSSFSheetXMLHandler(stylesTable, strings, chunkHandler, false);
                XMLReader sheetParser = XMLHelper.newXMLReader();
                
                sheetParser.setContentHandler(handler);
                sheetParser.parse(sheetSource);
                
                // 남은 데이터 처리
                chunkHandler.finalizeChunks();
            }
        }
    }
    
    private class ExcelChunkHandler implements XSSFSheetXMLHandler.SheetContentsHandler {
        private final String folderName;
        private final int chunkSize;
        private final List<String> header = new ArrayList<>();
        private final List<List<String>> currentChunk = new ArrayList<>();
        private final List<String> currentRow = new ArrayList<>();
        
        private int headerRowIndex = -1;
        private int checkedCol = -1;
        private int chunkIndex = 0;
        
        public ExcelChunkHandler(String folderName, int chunkSize) {
            this.folderName = folderName;
            this.chunkSize = chunkSize;
        }
        
        @Override
        public void startRow(int rowNum) {
            this.checkedCol = -1;
        }
        
        @Override
        public void endRow(int rowNum) {
            // 첫 번째 데이터가 있는 행을 헤더로 설정
            if (headerRowIndex == -1 && !currentRow.isEmpty() && 
                currentRow.stream().anyMatch(cell -> cell != null && !cell.trim().isEmpty())) {
                headerRowIndex = rowNum;
                header.clear();
                header.addAll(currentRow);
            } else if (headerRowIndex != -1 && rowNum > headerRowIndex) {
                // 빈 셀 채우기
                while (currentRow.size() < header.size()) {
                    currentRow.add("");
                }
                currentChunk.add(new ArrayList<>(currentRow));
                
                // 청크 크기만큼 처리
                if (currentChunk.size() >= chunkSize) {
                    saveChunkToCsv();
                    currentChunk.clear();
                    chunkIndex++;
                }
            }
            currentRow.clear();
        }
        
        @Override
        public void cell(String cellReference, String value, XSSFComment comment) {
            int currentCol = new CellReference(cellReference).getCol();
            int emptyColumnCount = currentCol - checkedCol - 1;
            
            for (int i = 0; i < emptyColumnCount; i++) {
                currentRow.add("");
            }
            
            currentRow.add(value != null ? value : "");
            checkedCol = currentCol;
        }
        
        public void finalizeChunks() {
            if (!currentChunk.isEmpty()) {
                saveChunkToCsv();
            }
        }
        
        private void saveChunkToCsv() {
            if (header.isEmpty() || currentChunk.isEmpty()) {
                return;
            }
            
            try {
                StringBuilder csvContent = new StringBuilder();
                
                // 헤더 추가
                csvContent.append(String.join(",", header)).append("\n");
                
                // 데이터 행 추가
                for (List<String> row : currentChunk) {
                    csvContent.append(String.join(",", row)).append("\n");
                }
                
                // S3에 업로드
                String chunkFileName = "chunk_" + chunkIndex + ".csv";
                String s3ObjectPath = folderName + "/" + chunkFileName;
                
                byte[] csvBytes = csvContent.toString().getBytes(StandardCharsets.UTF_8);
                PutObjectRequest putRequest = PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(s3ObjectPath)
                        .contentType("text/csv; charset=UTF-8")
                        .build();
                
                s3Client.putObject(putRequest, RequestBody.fromBytes(csvBytes));
                
            } catch (Exception e) {
                throw new RuntimeException("CSV 청크 저장 실패", e);
            }
        }
    }
    
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }
}
