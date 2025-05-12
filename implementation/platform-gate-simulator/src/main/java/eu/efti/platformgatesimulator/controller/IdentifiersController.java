package eu.efti.platformgatesimulator.controller;

import eu.efti.commons.utils.SerializeUtils;
import eu.efti.platformgatesimulator.config.GateProperties;
import eu.efti.platformgatesimulator.exception.UploadException;
import eu.efti.platformgatesimulator.mapper.MapperUtils;
import eu.efti.platformgatesimulator.service.ApIncomingService;
import eu.efti.platformgatesimulator.service.ApiKeyService;
import eu.efti.platformgatesimulator.service.IdentifierService;
import eu.efti.platformgatesimulator.service.ReaderService;
import eu.efti.v1.edelivery.ObjectFactory;
import eu.efti.v1.json.SaveIdentifiersRequest;
import jakarta.xml.bind.JAXBElement;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/identifiers")
@AllArgsConstructor
@Slf4j
public class IdentifiersController {

    private final ApIncomingService apIncomingService;
    private final MapperUtils mapperUtils = new MapperUtils();
    private final ObjectFactory objectFactory = new ObjectFactory();
    private final SerializeUtils serializeUtils;
    private final ReaderService readerService;
    private final GateProperties gateProperties;
    private final ApiKeyService apiKeyService;

    private final IdentifierService identifierService;

    @PostMapping("/upload/file")
    public ResponseEntity<String> uploadFile(@RequestPart final MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.error("No file sent");
            return new ResponseEntity<>("Error, no file sent", HttpStatus.BAD_REQUEST);
        }
        log.info("try to upload file");
        try {
            readerService.uploadFile(file);
        } catch (UploadException e) {
            return new ResponseEntity<>("Error while uploading file " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("File saved", HttpStatus.OK);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadIdentifiers(@RequestBody final SaveIdentifiersRequest identifiersDto) {
        if (identifiersDto == null) {
            log.error("Error no identifiers sent");
            return new ResponseEntity<>("No identifiers sent", HttpStatus.BAD_REQUEST);
        }
        log.info("send identifiers to gate");
        try {
            final eu.efti.v1.edelivery.SaveIdentifiersRequest edeliveryRequest = mapperUtils.mapToEdeliveryRequest(identifiersDto);
            final JAXBElement<eu.efti.v1.edelivery.SaveIdentifiersRequest> jaxbElement = objectFactory.createSaveIdentifiersRequest(edeliveryRequest);
            final String requestBody = serializeUtils.mapJaxbObjectToXmlString(jaxbElement, eu.efti.v1.edelivery.SaveIdentifiersRequest.class);

            RestClient restClient = RestClient
                    .builder()
                    .baseUrl(gateProperties.getGateBaseUrl() + "/api/v1/platform")
                    .build();
            String response = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/identifiers")
                            .build()
                    )
                    .header("X-API-Key", apiKeyService.getApiKey())
                    .contentType(MediaType.APPLICATION_XML)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
            log.info("Response from gate: {}", response);
            return new ResponseEntity<>("Identifiers uploaded", HttpStatus.OK);
        } catch (final Exception e) {
            log.error("Error when try to send to gate the Identifiers", e);
            return new ResponseEntity<>("No identifiers sent", HttpStatus.BAD_REQUEST);
        }
    }
}
