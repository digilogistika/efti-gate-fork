package eu.efti.eftigate.controller;

import eu.efti.eftigate.dto.GetWhoami200Response;
import eu.efti.eftigate.service.request.IdentifiersRequestService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

@RestController
@RequestMapping("/api/v1/platform")
@Tag(name = "Platform API", description = "REST API for the platforms")
@AllArgsConstructor
@Slf4j
public class PlatformApiController {
    private final IdentifiersRequestService identifiersRequestService;

    public ResponseEntity<GetWhoami200Response> getWhoami() {
        return null;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE,
            path = "identifiers")
    public ResponseEntity<String> putConsignmentIdentifiers(
            // the xml content
            @RequestBody String body
    ) {
        log.info("POST on /api/v1/platform/identifiers");
        try {
            identifiersRequestService.createOrUpdate(body, "acme");
        } catch (SAXException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return ResponseEntity.ok().body("OK");
    }
}
