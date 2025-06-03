package eu.efti.eftigate.service;

import eu.efti.commons.dto.ControlDto;
import eu.efti.commons.dto.PostFollowUpRequestDto;
import eu.efti.commons.utils.SerializeUtils;
import eu.efti.edeliveryapconnector.dto.NotificationContentDto;
import eu.efti.edeliveryapconnector.dto.NotificationDto;
import eu.efti.edeliveryapconnector.dto.NotificationType;
import eu.efti.eftigate.service.request.UilRequestService;
import eu.efti.v1.edelivery.ObjectFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@AllArgsConstructor
public class PlatformApiService {
    private final SerializeUtils serializeUtils;
    private final ObjectFactory objectFactory = new ObjectFactory();
    private final WebClient.Builder webClientBuilder;
    private final PlatformIdentityService platformIdentityService;
    private final UilRequestService uilRequestService;

    @Async
    public CompletableFuture<Void> sendFollowUpRequest(PostFollowUpRequestDto postFollowUpRequestDto, ControlDto controlDto) {
        try {
            log.info("Sending follow up request to platform with id: {}", controlDto.getPlatformId());
            webClientBuilder
                    .build()
                    .post()
                    .uri(platformIdentityService.getRequestBaseUrl(controlDto.getPlatformId()) + "/" + controlDto.getDatasetId() + "/follow-up")
                    .contentType(MediaType.TEXT_PLAIN)
                    .bodyValue(postFollowUpRequestDto.getMessage())
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("Successfully sent follow up request to platform with id: {}", controlDto.getPlatformId());
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Error while sending follow up request to platform with id: {}", controlDto.getPlatformId(), e);
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Async
    public CompletableFuture<Void> sendUilRequest(ControlDto controlDto) {
        try {
            log.info("Sending UIL request to platform with id: {}", controlDto.getPlatformId());

            String uri = platformIdentityService.getRequestBaseUrl(controlDto.getPlatformId()) +
                    "/" + controlDto.getDatasetId() +
                    "?" + "&subsetId=" + "full";  // TODO: add real subsets support

            ResponseEntity<String> response = webClientBuilder
                    .build()
                    .get()
                    .uri(uri)
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            // wrap the response in a edelivery message format
            String transformedXml = transformXml(response.getBody(), controlDto.getRequestId());

            NotificationDto notificationDto = NotificationDto
                    .builder()
                    .notificationType(NotificationType.RECEIVED)
                    .content(NotificationContentDto
                            .builder()
                            .body(transformedXml)
                            .fromPartyId(controlDto.getPlatformId())
                            .build()
                    )
                    .build();

            uilRequestService.manageResponseReceived(notificationDto);

            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Error while sending UIL request to platform with id: {}", controlDto.getPlatformId(), e);
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }


    public String transformXml(String inputXml, String requestId) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Parse the input XML
        Document inputDoc = builder.parse(new InputSource(new StringReader(inputXml)));
        Element consignmentElement = inputDoc.getDocumentElement();

        // Create a new document for the output
        Document outputDoc = builder.newDocument();

        // Create the root uilResponse element with namespaces
        Element uilResponseElement = outputDoc.createElementNS("http://efti.eu/v1/edelivery", "uilResponse");
        uilResponseElement.setAttribute("xmlns", "http://efti.eu/v1/edelivery");
        uilResponseElement.setAttribute("xmlns:ns2", "http://efti.eu/v1/consignment/common");
        uilResponseElement.setAttribute("xmlns:ns3", "http://efti.eu/v1/consignment/identifier");
        uilResponseElement.setAttribute("status", "200");

        // Set the requestId from the parameter or generate a new one if empty
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }
        uilResponseElement.setAttribute("requestId", requestId);
        outputDoc.appendChild(uilResponseElement);

        // Create the ns2:consignment element
        Element ns2ConsignmentElement = outputDoc.createElementNS("http://efti.eu/v1/consignment/common", "ns2:consignment");
        uilResponseElement.appendChild(ns2ConsignmentElement);

        // Copy all child elements from the input to the output, preserving structure but adding ns2 prefix
        copyElementsWithNamespace(inputDoc, consignmentElement, outputDoc, ns2ConsignmentElement, "http://efti.eu/v1/consignment/common", "ns2");

        // Transform the document to a string
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(outputDoc), new StreamResult(writer));

        return writer.toString();
    }

    private void copyElementsWithNamespace(Document sourceDoc, Element sourceElement, Document targetDoc, Element targetParent, String namespace, String prefix) {
        NodeList childNodes = sourceElement.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element sourceChild = (Element) node;
                String nodeName = sourceChild.getLocalName() != null ? sourceChild.getLocalName() : sourceChild.getNodeName();

                Element targetChild = targetDoc.createElementNS(namespace, prefix + ":" + nodeName);
                targetParent.appendChild(targetChild);

                // Copy attributes
                for (int j = 0; j < sourceChild.getAttributes().getLength(); j++) {
                    Node attr = sourceChild.getAttributes().item(j);
                    targetChild.setAttribute(attr.getNodeName(), attr.getNodeValue());
                }

                // Recursively copy child elements
                copyElementsWithNamespace(sourceDoc, sourceChild, targetDoc, targetChild, namespace, prefix);
            } else if (node.getNodeType() == Node.TEXT_NODE && !node.getNodeValue().trim().isEmpty()) {
                targetParent.appendChild(targetDoc.createTextNode(node.getNodeValue()));
            }
        }
    }
}
