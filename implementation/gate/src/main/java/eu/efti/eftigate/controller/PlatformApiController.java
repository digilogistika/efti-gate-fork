package eu.efti.eftigate.controller;

import eu.efti.commons.dto.SaveIdentifiersRequestWrapper;
import eu.efti.commons.utils.SerializeUtils;
import eu.efti.eftigate.dto.GetWhoami200Response;
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
@RequestMapping("/api/platform")
@Slf4j
public class PlatformApiController { // implements V0Api {
    private final PlatformIdentityService platformIdentityService;
    private final ValidationService validationService;
    private final SerializeUtils serializeUtils;
    private final IdentifiersService identifiersService;

    @RequestMapping(
            method = RequestMethod.PUT,
            value = "/v0/consignments/{datasetId}",
            consumes = {"application/xml"}
    )
    public ResponseEntity<Void> putConsignmentIdentifiers(
            @PathVariable String datasetId,
            @RequestBody String body
    ) {
        log.info("PUT on /api/platform/v0/consignments/{datasetId}");
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null;
        HttpServletRequest request = attributes.getRequest();
        String apiKey = request.getHeader("X-API-Key");

        platformIdentityService.validateXApiKeyHeader(apiKey);
        String name = platformIdentityService.getPlatformNameFromHeader(apiKey);

        String xml = body;
        var validationError = validationService.isXmlValid(xml);
        if (validationError.isPresent()) {
            var problemDetail = org.springframework.http.ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
            problemDetail.setDetail(validationError.get());
            return ResponseEntity.of(problemDetail).headers(h -> h.setContentType(MediaType.APPLICATION_PROBLEM_XML)).build();
        } else {
            SupplyChainConsignment consignment = serializeUtils.mapXmlStringToJaxbObject(xml, SupplyChainConsignment.class);

            SaveIdentifiersRequest saveIdentifiersRequest = new SaveIdentifiersRequest();
            saveIdentifiersRequest.setDatasetId(datasetId);
            saveIdentifiersRequest.setConsignment(consignment);
            identifiersService.createOrUpdate(new SaveIdentifiersRequestWrapper(name, saveIdentifiersRequest));

            return ResponseEntity.ok().build();
        }
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/v0/whoami",
            produces = {"application/xml"}
    )
    public ResponseEntity<GetWhoami200Response> getWhoami() {
        log.info("POST on /api/platform/v0/whoami");

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null;
        String apiKey = attributes.getRequest().getHeader("X-API-Key");

        platformIdentityService.validateXApiKeyHeader(apiKey);
        String name = platformIdentityService.getPlatformNameFromHeader(apiKey);

        return ResponseEntity.ok(new GetWhoami200Response(name, "PLATFORM"));
    }
}
