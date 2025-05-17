package eu.efti.eftigate.controller;

import eu.efti.eftigate.controller.api.PlatformApiV1;
import eu.efti.eftigate.exception.XApiKeyValidationexception;
import eu.efti.eftigate.service.PlatformIdentityService;
import eu.efti.eftigate.service.request.IdentifiersRequestService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/platforms")
@Slf4j
public class PlatformApiController implements PlatformApiV1 {
    private final IdentifiersRequestService identifiersRequestService;
    private final PlatformIdentityService platformIdentityService;


    public ResponseEntity<String> postConsignmentIdentifiers(
            @RequestBody String body,
            @RequestHeader("X-API-Key") String apiKey
    ) {
        log.info("POST on /api/v1/platforms/identifiers");
        try {
            platformIdentityService.validateXApiKeyHeader(apiKey);
        } catch (XApiKeyValidationexception e) {
            log.error("X-API-Key validation failed: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        }

        try {
            String name = platformIdentityService.getPlatformNameFromHeader(apiKey);
            identifiersRequestService.createOrUpdate(body, name);
        } catch (SAXException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return ResponseEntity.accepted().build();
    }
}
