package com.leo.articleimporterservice.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.leo.articleimporterservice.models.dtos.oai.ListSetsResponse;
import com.leo.articleimporterservice.models.dtos.oai.OaiPmhResponse;
import com.leo.articleimporterservice.models.dtos.oai.Record;
import com.leo.articleimporterservice.models.dtos.oai.SetUFRGS;
import com.leo.articleimporterservice.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Component
@Slf4j
public class OaiClient {

    RestTemplate restTemplate = Utils.getCustomRestTemplate();

    /**
     * Retrieves all records from an OAI-PMH source, filtering by the last updated date.
     * This method will iterate over pages of records, collecting valid ones and handling failures.
     * The process continues until all records have been retrieved.
     *
     * @param lastUpdated The date after which records should have been updated.
     *                    If null, no filter on the update date is applied.
     * @return A list of records that were successfully retrieved from the OAI-PMH source.
     * @throws IOException If there is an issue with reading or processing the XML response from the OAI-PMH service.
     */
    public List<Record> getAllRecords(LocalDate lastUpdated, Integer payloadLimit, String apiUrl) throws IOException {
        List<Record> records = new LinkedList<>();
        List<Record> failures = new LinkedList<>();
        XmlMapper xmlMapper = new XmlMapper();

        String resumptionToken = null;

        do {
            String url = buildRequestUrl(resumptionToken, lastUpdated, apiUrl);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            OaiPmhResponse oaiResponse = xmlMapper.readValue(response.getBody(), OaiPmhResponse.class);

            records.addAll(oaiResponse.getListRecords().getRecords().stream().filter(x -> x.getMetadata() != null && x.getHeader() != null).toList());
            failures.addAll(oaiResponse.getListRecords().getRecords().stream().filter(x -> x.getMetadata() == null || x.getHeader() == null).toList());

            resumptionToken = oaiResponse.getListRecords().getResumptionToken();
        } while (resumptionToken != null && !resumptionToken.isBlank() && records.size() < payloadLimit);

        return records;
    }
    public List<Record> getAllRecordsBySet(LocalDate lastUpdated, Integer payloadLimit, String apiUrl, Integer setId) throws IOException {
        List<Record> records = new LinkedList<>();
        List<Record> failures = new LinkedList<>();
        XmlMapper xmlMapper = new XmlMapper();

        String resumptionToken = null;

        do {
            String url = buildRequestUrlBySetId(resumptionToken, lastUpdated, apiUrl, setId);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            OaiPmhResponse oaiResponse = xmlMapper.readValue(response.getBody(), OaiPmhResponse.class);

            records.addAll(oaiResponse.getListRecords().getRecords().stream().filter(x -> x.getMetadata() != null && x.getHeader() != null).toList());
            failures.addAll(oaiResponse.getListRecords().getRecords().stream().filter(x -> x.getMetadata() == null || x.getHeader() == null).toList());

            resumptionToken = oaiResponse.getListRecords().getResumptionToken();
        } while (resumptionToken != null && !resumptionToken.isBlank() && records.size() < payloadLimit);

        return records;
    }


    public List<Record> getAllSets(LocalDate lastUpdated, Integer payloadLimit, String apiUrl) throws JsonProcessingException {
        List<Record> records = new LinkedList<>();
        List<Record> failures = new LinkedList<>();
        XmlMapper xmlMapper = new XmlMapper();

        String resumptionToken = null;

        do {
            String url = buildRequestListSets(resumptionToken, lastUpdated, apiUrl);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            ListSetsResponse oaiResponse = xmlMapper.readValue(response.getBody(), ListSetsResponse.class);
            System.out.println(oaiResponse);
//            records.addAll(oaiResponse.getListRecords().getRecords().stream().filter(x -> x.getMetadata() != null && x.getHeader() != null).toList());
//            failures.addAll(oaiResponse.getListRecords().getRecords().stream().filter(x -> x.getMetadata() == null || x.getHeader() == null).toList());
//
//            resumptionToken = oaiResponse.getListRecords().getResumptionToken();
        } while (resumptionToken != null && !resumptionToken.isBlank() && records.size() < payloadLimit);

        return records;
    }

    /**
     * Builds the request URL to query the OAI-PMH service, optionally including a resumption token for pagination
     * and a filter for records updated after the specified date.
     *
     * @param resumptionToken The token used for pagination when fetching records across multiple requests.
     *                        If null, the first page of results will be requested.
     * @param lastUpdated The date after which records should have been updated. If null, no date filter is applied.
     * @return A fully constructed URL for the OAI-PMH service request.
     */
    private String buildRequestUrl(String resumptionToken, LocalDate lastUpdated, String apiUrl) {
        StringBuilder urlBuilder = new StringBuilder(apiUrl);
        urlBuilder.append("?verb=ListRecords");

        if (resumptionToken != null) {
            urlBuilder.append("&resumptionToken=").append(resumptionToken);
            return urlBuilder.toString();
        }

        urlBuilder.append("&metadataPrefix=oai_dc");
        if (lastUpdated != null){
            urlBuilder.append("&from=").append(lastUpdated.atTime(0, 0));
        }

        return urlBuilder.toString();
    }
    private String buildRequestUrlBySetId(String resumptionToken, LocalDate lastUpdated, String apiUrl, Integer setId) {
        StringBuilder urlBuilder = new StringBuilder(apiUrl);
        urlBuilder.append("?verb=ListRecords");

        if (resumptionToken != null) {
            urlBuilder.append("&resumptionToken=").append(resumptionToken);
            return urlBuilder.toString();
        }

        urlBuilder.append("&metadataPrefix=oai_dc");
        if (lastUpdated != null){
            urlBuilder.append("&from=").append(lastUpdated.atTime(0, 0));
        }

        if(setId != null){
            urlBuilder.append("&set=").append(getSetById(setId));
        }
        return urlBuilder.toString();
    }

    private String buildRequestListSets(String resumptionToken, LocalDate lastUpdated, String apiUrl){
        StringBuilder urlBuilder = new StringBuilder(apiUrl);
        urlBuilder.append("?verb=ListSets");

        return urlBuilder.toString();
    }

    /**
     * Downloads a PDF from the specified URL and returns its content as a byte array.
     *
     * @param url The URL from which to download the PDF.
     * @return A byte array containing the PDF data, or null if the download fails.
     */
    public byte[] downloadPdf(String url){
        // Make a GET request to the PDF endpoint
        ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
        return response.getBody();
    }

    public String getSetById(Integer setId) {
        return SetUFRGS.getSetSpecByIndex(setId);
    }
}

