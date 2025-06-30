package eu.efti.eftigate.controller;

import eu.efti.commons.dto.IdentifiersResponseDto;
import eu.efti.commons.dto.SearchWithIdentifiersRequestDto;
import eu.efti.commons.dto.identifiers.api.ConsignmentApiDto;
import eu.efti.commons.dto.identifiers.api.IdentifierRequestResultDto;
import eu.efti.commons.enums.StatusEnum;
import eu.efti.eftigate.service.IdentifiersSearchService;
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

import java.util.List;

import static com.jayway.jsonassert.JsonAssert.with;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IdentifiersController.class)
@ContextConfiguration(classes = {IdentifiersController.class})
@ExtendWith(SpringExtension.class)
class IdentifiersControllerTest {

    public static final String REQUEST_ID = "requestId";

    private final IdentifiersResponseDto identifiersResponseDto = new IdentifiersResponseDto();
    private final ConsignmentApiDto consignmentDto = new ConsignmentApiDto();

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    IdentifiersSearchService identifiersSearchService;

    @BeforeEach
    void before() {
        identifiersResponseDto.setStatus(StatusEnum.COMPLETE);
        identifiersResponseDto.setRequestId(REQUEST_ID);
        consignmentDto.setPlatformId("acme");
        consignmentDto.setDatasetId("datasetId");
        consignmentDto.setGateId("gateId");
        identifiersResponseDto.setIdentifiers(List.of(IdentifierRequestResultDto.builder()
                .consignments(List.of(consignmentDto)).build()));


    }

    @Test
    @WithMockUser
    void getIdentifiersWithValidationErrorTest() throws Exception {
        // Test validation constraints from the API interface
        mockMvc.perform(get("/v1/identifiers/{identifier}", "?"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getIdentifiersWithInvalidModeCodeTest() throws Exception {
        Mockito.when(identifiersSearchService.searchIdentifiers(any(SearchWithIdentifiersRequestDto.class)))
                .thenReturn(identifiersResponseDto);

        mockMvc.perform(get("/v1/identifiers/{identifier}", "abc123")
                        .param("modeCode", "99"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getIdentifiersWithValidModeCodeTest() throws Exception {
        Mockito.when(identifiersSearchService.searchIdentifiers(any(SearchWithIdentifiersRequestDto.class)))
                .thenReturn(identifiersResponseDto);

        final String result = mockMvc.perform(get("/v1/identifiers/{identifier}", "abc123")
                        .param("modeCode", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        with(result)
                .assertThat("$.requestId", is("requestId"))
                .assertThat("$.status", is("COMPLETE"));
    }

    @Test
    @WithMockUser
    void getIdentifiersTest() throws Exception {
        Mockito.when(identifiersSearchService.searchIdentifiers(any(SearchWithIdentifiersRequestDto.class)))
                .thenReturn(identifiersResponseDto);

        final String result = mockMvc.perform(get("/v1/identifiers/{identifier}", "abc123")
                        .param("modeCode", "3")
                        .param("identifierType", "MEANS")
                        .param("registrationCountryCode", "FR")
                        .param("dangerousGoodsIndicator", "false")
                        .param("eftiGateIndicator", "EE")
                        .param("callback", "false"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        with(result)
                .assertThat("$.requestId", is("requestId"))
                .assertThat("$.status", is("COMPLETE"))
                .assertThat("$.identifiers[0].consignments[0].platformId", is("acme"));
    }

    @Test
    @WithMockUser
    void getIdentifiersWithMinimalParametersTest() throws Exception {
        Mockito.when(identifiersSearchService.searchIdentifiers(any(SearchWithIdentifiersRequestDto.class)))
                .thenReturn(identifiersResponseDto);

        final String result = mockMvc.perform(get("/v1/identifiers/{identifier}", "abc123"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        with(result)
                .assertThat("$.requestId", is("requestId"))
                .assertThat("$.status", is("COMPLETE"))
                .assertThat("$.identifiers[0].consignments[0].platformId", is("acme"));
    }

    @Test
    @WithMockUser
    void getIdentifiersWithMultipleValuesTest() throws Exception {
        Mockito.when(identifiersSearchService.searchIdentifiers(any(SearchWithIdentifiersRequestDto.class)))
                .thenReturn(identifiersResponseDto);

        final String result = mockMvc.perform(get("/v1/identifiers/{identifier}", "abc123")
                        .param("identifierType", "MEANS", "EQUIPMENT")
                        .param("eftiGateIndicator", "FR", "EE"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        with(result)
                .assertThat("$.requestId", is("requestId"))
                .assertThat("$.status", is("COMPLETE"))
                .assertThat("$.identifiers[0].consignments[0].platformId", is("acme"));
    }

    @Test
    @WithMockUser
    void getIdentifiersNotFoundTest() throws Exception {
        identifiersResponseDto.setRequestId(null);
        identifiersResponseDto.setErrorCode("ID_NOT_FOUND");
        identifiersResponseDto.setErrorDescription("Error requestId not found.");

        Mockito.when(identifiersSearchService.searchIdentifiers(any(SearchWithIdentifiersRequestDto.class)))
                .thenReturn(identifiersResponseDto);

        final String result = mockMvc.perform(get("/v1/identifiers/{identifier}", "nonexistent")
                        .param("eftiGateIndicator", "FR"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        with(result)
                .assertThat("$.errorCode", is("ID_NOT_FOUND"))
                .assertThat("$.errorDescription", is("Error requestId not found."))
                .assertThat("$.status", is("COMPLETE"));
    }

    @Test
    @WithMockUser
    void getIdentifiersWithPendingStatusTest() throws Exception {
        identifiersResponseDto.setStatus(StatusEnum.PENDING);

        Mockito.when(identifiersSearchService.searchIdentifiers(any(SearchWithIdentifiersRequestDto.class)))
                .thenReturn(identifiersResponseDto);

        final String result = mockMvc.perform(get("/v1/identifiers/{identifier}", "abc123")
                        .param("eftiGateIndicator", "FR"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        with(result)
                .assertThat("$.requestId", is("requestId"))
                .assertThat("$.status", is("PENDING"))
                .assertThat("$.identifiers[0].consignments[0].platformId", is("acme"));
    }

    @Test
    @WithMockUser
    void getIdentifiersWithErrorStatusTest() throws Exception {
        identifiersResponseDto.setStatus(StatusEnum.ERROR);
        identifiersResponseDto.setErrorCode("VALIDATION_ERROR");
        identifiersResponseDto.setErrorDescription("Invalid request parameters.");

        Mockito.when(identifiersSearchService.searchIdentifiers(any(SearchWithIdentifiersRequestDto.class)))
                .thenReturn(identifiersResponseDto);

        final String result = mockMvc.perform(get("/v1/identifiers/{identifier}", "invalid")
                        .param("eftiGateIndicator", "FR"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        with(result)
                .assertThat("$.errorCode", is("VALIDATION_ERROR"))
                .assertThat("$.errorDescription", is("Invalid request parameters."))
                .assertThat("$.status", is("ERROR"));
    }
}
