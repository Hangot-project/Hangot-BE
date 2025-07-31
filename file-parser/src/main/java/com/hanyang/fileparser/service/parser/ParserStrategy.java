package com.hanyang.fileparser.service.parser;

import com.hanyang.fileparser.core.exception.ParsingException;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public interface ParserStrategy {
    void parse(Path path, String datasetId, Consumer<List<String>> headerCallback,
               Consumer<List<List<String>>> chunkCallback, int chunkSiz) throws ParsingException;
}