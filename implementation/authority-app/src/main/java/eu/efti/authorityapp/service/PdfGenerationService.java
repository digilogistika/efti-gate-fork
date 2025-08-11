package eu.efti.authorityapp.service;

import eu.efti.authorityapp.dto.PdfGenerationResult;
import eu.efti.v1.consignment.common.LogisticsPackage;
import eu.efti.v1.consignment.common.SupplyChainConsignment;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PdfGenerationService {

    private Date fromDateTime(eu.efti.v1.types.DateTime dateTime) {
        if (dateTime == null || StringUtils.isBlank(dateTime.getValue())) {
            return null;
        }
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

    private String toStr(Object obj) {
        if (obj == null) {
            return "N/A";
        }
        final String s = obj.toString();
        return StringUtils.isBlank(s) ? "N/A" : s;
    }

    private <T> T getSafely(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            return null;
        }
    }

    public PdfGenerationResult generatePdf(
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
            parameters.put("consignorCompanyName", toStr(getSafely(() -> sc.getConsignor().getName().get(0))));
            parameters.put("consignorStreet", toStr(getSafely(() -> sc.getConsignor().getPostalAddress().getStreetName().get(0))));
            parameters.put("consignorCity", toStr(getSafely(() -> sc.getConsignor().getPostalAddress().getCityName().get(0))));
            parameters.put("consignorPostCode", toStr(getSafely(() -> sc.getConsignor().getPostalAddress().getPostcode().get(0))));
            parameters.put("consignorCountryCode", toStr(getSafely(() -> sc.getConsignor().getPostalAddress().getCountryCode().value())));

            // Box 2: Consignee
            parameters.put("consigneeCompanyName", toStr(getSafely(() -> sc.getConsignee().getName().get(0))));
            parameters.put("consigneeStreet", toStr(getSafely(() -> sc.getConsignee().getPostalAddress().getStreetName().get(0))));
            parameters.put("consigneeCity", toStr(getSafely(() -> sc.getConsignee().getPostalAddress().getCityName().get(0))));
            parameters.put("consigneePostcode", toStr(getSafely(() -> sc.getConsignee().getPostalAddress().getPostcode().get(0))));
            parameters.put("consigneeCountryCode", toStr(getSafely(() -> sc.getConsignee().getPostalAddress().getCountryCode().value())));

            // Box 3: Taking over the Goods
            parameters.put("carrierAcceptanceLocationName", toStr(getSafely(() -> sc.getCarrierAcceptanceLocation().getName().get(0))));
            parameters.put("carrierAcceptanceDateTime", fromDateTime(sc.getCarrierAcceptanceDateTime()));

            // Box 4: Delivery of the Goods
            parameters.put("consigneeReceiptLocationName", toStr(getSafely(() -> sc.getConsigneeReceiptLocation().getName().get(0))));

            // Box 5: Consignor's instructions
            parameters.put("consignorProvidedBorderClearanceInstructions", toStr(getSafely(() -> sc.getConsignorProvidedBorderClearanceInstructions().get(0).getDescription().get(0))));

            // Box 6: Carrier
            parameters.put("carrierCompanyName", toStr(getSafely(() -> sc.getCarrier().getName().get(0))));
            parameters.put("carrierStreet", toStr(getSafely(() -> sc.getCarrier().getPostalAddress().getStreetName().get(0))));
            parameters.put("carrierCity", toStr(getSafely(() -> sc.getCarrier().getPostalAddress().getCityName().get(0))));
            parameters.put("carrierPostcode", toStr(getSafely(() -> sc.getCarrier().getPostalAddress().getPostcode().get(0))));
            parameters.put("carrierCountryCode", toStr(getSafely(() -> sc.getCarrier().getPostalAddress().getCountryCode().value())));
            parameters.put("mainCarriageTransportMeansId", toStr(getSafely(() -> sc.getMainCarriageTransportMovement().get(0).getUsedTransportMeans().getId().getValue())));

            // Box 7: Connecting Carriers
            parameters.put("connectingCarrierCompanyName", toStr(getSafely(() -> sc.getConnectingCarrier().get(0).getName().get(0))));
            parameters.put("connectingCarrierStreet", toStr(getSafely(() -> sc.getConnectingCarrier().get(0).getPostalAddress().getStreetName().get(0))));
            parameters.put("connectingCarrierCity", toStr(getSafely(() -> sc.getConnectingCarrier().get(0).getPostalAddress().getCityName().get(0))));
            parameters.put("connectingCarrierPostcode", toStr(getSafely(() -> sc.getConnectingCarrier().get(0).getPostalAddress().getPostcode().get(0))));
            parameters.put("connectingCarrierCountryCode", toStr(getSafely(() -> sc.getConnectingCarrier().get(0).getPostalAddress().getCountryCode().value())));

            // Box 10-15
            final String natureOfGoodsText = getSafely(() -> sc.getNatureIdentificationCargo().getIdentificationText().get(0).getValue());
            final List<LogisticsPackage> packages = Optional.ofNullable(sc.getTransportPackage()).orElse(Collections.emptyList());

            final List<Map<String, Object>> items = packages.stream().limit(6).map(pkg -> {
                Map<String, Object> item = new HashMap<>();
                String packageType = "N/A";
                if (getSafely(() -> pkg.getTypeText().get(0)) != null) {
                    packageType = pkg.getTypeText().get(0).getValue();
                } else if (getSafely(() -> !pkg.getTypeCode().isEmpty()) == Boolean.TRUE) {
                    packageType = String.join(", ", pkg.getTypeCode());
                }
                item.put("logisticsPackageType", packageType);
                item.put("logisticsPackageItemQuantity", getSafely(() -> pkg.getItemQuantity().intValue()));
                item.put("transportCargoIdentification", natureOfGoodsText);
                item.put("supplyChainConsignmentItemGrossWeight", getSafely(() -> sc.getGrossWeight().get(0).getValue().floatValue()));
                item.put("supplyChainConsignmentItemGrossVolume", getSafely(() -> sc.getGrossVolume().get(0).getValue().floatValue()));
                return item;
            }).collect(Collectors.toList());

            parameters.put("items", new JRBeanCollectionDataSource(items));

            // Other Parameters
            parameters.put("information", toStr(getSafely(() -> String.join(", ", sc.getInformation()))));
            parameters.put("associatedDocumentId", toStr(getSafely(() -> sc.getAssociatedDocument().get(0).getId().getValue())));
            parameters.put("associatedDocumentTypeCode", toStr(getSafely(() -> sc.getAssociatedDocument().get(0).getTypeCode())));
            parameters.put("contractTermsText", toStr(getSafely(() -> String.join(", ", sc.getContractTermsText()))));
            parameters.put("deliveryInformation", toStr(sc.getDeliveryInformation()));
            parameters.put("codAmount", getSafely(() -> sc.getCODAmount().getValue().floatValue()));
            parameters.put("codAmountCurrency", toStr(getSafely(() -> sc.getCODAmount().getCurrencyId().toString())));
            parameters.put("transportContractDocumentId", toStr(getSafely(() -> sc.getTransportContractDocument().getId().getValue())));
            parameters.put("deliveryEventDateTime", fromDateTime(getSafely(() -> sc.getDeliveryEvent().getActualOccurrenceDateTime())));
            parameters.put("deliveryEventLocationName", toStr(getSafely(() -> sc.getDeliveryEvent().getOccurrenceLocation().getName().get(0))));
            parameters.put("consigneeSignatureDate", fromDateTime(getSafely(() -> sc.getDeliveryEvent().getActualOccurrenceDateTime())));

            log.info("Populated report parameters successfully.");

            final byte[] pdfBytes = JasperRunManager.runReportToPdf(jasperReport, parameters, new JREmptyDataSource());
            log.info("Report filled and exported to PDF successfully.");

            return new PdfGenerationResult(pdfBytes, sc);
            
        } catch (final Exception e) {
            log.error("CRITICAL ERROR during PDF generation for request ID: {}", requestId, e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }
}