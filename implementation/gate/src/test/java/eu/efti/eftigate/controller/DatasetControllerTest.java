package eu.efti.eftigate.controller;

import eu.efti.commons.dto.UilDto;
import eu.efti.commons.enums.StatusEnum;
import eu.efti.eftigate.dto.DatasetDto;
import eu.efti.eftigate.service.DatasetSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static com.jayway.jsonassert.JsonAssert.with;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DatasetController.class)
@ContextConfiguration(classes = {DatasetController.class})
@ExtendWith(SpringExtension.class)
class DatasetControllerTest {

    public static final String REQUEST_ID = "requestId";
    public static final String GATE_ID = "gateId";
    public static final String PLATFORM_ID = "platformId";
    public static final String DATASET_ID = "550e8400-e29b-41d4-a716-446655440000";
    public static final String EFTI_DATA = "sample efti data";
    public static final String EFTI_DATA_ENCODED = "c2FtcGxlIGVmdGkgZGF0YQ==";

    private final DatasetDto datasetDto = new DatasetDto();

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    DatasetSearchService datasetSearchService;

    @BeforeEach
    void before() {
        datasetDto.setStatus(StatusEnum.COMPLETE);
        datasetDto.setRequestId(REQUEST_ID);
        datasetDto.setData(EFTI_DATA.getBytes());
    }

    @Test
    @WithMockUser
    void getDatasetWithValidParametersTest() throws Exception {
        Mockito.when(datasetSearchService.getDataset(any(UilDto.class)))
                .thenReturn(datasetDto);

        final String result = mockMvc.perform(get("/v1/dataset/{gateId}/{platformId}/{datasetId}",
                        GATE_ID, PLATFORM_ID, DATASET_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        with(result)
                .assertThat("$.requestId", is(REQUEST_ID))
                .assertThat("$.status", is("COMPLETE"))
                .assertThat("$.data", is(EFTI_DATA_ENCODED));
    }

    @Test
    @WithMockUser
    void getDatasetWithSubsetsTest() throws Exception {
        Mockito.when(datasetSearchService.getDataset(any(UilDto.class)))
                .thenReturn(datasetDto);

        final String result = mockMvc.perform(get("/v1/dataset/{gateId}/{platformId}/{datasetId}",
                        GATE_ID, PLATFORM_ID, DATASET_ID)
                        .param("subsets", "EE01", "EU01"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        with(result)
                .assertThat("$.requestId", is(REQUEST_ID))
                .assertThat("$.status", is("COMPLETE"))
                .assertThat("$.data", is(EFTI_DATA_ENCODED));
    }

    @Test
    @WithMockUser
    void getDatasetWithSingleSubsetTest() throws Exception {
        Mockito.when(datasetSearchService.getDataset(any(UilDto.class)))
                .thenReturn(datasetDto);

        final String result = mockMvc.perform(get("/v1/dataset/{gateId}/{platformId}/{datasetId}",
                        GATE_ID, PLATFORM_ID, DATASET_ID)
                        .param("subsets", "EE01"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        with(result)
                .assertThat("$.requestId", is(REQUEST_ID))
                .assertThat("$.status", is("COMPLETE"))
                .assertThat("$.data", is(EFTI_DATA_ENCODED));
    }

    @Test
    @WithMockUser
    void getDatasetWithEmptySubsetsTest() throws Exception {
        Mockito.when(datasetSearchService.getDataset(any(UilDto.class)))
                .thenReturn(datasetDto);

        final String result = mockMvc.perform(get("/v1/dataset/{gateId}/{platformId}/{datasetId}",
                        GATE_ID, PLATFORM_ID, DATASET_ID)
                        .param("subsets", ""))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        with(result)
                .assertThat("$.requestId", is(REQUEST_ID))
                .assertThat("$.status", is("COMPLETE"))
                .assertThat("$.data", is(EFTI_DATA_ENCODED));
    }

    @Test
    @WithMockUser
    void getDatasetWithInvalidGateIdTest() throws Exception {
        mockMvc.perform(get("/v1/dataset/{gateId}/{platformId}/{datasetId}",
                        "gate<>id", PLATFORM_ID, DATASET_ID))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getDatasetWithTooLongGateIdTest() throws Exception {
        String longGateId = "a".repeat(256);
        mockMvc.perform(get("/v1/dataset/{gateId}/{platformId}/{datasetId}",
                        longGateId, PLATFORM_ID, DATASET_ID))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getDatasetWithInvalidPlatformIdTest() throws Exception {
        mockMvc.perform(get("/v1/dataset/{gateId}/{platformId}/{datasetId}",
                        GATE_ID, "platform<>id", DATASET_ID))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getDatasetWithTooLongPlatformIdTest() throws Exception {
        String longPlatformId = "a".repeat(256);
        mockMvc.perform(get("/v1/dataset/{gateId}/{platformId}/{datasetId}",
                        GATE_ID, longPlatformId, DATASET_ID))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getDatasetWithInvalidDatasetIdFormatTest() throws Exception {
        mockMvc.perform(get("/v1/dataset/{gateId}/{platformId}/{datasetId}",
                        GATE_ID, PLATFORM_ID, "invalid-uuid"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getDatasetWithTooLongDatasetIdTest() throws Exception {
        String longDatasetId = "550e8400-e29b-41d4-a716-446655440000-extra";
        mockMvc.perform(get("/v1/dataset/{gateId}/{platformId}/{datasetId}",
                        GATE_ID, PLATFORM_ID, longDatasetId))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getDatasetWithShortUuidTest() throws Exception {
        mockMvc.perform(get("/v1/dataset/{gateId}/{platformId}/{datasetId}",
                        GATE_ID, PLATFORM_ID, "550e8400-e29b-41d4-a716-44665544000"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getDatasetWithPendingStatusTest() throws Exception {
        datasetDto.setStatus(StatusEnum.PENDING);

        Mockito.when(datasetSearchService.getDataset(any(UilDto.class)))
                .thenReturn(datasetDto);

        final String result = mockMvc.perform(get("/v1/dataset/{gateId}/{platformId}/{datasetId}",
                        GATE_ID, PLATFORM_ID, DATASET_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        with(result)
                .assertThat("$.requestId", is(REQUEST_ID))
                .assertThat("$.status", is("PENDING"))
                .assertThat("$.data", is(EFTI_DATA_ENCODED));
    }

    @Test
    @WithMockUser
    void getDatasetWithErrorStatusTest() throws Exception {
        datasetDto.setStatus(StatusEnum.ERROR);
        datasetDto.setErrorCode("VALIDATION_ERROR");
        datasetDto.setErrorDescription("Invalid request parameters.");

        Mockito.when(datasetSearchService.getDataset(any(UilDto.class)))
                .thenReturn(datasetDto);

        final String result = mockMvc.perform(get("/v1/dataset/{gateId}/{platformId}/{datasetId}",
                        GATE_ID, PLATFORM_ID, DATASET_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        with(result)
                .assertThat("$.requestId", is(REQUEST_ID))
                .assertThat("$.status", is("ERROR"))
                .assertThat("$.errorCode", is("VALIDATION_ERROR"))
                .assertThat("$.errorDescription", is("Invalid request parameters."));
    }

    @Test
    @WithMockUser
    void getDatasetNotFoundTest() throws Exception {
        datasetDto.setRequestId(null);
        datasetDto.setErrorCode("DATASET_NOT_FOUND");
        datasetDto.setErrorDescription("Dataset not found.");
        datasetDto.setStatus(StatusEnum.COMPLETE);

        Mockito.when(datasetSearchService.getDataset(any(UilDto.class)))
                .thenReturn(datasetDto);

        final String result = mockMvc.perform(get("/v1/dataset/{gateId}/{platformId}/{datasetId}",
                        GATE_ID, PLATFORM_ID, DATASET_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        with(result)
                .assertThat("$.errorCode", is("DATASET_NOT_FOUND"))
                .assertThat("$.errorDescription", is("Dataset not found."))
                .assertThat("$.status", is("COMPLETE"));
    }

    @Test
    @WithMockUser
    void getDatasetPlatformNotExistsTest() throws Exception {
        datasetDto.setErrorCode("PLATFORM_ID_DOES_NOT_EXIST");
        datasetDto.setErrorDescription("Platform ID does not exist.");
        datasetDto.setStatus(StatusEnum.ERROR);

        Mockito.when(datasetSearchService.getDataset(any(UilDto.class)))
                .thenReturn(datasetDto);

        final String result = mockMvc.perform(get("/v1/dataset/{gateId}/{platformId}/{datasetId}",
                        GATE_ID, "nonexistent", DATASET_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        with(result)
                .assertThat("$.errorCode", is("PLATFORM_ID_DOES_NOT_EXIST"))
                .assertThat("$.errorDescription", is("Platform ID does not exist."))
                .assertThat("$.status", is("ERROR"));
    }

    @Test
    @WithMockUser
    void getDatasetWithWhitespaceInPathTest() throws Exception {
        String gateIdWithSpace = "gate id";
        String platformIdWithSpace = "platform id";

        Mockito.when(datasetSearchService.getDataset(any(UilDto.class)))
                .thenReturn(datasetDto);

        final String result = mockMvc.perform(get("/v1/dataset/{gateId}/{platformId}/{datasetId}",
                        gateIdWithSpace, platformIdWithSpace, DATASET_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        with(result)
                .assertThat("$.requestId", is(REQUEST_ID))
                .assertThat("$.status", is("COMPLETE"))
                .assertThat("$.data", is(EFTI_DATA_ENCODED));
    }
}
