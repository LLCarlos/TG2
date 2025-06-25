package com.leo.articleimporterservice.models.dtos.oai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OaiPmhResponse {

    @JacksonXmlProperty(isAttribute = true)
    private String xmlns;

    @JacksonXmlProperty(isAttribute = true)
    private String xsiSchemaLocation;

    private String responseDate;

    @JacksonXmlProperty(localName = "ListRecords")
    private ListRecords listRecords;

}
