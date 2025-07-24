package com.hanyang.datacrawler.service.file;

import lombok.Getter;

@Getter
public enum FileType {
    PDF("pdf"),
    CSV("csv"),
    XLS("xls"),
    XLSX("xlsx"),
    JSON("json"),
    XML("xml"),
    TTL("ttl"),
    ZIP("zip"),
    TXT("txt"),
    HWP("hwp"),
    HWPX("hwpx"),
    DOC("doc"),
    DOCX("docx"),
    JPG("jpg"),
    JPEG("jpeg"),
    PNG("png"),
    GIF("gif"),
    TIFF("tiff"),
    TIF("tif"),
    MP4("mp4"),
    MP3("mp3"),
    WAV("wav"),
    SHP("shp"),
    RDF("rdf"),
    LOD("lod"),
    STL("stl"),
    PY("py"),
    GPX("gpx"),
    SGML("sgml"),
    ODT("odt"),
    FASTA("fasta"),
    PPT("ppt"),
    PPTX("pptx"),
    DTD("dtd"),
    GEOJSON("geojson"),
    ETC("etc"),
    UNKNOWN("");

    private final String extension;

    FileType(String extension) {
        this.extension = extension;
    }

    public static FileType fromExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return UNKNOWN;
        }
        String lowerExtension = extension.toLowerCase();
        for (FileType fileType : values()) {
            if (fileType.getExtension().equals(lowerExtension)) {
                return fileType;
            }
        }
        return UNKNOWN;
    }

    public static FileType getFileType(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1 || fileName.endsWith(".")) {
            return UNKNOWN;
        }
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        return fromExtension(extension);
    }


    public boolean IsSupportVisualization() {
        return this == FileType.XLS || this == FileType.XLSX || this == FileType.CSV;
    }
}