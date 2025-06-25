package com.leo.articleimporterservice.models.dtos.oai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListSetsResponse extends ListSets {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "set")
    private List<Set> sets;
}