package com.leo.articleimporterservice.models.dtos.oai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class OaiDc {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "title", namespace = "dc")
    private List<String> title;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "creator", namespace = "dc")
    private List<String> creator;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "contributor", namespace = "dc")
    private List<String> contributor;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "subject", namespace = "dc")
    private List<String> subject;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "description", namespace = "dc")
    private List<String> description;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "date", namespace = "dc")
    private List<String> date;

    @JacksonXmlProperty(localName = "type", namespace = "dc")
    private String type;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "identifier", namespace = "dc")
    private List<String> identifier;

    @JacksonXmlProperty(localName = "language", namespace = "dc")
    private String language;

    @JacksonXmlProperty(localName = "rights", namespace = "dc")
    private String rights;

    @JacksonXmlProperty(localName = "format", namespace = "dc")
    private String format;
}