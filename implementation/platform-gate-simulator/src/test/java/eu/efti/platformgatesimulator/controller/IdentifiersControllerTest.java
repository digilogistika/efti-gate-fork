package eu.efti.platformgatesimulator.controller;

import eu.efti.commons.utils.SerializeUtils;
import eu.efti.platformgatesimulator.exception.UploadException;
import eu.efti.platformgatesimulator.service.ApIncomingService;
import eu.efti.platformgatesimulator.service.IdentifierService;
import eu.efti.platformgatesimulator.service.ReaderService;
import eu.efti.v1.json.Consignment;
import eu.efti.v1.json.SaveIdentifiersRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

@WebMvcTest(IdentifiersController.class)
@ContextConfiguration(classes = {IdentifiersController.class})
@ExtendWith(SpringExtension.class)
class IdentifiersControllerTest {

    private final SaveIdentifiersRequest saveIdentifiersRequest = new SaveIdentifiersRequest();
    @Autowired
    protected MockMvc mockMvc;
    @MockBean
    private IdentifiersController identifiersController;
    @Mock
    private ApIncomingService apIncomingService;
    @Mock
    private ReaderService readerService;
    @Mock
    private IdentifierService identifierService;
    @Mock
    private SerializeUtils serializeUtils;

    @BeforeEach
    void before() {
        identifiersController = new IdentifiersController(apIncomingService, serializeUtils, readerService, identifierService);
        saveIdentifiersRequest.setRequestId("requestId");
        saveIdentifiersRequest.setConsignment(new Consignment());
        saveIdentifiersRequest.setDatasetId("datasetId");
    }

    @Test
    void uploadFileTest() {
        MockMultipartFile file = new MockMultipartFile("data", "other-file-name.data", "text/plain", "some other type".getBytes(StandardCharsets.UTF_8));

        ResponseEntity<String> result = identifiersController.uploadFile(file);

        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals("File saved", result.getBody());
    }

    @Test
    void uploadFileNullTest() {
        ResponseEntity<String> result = identifiersController.uploadFile(null);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        Assertions.assertEquals("Error, no file sent", result.getBody());
    }

    @Test
    void uploadFileThrowTest() throws UploadException {
        MockMultipartFile file = new MockMultipartFile("data", "other-file-name.data", "text/plain", "some other type".getBytes(StandardCharsets.UTF_8));
        Mockito.doThrow(UploadException.class).when(readerService).uploadFile(file);

        ResponseEntity<String> result = identifiersController.uploadFile(file);

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        Assertions.assertEquals("Error while uploading file null", result.getBody());
    }
    

    @Test
    void uploadIdentifiersNullTest() {
        final ResponseEntity<String> result = identifiersController.uploadIdentifiers(null);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        Assertions.assertEquals("No identifiers sent", result.getBody());
    }
}
