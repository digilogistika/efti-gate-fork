package eu.efti.eftigate.controller;

import eu.efti.commons.dto.IdentifiersResponseDto;
import eu.efti.commons.enums.CountryIndicator;
import eu.efti.commons.validator.ValueOfEnum;
import eu.efti.eftigate.controller.api.IdentifiersControllerApi;
import eu.efti.eftigate.service.ControlService;
import eu.efti.v1.edelivery.IdentifierType;
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

    private final ControlService controlService;

    @Override
    public ResponseEntity<IdentifiersResponseDto> getIdentifiers(
            String identifier,
            String modeCode,
            List<@ValueOfEnum(enumClass = IdentifierType.class, message = "IDENTIFIER_TYPE_INCORRECT") String> identifierType,
            String registrationCountryCode,
            Boolean dangerousGoodsIndicator,
            List<@ValueOfEnum(enumClass = CountryIndicator.class, message = "GATE_INDICATOR_INCORRECT") String> eftiGateIndicator,
            Boolean callback) {
        log.info("GET on /v1/identifiers on gates {} with params, identifier: {}, identifierType:{}, modeCode: {}, registrationCountryCode: {}, dangerousGoodsIndicator: {} ",
                StringUtils.join(eftiGateIndicator, ","), identifier,
                StringUtils.join(identifierType, ","), modeCode,
                registrationCountryCode, dangerousGoodsIndicator);

        // TODO
        // Tee korda SearchWithIdentifiersRequestDto
        // Loo siin SearchWithIdentifiersRequestDto
        // Anna see service-ile
        // Tee service ümber nii, et see oleks sünkroone


        return new ResponseEntity<>(controlService.createIdentifiersControl(identifiersRequestDto), HttpStatus.ACCEPTED);
    }
}
