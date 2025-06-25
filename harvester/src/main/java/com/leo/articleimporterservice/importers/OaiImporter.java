package com.leo.articleimporterservice.importers;

import com.leo.articleimporterservice.clients.OaiClient;
import com.leo.articleimporterservice.models.dtos.oai.ApiConfig;
import com.leo.articleimporterservice.models.dtos.oai.Record;
import com.leo.articleimporterservice.clients.*;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

@Component
@Slf4j
public class OaiImporter {

    @Autowired
    private OaiClient oaiClient;

    private static final String DEFAULT_CONFIG_FILE_PATH = "config.csv";
    private static final String PDF_FOLDER = "/pdfs/";

    public void importOaiData(LocalDate lastUpdated, Boolean skipPdfs, Integer payloadLimit)
            throws IOException, InterruptedException, CsvValidationException {

        List<ApiConfig> apis = readConfigurationFile(null);

        for (ApiConfig api : apis) {

            List<Record> oaiResponseSets = oaiClient.getAllSets(lastUpdated, payloadLimit, api.getUrl());

            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            log.info("Starting download of OAI Records from {}", api.getSource());
            List<Record> oaiResponse = oaiClient.getAllRecords(lastUpdated, payloadLimit, api.getUrl());
            log.info("Finished downloading {} OAI Records", oaiResponse.size());

            log.info("Starting saving OAI Records in CSV format");
            saveToCsv(oaiResponse, api.getSource(), timestamp);
            log.info("Finished saving OAI Records in CSV format");

            if (!skipPdfs) {
                log.info("Starting downloading PDFs. This might take a while.");
                Map<String, String> pdfFailures = downloadPdfs(oaiResponse, api.getSource(), api.getBitstreamUrl());
                log.info("Finished downloading PDFs. {} PDFs failed to download.", pdfFailures.size());
                log.info("Saving load history for PDFs");
                saveLoadHistory(pdfFailures, api.getSource(), timestamp);
                log.info("Finished saving load history");
            } else {
                log.info("Dry run mode enabled. Skipping PDF downloads and load history.");
            }

            log.info("Finished load from {}", api.getSource());
        }
        log.info("Finished load");
    }

    public void importOaiDataBySet(LocalDate lastUpdated, Boolean skipPdfs, Integer payloadLimit, Integer setId)
            throws IOException, InterruptedException, CsvValidationException {

        List<ApiConfig> apis = readConfigurationFile(null);

        for (ApiConfig api : apis) {
            if (api.getSource().equals("UFRGS")) {

                String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
                log.info("Starting download of OAI Records from {}", api.getSource());
                List<Record> oaiResponse = oaiClient.getAllRecordsBySet(lastUpdated, payloadLimit, api.getUrl(), setId);
                log.info("Finished downloading {} OAI Records", oaiResponse.size());

                log.info("Starting saving OAI Records in CSV format");
                saveToCsv(oaiResponse, api.getSource(), timestamp);
                log.info("Finished saving OAI Records in CSV format");

                if (!skipPdfs) {
                    log.info("Starting downloading PDFs. This might take a while.");
                    Map<String, String> pdfFailures = downloadPdfs(oaiResponse, api.getSource(), api.getBitstreamUrl());
                    log.info("Finished downloading PDFs. {} PDFs failed to download.", pdfFailures.size());
                    log.info("Saving load history for PDFs");
                    saveLoadHistory(pdfFailures, api.getSource(), timestamp);
                    log.info("Finished saving load history");
                } else {
                    log.info("Dry run mode enabled. Skipping PDF downloads and load history.");
                }
                log.info("Finished load from {}", api.getSource());
            }
            log.info("Finished load");
        }
    }

    public void importAllSets(LocalDate lastUpdated, Boolean skipPdfs, Integer payloadLimit) throws IOException, InterruptedException, CsvValidationException {

        List<ApiConfig> apis = readConfigurationFile(null);


        for (ApiConfig api : apis) {
            if (api.getSource().equals("UFRGS")) {
                
                for (int i = 1; i<=10;i++){
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
                    log.info("Starting download of OAI Records from {}", api.getSource());
                    List<Record> oaiResponse = oaiClient.getAllRecordsBySet(lastUpdated, payloadLimit, api.getUrl(), i);
                    log.info("Finished downloading {} OAI Records", oaiResponse.size());

                    String source = "UFRGS_" + oaiClient.getSetById(i);

                    log.info("Starting saving OAI Records in CSV format");
                    saveToCsv(oaiResponse,source, timestamp);
                    log.info("Finished saving OAI Records in CSV format");
                }
            }
            log.info("Finished load");
        }
    }

    /**
     * Downloads PDF files for a list of records and saves them locally.
     * <p>
     * This method attempts to download PDF files for each record in the provided
     * list.
     * It constructs the download URL using a base URL and the record's identifier,
     * downloads the PDF content, and saves it to a local file. If any errors occur
     * during this process, they are captured and stored in a map of failures.
     *
     * @param records A List of Record objects containing the information needed to
     *                download PDFs.
     * @return A Map where the key is the record identifier and the value is an
     *         error message
     *         for any records that failed to download. If all downloads are
     *         successful,
     *         the map will be empty.
     */
    public Map<String, String> downloadPdfs(List<Record> records, String source, String bitstreamUrl)
            throws InterruptedException {
        Map<String, String> failures = new HashMap<>();

        // Process the records in this batch
        for (Record record : records) {
            try {
                String identifier = record.getHeader().getIdentifier().split("/")[1];
                String url = bitstreamUrl + "/" + identifier + "/article.pdf?sequence=1&isAllowed=y";

                // Download PDF
                byte[] fileData = oaiClient.downloadPdf(url);

                Path projectRoot = Paths.get(System.getProperty("user.dir"));
                Path folderPath = Path.of(projectRoot + "/results/" + source + "/" + PDF_FOLDER);

                if (!Files.exists(folderPath)) {
                    Files.createDirectories(folderPath);
                }

                Path saveLocation = Paths.get(folderPath + "/" + identifier + ".pdf");

                // Write the PDF to a file
                try (FileOutputStream fos = new FileOutputStream(saveLocation.toFile())) {
                    fos.write(fileData);
                }

            } catch (NoSuchElementException e) {
                failures.put(record.getHeader().getIdentifier(), "Unable to find PDF download URL.");
                log.error("Unable to find PDF download URL");
            } catch (IOException e) {
                failures.put(record.getHeader().getIdentifier(), "Unable to save PDF locally due to I/O restrictions.");
                log.error("Unable to save PDF locally due to I/O restrictions: {}", e.getMessage());
            } catch (RestClientException e) {
                failures.put(record.getHeader().getIdentifier(),
                        "Unable to download PDF from given source: " + e.getMessage());
                log.error("Unable to download PDF from given source: {}", e.getMessage());
            } catch (Exception e) {
                failures.put(record.getHeader().getIdentifier(),
                        "An unexpected error occurred while downloading PDF: " + e.getMessage());
                log.error("An unexpected error occurred while downloading PDF: {}", e.getMessage());
            }
        }

        return failures;
    }

    /**
     * Saves a list of Record objects to a CSV file.
     * <p>
     * This method creates a CSV file named "theses.csv" in the current directory
     * and writes the data from the provided Record objects into it. The CSV file
     * uses '|' as the delimiter and includes a header row.
     *
     * @param records A List of Record objects containing the data to be written to
     *                the CSV file.
     * @throws IOException If an I/O error occurs while writing to the file.
     */
    public void saveToCsv(List<Record> records, String source, String timestamp) throws IOException {
        Path projectRoot = Paths.get(System.getProperty("user.dir"));
        Path folderPath = Path.of(projectRoot + "/results/" + source);

        if (!Files.exists(folderPath)) {
            Files.createDirectories(folderPath);
        }
        File csvFile = new File(folderPath + "/theses_" + timestamp + ".csv");

        FileWriter fileWriter = new FileWriter(csvFile);
        CSVWriter csvWriter = new CSVWriter(fileWriter, '|', CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

        csvWriter.writeNext(new String[] { "Título", "Ano de Publicação", "Tipo de Acesso", "Tipo de Documento",
                "Assuntos", "Idioma", "Descrição", "Link de Acesso" });

        // Loop through each record and write to CSV
        for (Record record : records) {
            try {
                csvWriter.writeNext(record.toCsvRow());
            } catch (Exception e) {
                log.error("Error writing record to CSV: {}", e.getMessage());
            }
        }

        // Close the CSV writer
        csvWriter.flush();
        csvWriter.close();
    }

    /**
     * Reads the configuration file and returns a list of ApiConfig objects.
     * <p>
     * This method reads a CSV file containing source, base URL, and bitstream URL
     * for each OAI source.
     * It creates and returns a list of ApiConfig objects based on the data from the
     * file.
     *
     * @param filePath The path to the configuration CSV file. If null, the default
     *                 file "config.csv" is used.
     * @return A list of ApiConfig objects containing source and URL information for
     *         each OAI source.
     * @throws IOException            If an I/O error occurs while reading the file.
     * @throws CsvValidationException If there is an issue while validating the CSV
     *                                data.
     */
    public List<ApiConfig> readConfigurationFile(String filePath) throws IOException, CsvValidationException {
        if (filePath == null)
            filePath = DEFAULT_CONFIG_FILE_PATH;
        List<ApiConfig> apiConfigs = new LinkedList<>();

        CSVReader csvReader = new CSVReader(new FileReader(filePath));
        String[] record;
        while ((record = csvReader.readNext()) != null) {
            // Assuming each row has two columns: source and baseUrl
            String source = record[0];
            String baseUrl = record[1];
            String beatstreamUrl = record[2];
            apiConfigs.add(new ApiConfig(source, baseUrl, beatstreamUrl, null));
        }
        return apiConfigs;
    }

    /**
     * Appends a new configuration to the CSV configuration file.
     * <p>
     * This method appends a new ApiConfig entry (source, base URL, bitstream URL)
     * to the specified configuration file.
     * If the file does not exist, it will be created.
     *
     * @param config The ApiConfig object containing the data to be added to the
     *               configuration file.
     * @throws IOException If an I/O error occurs while writing to the file.
     */
    public void appendToConfigurationFile(ApiConfig config) throws IOException {
        String path = config.getFilePath();
        if (path == null)
            path = DEFAULT_CONFIG_FILE_PATH;

        File file = new File(path);

        // Check if the file exists, and if not, create it
        if (!file.exists()) {
            file.createNewFile();
        }

        CSVWriter csvWriter = new CSVWriter(new FileWriter(path, true));

        // Append data to the CSV file
        String[] record = { config.getSource(), config.getUrl(), config.getBitstreamUrl() };
        csvWriter.writeNext(record);
        csvWriter.close();
    }

    /**
     * Saves the load history of failed PDF downloads to a CSV file.
     * <p>
     * This method saves a CSV file that contains the record identifiers and error
     * messages
     * for PDF download failures. The file is named "loadhistory_TIMESTAMP.csv" and
     * is stored
     * in a directory based on the source name.
     *
     * @param failures  A map containing the record identifiers and error messages
     *                  for any failed downloads.
     * @param source    The source name for the records (used to organize the output
     *                  folder).
     * @param timestamp A timestamp string that is appended to the output file name.
     * @throws IOException If an I/O error occurs while writing to the file.
     */
    public void saveLoadHistory(Map<String, String> failures, String source, String timestamp) throws IOException {
        Path projectRoot = Paths.get(System.getProperty("user.dir"));
        Path folderPath = Path.of(projectRoot + "/results/" + source);

        if (!Files.exists(folderPath)) {
            Files.createDirectories(folderPath);
        }
        File csvFile = new File(folderPath + "/loadhistory_" + timestamp + ".csv");

        FileWriter fileWriter = new FileWriter(csvFile);
        CSVWriter csvWriter = new CSVWriter(fileWriter);

        csvWriter.writeNext(new String[] { "Identifier", "Error Message" });
        for (Map.Entry<String, String> entry : failures.entrySet()) {
            csvWriter.writeNext(new String[] { entry.getKey(), entry.getValue() });
        }
        csvWriter.flush();
        csvWriter.close();
    }

}
