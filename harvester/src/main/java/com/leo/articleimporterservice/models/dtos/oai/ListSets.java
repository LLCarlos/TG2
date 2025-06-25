package com.leo.articleimporterservice.models.dtos.oai;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListSets {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "setName")
    private List<Record> records;

    @JacksonXmlProperty(localName = "resumptionToken")
    private String resumptionToken;
}
