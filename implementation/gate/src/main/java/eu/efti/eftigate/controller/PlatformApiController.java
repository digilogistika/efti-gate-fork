package eu.efti.eftigate.controller;

import eu.efti.commons.dto.SaveIdentifiersRequestWrapper;
import eu.efti.commons.utils.SerializeUtils;
import eu.efti.eftigate.service.PlatformIdentityService;
import eu.efti.eftigate.service.request.ValidationService;
import eu.efti.identifiersregistry.service.IdentifiersService;
import eu.efti.v1.consignment.identifier.SupplyChainConsignment;
import eu.efti.v1.edelivery.SaveIdentifiersRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@AllArgsConstructor
@Slf4j
public class PlatformApiController {
    private final PlatformIdentityService platformIdentityService;
    private final ValidationService validationService;
    private final SerializeUtils serializeUtils;
    private final IdentifiersService identifiersService;

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/v1/identifiers/{datasetId}",
            consumes = {"application/xml"}
    )
    public ResponseEntity<Void> putConsignmentIdentifiers(
            @PathVariable String datasetId,
            @RequestBody String body
    ) {
        log.info("POST on /v1/identifiers/{datasetId}");
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null;
        HttpServletRequest request = attributes.getRequest();
        String apiKey = request.getHeader("X-API-Key");

        String platformId = platformIdentityService.getPlatformIdFromHeader(apiKey);

        var validationError = validationService.isXmlValid(body);
        if (validationError.isPresent()) {
            var problemDetail = org.springframework.http.ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
            problemDetail.setDetail(validationError.get());
            return ResponseEntity.of(problemDetail).headers(h -> h.setContentType(MediaType.APPLICATION_PROBLEM_XML)).build();
        }

        SupplyChainConsignment consignment = serializeUtils.mapXmlStringToJaxbObject(body, SupplyChainConsignment.class);

        SaveIdentifiersRequest saveIdentifiersRequest = new SaveIdentifiersRequest();
        saveIdentifiersRequest.setDatasetId(datasetId);
        saveIdentifiersRequest.setConsignment(consignment);
        identifiersService.createOrUpdate(new SaveIdentifiersRequestWrapper(platformId, saveIdentifiersRequest));

        return ResponseEntity.ok().build();
    }
}
