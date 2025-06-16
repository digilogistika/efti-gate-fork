package eu.efti.platformgatesimulator.service;

import eu.efti.commons.utils.EftiSchemaUtils;
import eu.efti.commons.utils.SerializeUtils;
import eu.efti.platformgatesimulator.config.GateProperties;
import eu.efti.v1.consignment.identifier.SupplyChainConsignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Service
public class GateIntegrationService {
    private final GateProperties gateProperties;
    private final SerializeUtils serializeUtils;
    private final ApiKeyService apiKeyService;

    @Autowired
    public GateIntegrationService(GateProperties gateProperties,
                                  SerializeUtils serializeUtils, ApiKeyService apiKeyService) {
        this.gateProperties = gateProperties;
        this.serializeUtils = serializeUtils;
        this.apiKeyService = apiKeyService;
    }


    public void uploadIdentifiers(String datasetId, SupplyChainConsignment consignmentIdentifiers) throws GateIntegrationServiceException {
        try {
            var doc = EftiSchemaUtils.mapIdentifiersObjectToDoc(serializeUtils, consignmentIdentifiers);
            var xml = serializeUtils.mapDocToXmlString(doc);

            var url = gateProperties.getGateBaseUrl() + "/v1/identifiers/" + datasetId;
            RestClient restClient = RestClient
                    .builder()
                    .baseUrl(url)
                    .build();
            String response = restClient.put()
                    .header("X-API-Key", apiKeyService.getApiKey())
                    .contentType(MediaType.APPLICATION_XML)
                    .body(xml)
                    .retrieve()
                    .body(String.class);
        } catch (HttpClientErrorException e) {
            throw new GateIntegrationServiceException(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    public static class GateIntegrationServiceException extends Exception {
        public GateIntegrationServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}