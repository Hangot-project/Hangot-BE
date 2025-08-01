package com.hanyang.fileparser.service.parser;

import com.hanyang.fileparser.core.exception.ParsingException;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.opencsv.CSVReader;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Component
public class CsvParser implements ParserStrategy {

    @Override
    public void parse(Path path, String datasetId, Consumer<List<String>> headerCallback,
                                 Consumer<List<List<String>>> chunkCallback, int chunkSize) throws ParsingException {
        
        try (BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(path))) {
            Charset detectedCharset = detectCharset(inputStream);
            
            InputStreamReader reader = new InputStreamReader(inputStream, detectedCharset);
            BufferedReader bufferedReader = new BufferedReader(reader);
            CSVReader csvReader = new CSVReader(bufferedReader);

            String[] nextRecord;
            boolean isFirstRow = true;
            List<List<String>> tempChunk = new ArrayList<>();
            
            while ((nextRecord = csvReader.readNext()) != null) {
                if (isFirstRow) {
                    List<String> header = Arrays.asList(nextRecord);
                    headerCallback.accept(header);
                    isFirstRow = false;
                } else {
                    tempChunk.add(Arrays.asList(nextRecord));
                    
                    if (tempChunk.size() >= chunkSize) {
                        chunkCallback.accept(new ArrayList<>(tempChunk));
                        tempChunk.clear();
                    }
                }
            }
            
            if (!tempChunk.isEmpty()) {
                chunkCallback.accept(tempChunk);
            }
            
        } catch (Exception e) {
            throw new ParsingException(Arrays.toString(e.getStackTrace()));
        }
    }

    private Charset detectCharset(BufferedInputStream inputStream) throws IOException {
        inputStream.mark(8192);

        try {
            byte[] bom = new byte[3];
            inputStream.read(bom);
            inputStream.reset();

            if (bom[0] == (byte)0xEF && bom[1] == (byte)0xBB && bom[2] == (byte)0xBF) {
                return StandardCharsets.UTF_8;
            }

            CharsetDetector detector = new CharsetDetector();
            detector.setText(inputStream);
            CharsetMatch match = detector.detect();

            if (match != null && match.getConfidence() >= 70) {
                return Charset.forName(match.getName());
            }

            return StandardCharsets.UTF_8;

        } catch (Exception e) {
            return StandardCharsets.UTF_8;
        } finally {
            inputStream.reset();
        }
    }
}