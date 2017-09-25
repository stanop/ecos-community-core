package ru.citeck.ecos.cmmn.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author deathNC
 */
public class CaseImportServiceJS extends AlfrescoScopableProcessorExtension {

    private final Logger log = LoggerFactory.getLogger(CaseImportServiceJS.class);

    private CaseImportService caseImportService;

    public void importCase(String fileName, boolean replaceExisting) {
        try {
            byte[] fileData = Files.readAllBytes(Paths.get(fileName));
            caseImportService.importCase(fileData);
        } catch (Exception e) {
            log.error("cannot import case from file: " + fileName);
        }
    }


    public void setCaseImportService(CaseImportService caseImportService) {
        this.caseImportService = caseImportService;
    }
}
