package com.leo.articleimporterservice.controllers;

import com.leo.articleimporterservice.clients.OaiClient;
import com.leo.articleimporterservice.importers.OaiImporter;
import com.leo.articleimporterservice.models.dtos.oai.ApiConfig;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
public class OaiController {

    @Autowired
    private OaiImporter importer;

    /**
     * Downloads and saves OAI data locally in csv and pdf files on the project root.
     * <p>
     * This endpoint allows you to retrieve the metadata for all theses from the OAI source. It can be filtered
     * by the `lastUpdated` parameter to only retrieve records updated after the specified date. The `skipPdfs` parameter
     * can be used to simulate the import process without actually downloading the pdfs.
     *
     * @param lastUpdated An optional date parameter to filter records based on their last update date.
     *                    If not provided, all records will be included.
     * @param skipPdfs    Boolean that dictates whether the PDF download will be skipped.
     * @param payloadLimit Integer that specifies the maximum number of entries to be downloaded. Works in multiples of 100. (On average, 1000 entries occupy 2GB)
     */
     @PostMapping(value = "/load")
     public ResponseEntity<?> getAllThesis(@RequestParam(required = false) LocalDate lastUpdated, @RequestParam(required = false, defaultValue = "false") Boolean skipPdfs, @RequestParam(required = false) Integer payloadLimit) throws IOException, InterruptedException, CsvValidationException {
         if (payloadLimit == null) payloadLimit = Integer.MAX_VALUE;

         importer.importOaiData(lastUpdated, skipPdfs, payloadLimit);

         return new ResponseEntity<>(HttpStatus.OK);
     }

     @GetMapping(value = "/list-sets")
     public ResponseEntity<?> listSets(@RequestParam(required = false) LocalDate lastUpdated) throws CsvValidationException, IOException, InterruptedException {
         return new ResponseEntity<>(HttpStatus.OK);
     }

    @PostMapping(value = "/load-by-set")
    public ResponseEntity<?> getAllThesisBySet(@RequestParam Integer setId,@RequestParam(required = false) LocalDate lastUpdated, @RequestParam(required = false, defaultValue = "false") Boolean skipPdfs, @RequestParam(required = false) Integer payloadLimit) throws IOException, InterruptedException, CsvValidationException {
        if (payloadLimit == null) payloadLimit = Integer.MAX_VALUE;

        importer.importOaiDataBySet(lastUpdated, skipPdfs, payloadLimit, setId);

        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PostMapping(value = "/load-all-sets")
    public ResponseEntity<?> getAllThesisBySet(@RequestParam(required = false) LocalDate lastUpdated, @RequestParam(required = false, defaultValue = "false") Boolean skipPdfs, @RequestParam(required = false) Integer payloadLimit) throws IOException, InterruptedException, CsvValidationException {
        if (payloadLimit == null) payloadLimit = Integer.MAX_VALUE;

        importer.importAllSets(lastUpdated, skipPdfs, payloadLimit);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    
     @PostMapping(value = "/tenant")
     public ResponseEntity<?> registerApi(@RequestBody List<ApiConfig> body){

         try {
             for (ApiConfig config : body) {
                 importer.appendToConfigurationFile(config);
             }
         } catch (Exception e) {
             return new ResponseEntity<>("Please check file path and permissions and try again.", HttpStatus.INTERNAL_SERVER_ERROR);
         }

         return new ResponseEntity<>(HttpStatus.OK);
     }



}
