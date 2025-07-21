package com.hanyang.datacrawler.infrastructure;

import lombok.Getter;

@Getter
public enum FileType {
    PDF("pdf", "application/pdf"),
    CSV("csv", "text/csv; charset=UTF-8"),
    XLS("xls", "application/vnd.ms-excel"),
    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    JSON("json", "application/json; charset=UTF-8"),
    XML("xml", "application/xml; charset=UTF-8"),
    TTL("ttl", "text/turtle"),
    ZIP("zip", "application/zip"),
    TXT("txt", "text/plain; charset=UTF-8"),
    HWP("hwp", "application/x-hwp"),
    HWPX("hwpx", "application/x-hwp"),
    DOC("doc", "application/msword"),
    DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    JPG("jpg", "image/jpeg"),
    JPEG("jpeg", "image/jpeg"),
    PNG("png", "image/png"),
    GIF("gif", "image/gif"),
    TIFF("tiff", "image/tiff"),
    TIF("tif", "image/tiff"),
    MP4("mp4", "video/mp4"),
    MP3("mp3", "audio/mpeg"),
    WAV("wav", "audio/wav"),
    SHP("shp", "application/x-esri-shape"),
    RDF("rdf", "application/rdf+xml"),
    LOD("lod", "application/octet-stream"),
    STL("stl", "model/stl"),
    PY("py", "text/x-python"),
    GPX("gpx", "application/gpx+xml"),
    SGML("sgml", "text/sgml"),
    ODT("odt", "application/vnd.oasis.opendocument.text"),
    FASTA("fasta", "text/x-fasta"),
    PPT("ppt", "application/vnd.ms-powerpoint"),
    PPTX("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
    DTD("dtd", "application/xml-dtd"),
    GEOJSON("geojson", "application/geo+json"),
    ETC("etc", "application/octet-stream"),
    UNKNOWN("", "application/octet-stream");

    private final String extension;
    private final String contentType;

    FileType(String extension, String contentType) {
        this.extension = extension;
        this.contentType = contentType;
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
}