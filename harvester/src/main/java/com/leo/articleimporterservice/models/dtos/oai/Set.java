package com.leo.articleimporterservice.models.dtos.oai;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class Set {
    @JacksonXmlProperty(localName = "setSpec")
    private String setSpec;

    @JacksonXmlProperty(localName = "setName")
    private String setName;
}