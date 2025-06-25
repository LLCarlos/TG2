package com.leo.articleimporterservice.models.dtos.oai;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiConfig {
    private String source;

    private String url;

    private String bitstreamUrl;

    private String filePath;
}
