package com.leo.articleimporterservice.models.dtos.oai;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.List;

@Data
public class Header {

    private String identifier;
    private String datestamp;
    private String status;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "setSpec")
    private List<String> setSpec;
}
