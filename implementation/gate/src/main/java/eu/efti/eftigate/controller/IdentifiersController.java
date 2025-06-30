package eu.efti.eftigate.controller;

import eu.efti.commons.dto.IdentifiersResponseDto;
import eu.efti.commons.dto.SearchWithIdentifiersRequestDto;
import eu.efti.eftigate.controller.api.IdentifiersControllerApi;
import eu.efti.eftigate.service.IdentifiersSearchService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1")
@AllArgsConstructor
@Slf4j
public class IdentifiersController implements IdentifiersControllerApi {

    private final IdentifiersSearchService identifiersSearchService;

    @Override
    public ResponseEntity<IdentifiersResponseDto> getIdentifiers(
            String identifier,
            String modeCode,
            List<String> identifierType,
            String registrationCountryCode,
            Boolean dangerousGoodsIndicator,
            List<String> eftiGateIndicator,
            Boolean callback) {
        log.info("GET on /v1/identifiers on gates {} with params, identifier: {}, identifierType:{}, modeCode: {}, registrationCountryCode: {}, dangerousGoodsIndicator: {}, callback: {}",
                StringUtils.join(eftiGateIndicator, ","), identifier,
                StringUtils.join(identifierType, ","), modeCode,
                registrationCountryCode, dangerousGoodsIndicator, callback);

        SearchWithIdentifiersRequestDto dto = SearchWithIdentifiersRequestDto
                .builder()
                .identifier(identifier)
                .modeCode(modeCode)
                .identifierType(identifierType)
                .registrationCountryCode(registrationCountryCode)
                .dangerousGoodsIndicator(dangerousGoodsIndicator)
                .eftiGateIndicator(eftiGateIndicator)
                .callback(callback)
                .build();
        return new ResponseEntity<>(identifiersSearchService.searchIdentifiers(dto), HttpStatus.OK);
    }
}
