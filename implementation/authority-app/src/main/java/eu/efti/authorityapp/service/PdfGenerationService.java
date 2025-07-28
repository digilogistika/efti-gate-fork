package eu.efti.authorityapp.service;

import eu.efti.v1.consignment.common.LogisticsPackage;
import eu.efti.v1.consignment.common.ReferencedDocument;
import eu.efti.v1.consignment.common.SupplyChainConsignment;
import eu.efti.v1.consignment.common.TradeAddress;
import eu.efti.v1.consignment.common.TradeParty;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class PdfGenerationService {

    private Date fromDateTime(eu.efti.v1.types.DateTime dateTime) {
        if (dateTime == null || StringUtils.isBlank(dateTime.getValue())) { return null; }
        return switch (dateTime.getFormatId()) {
            case "102" -> {
                LocalDate localDate = LocalDate.parse(dateTime.getValue(), DateTimeFormatter.ofPattern("yyyyMMdd"));
                yield Date.from(localDate.atStartOfDay().toInstant(ZoneOffset.UTC));
            }
            case "205" -> {
                OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateTime.getValue(), DateTimeFormatter.ofPattern("yyyyMMddHHmmZ"));
                yield Date.from(offsetDateTime.toInstant());
            }
            default -> {
                log.warn("Unsupported DateTime formatId: {}", dateTime.getFormatId());
                yield null;
            }
        };
    }

    // Helper to prevent "null" strings in the PDF
    private String toStr(Object obj) {
        return Objects.toString(obj, "");
    }

    public byte[] generatePdf(
            final String requestId,
            final byte[] xmlData) {

        try {
            log.info("Starting PDF generation for request ID: {}", requestId);

            final JAXBContext jaxbContext = JAXBContext.newInstance(SupplyChainConsignment.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            XMLInputFactory xif = XMLInputFactory.newFactory();
            xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            XMLStreamReader xsr = xif.createXMLStreamReader(new ByteArrayInputStream(xmlData));
            JAXBElement<SupplyChainConsignment> jaxbElement = jaxbUnmarshaller.unmarshal(xsr, SupplyChainConsignment.class);
            final SupplyChainConsignment sc = jaxbElement.getValue();
            log.info("Successfully parsed XML data into SupplyChainConsignment object.");

            final InputStream reportStream = new ClassPathResource("reports/dataset_report.jrxml").getInputStream();
            final JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            log.info("Report compiled successfully.");

            final Map<String, Object> parameters = new HashMap<>();

            // --- PARAMETER MAPPING ---

            // Box 1: Consignor
            TradeParty consignor = sc.getConsignor();
            TradeAddress consignorAddress = (consignor != null) ? consignor.getPostalAddress() : null;
            parameters.put("consignorCompanyName", toStr((consignor != null && consignor.getName() != null && !consignor.getName().isEmpty()) ? consignor.getName().get(0) : ""));
            parameters.put("consignorStreet", toStr((consignorAddress != null && consignorAddress.getStreetName() != null && !consignorAddress.getStreetName().isEmpty()) ? consignorAddress.getStreetName().get(0) : ""));
            parameters.put("consignorCity", toStr((consignorAddress != null && consignorAddress.getCityName() != null && !consignorAddress.getCityName().isEmpty()) ? consignorAddress.getCityName().get(0) : ""));
            parameters.put("consignorPostCode", toStr((consignorAddress != null && consignorAddress.getPostcode() != null && !consignorAddress.getPostcode().isEmpty()) ? consignorAddress.getPostcode().get(0) : ""));
            parameters.put("consignorCountryCode", toStr((consignorAddress != null && consignorAddress.getCountryCode() != null) ? consignorAddress.getCountryCode().value() : ""));
            parameters.put("consignorPersonName", "");

            // Box 2: Consignee
            TradeParty consignee = sc.getConsignee();
            TradeAddress consigneeAddress = (consignee != null) ? consignee.getPostalAddress() : null;
            parameters.put("consigneeCompanyName", toStr((consignee != null && consignee.getName() != null && !consignee.getName().isEmpty()) ? consignee.getName().get(0) : ""));
            parameters.put("consigneeStreet", toStr((consigneeAddress != null && consigneeAddress.getStreetName() != null && !consigneeAddress.getStreetName().isEmpty()) ? consigneeAddress.getStreetName().get(0) : ""));
            parameters.put("consigneeCity", toStr((consigneeAddress != null && consigneeAddress.getCityName() != null && !consigneeAddress.getCityName().isEmpty()) ? consigneeAddress.getCityName().get(0) : ""));
            parameters.put("consigneePostcode", toStr((consigneeAddress != null && consigneeAddress.getPostcode() != null && !consigneeAddress.getPostcode().isEmpty()) ? consigneeAddress.getPostcode().get(0) : ""));
            parameters.put("consigneeCountryCode", toStr((consigneeAddress != null && consigneeAddress.getCountryCode() != null) ? consigneeAddress.getCountryCode().value() : ""));
            parameters.put("consigneePersonName", "");

            // Box 3: Taking over the Goods
            parameters.put("carrierAcceptanceLocationName", toStr((sc.getCarrierAcceptanceLocation() != null && sc.getCarrierAcceptanceLocation().getName() != null && !sc.getCarrierAcceptanceLocation().getName().isEmpty()) ? sc.getCarrierAcceptanceLocation().getName().get(0) : ""));
            parameters.put("carrierAcceptanceDateTime", fromDateTime(sc.getCarrierAcceptanceDateTime()));
            parameters.put("logisticsTimeOfDepartureDateTime", null);

            // Box 4: Delivery of the Goods
            parameters.put("consigneeReceiptLocationName", toStr((sc.getConsigneeReceiptLocation() != null && sc.getConsigneeReceiptLocation().getName() != null) ? sc.getConsigneeReceiptLocation().getName().get(0) : ""));
            parameters.put("deliveryOfTheGoodsOpeningHours", "");

            // Box 5: Consignor's instructions
            String instructions = (sc.getConsignorProvidedBorderClearanceInstructions() != null && !sc.getConsignorProvidedBorderClearanceInstructions().isEmpty() && sc.getConsignorProvidedBorderClearanceInstructions().get(0).getDescription() != null)
                    ? sc.getConsignorProvidedBorderClearanceInstructions().get(0).getDescription().get(0) : "";
            parameters.put("consignorProvidedBorderClearanceInstructions", toStr(instructions));

            // Box 6: Carrier
            TradeParty carrier = sc.getCarrier();
            TradeAddress carrierAddress = (carrier != null) ? carrier.getPostalAddress() : null;
            parameters.put("carrierCompanyName", toStr((carrier != null && carrier.getName() != null && !carrier.getName().isEmpty()) ? carrier.getName().get(0) : ""));
            parameters.put("carrierStreet", toStr((carrierAddress != null && carrierAddress.getStreetName() != null && !carrierAddress.getStreetName().isEmpty()) ? carrierAddress.getStreetName().get(0) : ""));
            parameters.put("carrierCity", toStr((carrierAddress != null && carrierAddress.getCityName() != null && !carrierAddress.getCityName().isEmpty()) ? carrierAddress.getCityName().get(0) : ""));
            parameters.put("carrierPostcode", toStr((carrierAddress != null && carrierAddress.getPostcode() != null && !carrierAddress.getPostcode().isEmpty()) ? carrierAddress.getPostcode().get(0) : ""));
            parameters.put("carrierCountryCode", toStr((carrierAddress != null && carrierAddress.getCountryCode() != null) ? carrierAddress.getCountryCode().value() : ""));
            parameters.put("carrierPersonName", "");

            String transportMeansId = (sc.getMainCarriageTransportMovement() != null && !sc.getMainCarriageTransportMovement().isEmpty() && sc.getMainCarriageTransportMovement().get(0).getUsedTransportMeans() != null)
                    ? sc.getMainCarriageTransportMovement().get(0).getUsedTransportMeans().getId().getValue() : "";
            parameters.put("mainCarriageTransportMeansId", toStr(transportMeansId));

            // Box 7: Connecting Carriers
            TradeParty connectingCarrier = (sc.getConnectingCarrier() != null && !sc.getConnectingCarrier().isEmpty()) ? sc.getConnectingCarrier().get(0) : null;
            TradeAddress connectingCarrierAddress = (connectingCarrier != null) ? connectingCarrier.getPostalAddress() : null;
            parameters.put("connectingCarrierCompanyName", toStr((connectingCarrier != null && connectingCarrier.getName() != null && !connectingCarrier.getName().isEmpty()) ? connectingCarrier.getName().get(0) : ""));
            parameters.put("connectingCarrierStreet", toStr((connectingCarrierAddress != null && connectingCarrierAddress.getStreetName() != null && !connectingCarrierAddress.getStreetName().isEmpty()) ? connectingCarrierAddress.getStreetName().get(0) : ""));
            parameters.put("connectingCarrierCity", toStr((connectingCarrierAddress != null && connectingCarrierAddress.getCityName() != null && !connectingCarrierAddress.getCityName().isEmpty()) ? connectingCarrierAddress.getCityName().get(0) : ""));
            parameters.put("connectingCarrierPostcode", toStr((connectingCarrierAddress != null && connectingCarrierAddress.getPostcode() != null && !connectingCarrierAddress.getPostcode().isEmpty()) ? connectingCarrierAddress.getPostcode().get(0) : ""));
            parameters.put("connectingCarrierCountryCode", toStr((connectingCarrierAddress != null && connectingCarrierAddress.getCountryCode() != null) ? connectingCarrierAddress.getCountryCode().value() : ""));
            parameters.put("connectingCarrierPersonName", "");

            // Box 10-15
            final List<Map<String, Object>> items = new ArrayList<>();
            List<LogisticsPackage> packages = sc.getTransportPackage() != null ? sc.getTransportPackage() : new ArrayList<>();

            String natureOfGoodsText = "";
            if (sc.getNatureIdentificationCargo() != null && sc.getNatureIdentificationCargo().getIdentificationText() != null && !sc.getNatureIdentificationCargo().getIdentificationText().isEmpty()) {
                natureOfGoodsText = String.valueOf(sc.getNatureIdentificationCargo().getIdentificationText().get(0).getValue());
            }

            if (!packages.isEmpty()) {
                for (LogisticsPackage pkg : packages) {
                    Map<String, Object> item = new HashMap<>();
                    String packageType = "";
                    if (pkg.getTypeText() != null && !pkg.getTypeText().isEmpty() && pkg.getTypeText().get(0) != null) {
                        packageType = pkg.getTypeText().get(0).getValue();
                    } else if (pkg.getTypeCode() != null && !pkg.getTypeCode().isEmpty()) {
                        packageType = String.join(", ", pkg.getTypeCode());
                    }
                    item.put("logisticsPackageType", packageType);
                    item.put("logisticsPackageItemQuantity", pkg.getItemQuantity() != null ? pkg.getItemQuantity().intValue() : null);
                    item.put("transportCargoIdentification", natureOfGoodsText);
                    item.put("supplyChainConsignmentItemGrossWeight", (sc.getGrossWeight() != null && !sc.getGrossWeight().isEmpty() && sc.getGrossWeight().get(0).getValue() != null) ? sc.getGrossWeight().get(0).getValue().floatValue() : null);
                    item.put("supplyChainConsignmentItemGrossVolume", (sc.getGrossVolume() != null && !sc.getGrossVolume().isEmpty() && sc.getGrossVolume().get(0).getValue() != null) ? sc.getGrossVolume().get(0).getValue().floatValue() : null);
                    item.put("logisticsShippingMarksMarking", "");
                    item.put("logisticsShippingMarksCustomBarcode", "");
                    items.add(item);
                }
            }
            parameters.put("items", new JRBeanCollectionDataSource(items));

            String info = (sc.getInformation() != null && !sc.getInformation().isEmpty()) ? String.join(", ", sc.getInformation()) : "";
            parameters.put("information", toStr(info));

            ReferencedDocument doc = (sc.getAssociatedDocument() != null && !sc.getAssociatedDocument().isEmpty()) ? sc.getAssociatedDocument().get(0) : null;
            String associatedDocId = (doc != null && doc.getId() != null) ? doc.getId().getValue() : "";
            String associatedDocType = (doc != null && doc.getTypeCode() != null) ? doc.getTypeCode() : "";
            parameters.put("associatedDocumentId", toStr(associatedDocId));
            parameters.put("associatedDocumentTypeCode", toStr(associatedDocType));

            String contractTerms = (sc.getContractTermsText() != null && !sc.getContractTermsText().isEmpty()) ? String.join(", ", sc.getContractTermsText()) : "";
            parameters.put("contractTermsText", toStr(contractTerms));

            parameters.put("customChargeCarriageValue", null);
            parameters.put("customChargeCarriagePayer", "");
            parameters.put("customChargeCarriageCurrency", "");
            parameters.put("customChargeSupplementaryValue", null);
            parameters.put("customChargeSupplementaryCurrency", "");
            parameters.put("customChargeSupplementaryPayer", "");
            parameters.put("customChargeCustomsDutiesValue", null);
            parameters.put("customChargeCustomsDutiesCurrency", "");
            parameters.put("customChargeCustomsDutiesPayer", "");
            parameters.put("customChargeOtherValue", null);
            parameters.put("customChargeOtherCurrency", "");
            parameters.put("customChargeOtherPayer", "");

            parameters.put("deliveryInformation", toStr(sc.getDeliveryInformation()));
            parameters.put("codAmount", sc.getCODAmount() != null ? sc.getCODAmount().getValue().floatValue() : null);
            parameters.put("codAmountCurrency", toStr(sc.getCODAmount() != null ? sc.getCODAmount().getCurrencyId().toString() : ""));

            String transportContractId = (sc.getTransportContractDocument() != null && sc.getTransportContractDocument().getId() != null) ? sc.getTransportContractDocument().getId().getValue() : "";
            parameters.put("transportContractDocumentId", toStr(transportContractId));

            parameters.put("deliveryEventDateTime", (sc.getDeliveryEvent() != null) ? fromDateTime(sc.getDeliveryEvent().getActualOccurrenceDateTime()) : null);
            parameters.put("consignorSealText", "");
            parameters.put("carrierSealText", "");
            parameters.put("consigneeSealText", "");
            parameters.put("consigneeReservationsObservations", "");
            parameters.put("consigneeSignatureImage", null);
            parameters.put("deliveryEventLocationName", toStr((sc.getDeliveryEvent() != null && sc.getDeliveryEvent().getOccurrenceLocation() != null) ? sc.getDeliveryEvent().getOccurrenceLocation().getName().get(0) : ""));
            parameters.put("consigneeSignatureDate", (sc.getDeliveryEvent() != null) ? fromDateTime(sc.getDeliveryEvent().getActualOccurrenceDateTime()) : null);
            parameters.put("nonContractualCarrierRemarks", "");
            parameters.put("EN_InternationalNationalTransport", "");
            parameters.put("DE_InternationalNationalTransport", "");
            parameters.put("consigneeTimeOfDeparture", null);

            log.info("Populated report parameters successfully.");

            final byte[] pdfBytes = JasperRunManager.runReportToPdf(jasperReport, parameters, new JREmptyDataSource());
            log.info("Report filled and exported to PDF successfully.");

            return pdfBytes;

        } catch (final Exception e) {
            log.error("CRITICAL ERROR during PDF generation for request ID: {}", requestId, e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }
}