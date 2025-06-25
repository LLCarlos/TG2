package com.leo.articleimporterservice.models.dtos.oai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Metadata {

    @JacksonXmlProperty(localName = "dc", namespace = "oai_dc")
    private OaiDc oaiDc;
}
