package com.leo.articleimporterservice.models.dtos.oai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Record {

    private Header header;
    private Metadata metadata;


    public String[] toCsvRow() {
        String[] row = new String[8];

//        row[0] = formatAndReplace(metadata.getOaiDc().getCreator());
//        row[1] = formatAndReplace(metadata.getOaiDc().getContributor());
//        row[2] = formatAndReplace(metadata.getOaiDc().getTitle());
//        row[3] = getSafeDate(metadata.getOaiDc().getDate(), 1);
//        row[4] = replacePipes(metadata.getOaiDc().getRights());
//        row[5] = replacePipes(metadata.getOaiDc().getType());
//        row[6] = formatAndReplace(metadata.getOaiDc().getSubject());
//        row[7] = replacePipes(metadata.getOaiDc().getLanguage());
//        row[8] = formatAndReplace(metadata.getOaiDc().getDescription());
//        row[9] = getFirstHttpIdentifier(metadata.getOaiDc().getIdentifier());
        row[0] = formatAndReplace(metadata.getOaiDc().getTitle());
        row[1] = getSafeDate(metadata.getOaiDc().getDate(), 1);
        row[2] = replacePipes(metadata.getOaiDc().getRights());
        row[3] = replacePipes(metadata.getOaiDc().getType());
        row[4] = formatAndReplace(metadata.getOaiDc().getSubject());
        row[5] = replacePipes(metadata.getOaiDc().getLanguage());
        row[6] = formatAndReplace(metadata.getOaiDc().getDescription());
        row[7] = getFirstHttpIdentifier(metadata.getOaiDc().getIdentifier());
        return row;
    }

    // Helper method to format a list and replace pipes
    private String formatAndReplace(List<String> list) {
        return replacePipes(formatList(list));
    }

    // Helper method to handle null or missing dates safely
    private String getSafeDate(List<String> dates, int index) {
        if (dates == null || dates.size() <= index) {
            return "";
        }
        return replacePipes(dates.get(index));
    }

    // Helper method to extract the first HTTP identifier or return an empty string
    private String getFirstHttpIdentifier(List<String> identifiers) {
        return identifiers == null ? "" :
                identifiers.stream()
                        .filter(x -> x.toLowerCase().contains("http"))
                        .findFirst()
                        .map(this::replacePipes)
                        .orElse("");
    }

    // Helper method to replace pipes with commas for psv compatibility
    private String replacePipes(String value) {
        return value == null ? "" : value.replace("|", ":");
    }

    public String formatList(List<String> list){
        if (list == null  || list.isEmpty()) return  "";
        return String.join(";", list);
    }
}
