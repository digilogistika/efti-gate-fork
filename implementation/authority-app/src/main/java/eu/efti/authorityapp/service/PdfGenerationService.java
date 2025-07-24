// file: implementation/authority-app/src/main/java/eu/efti/authorityapp/service/PdfGenerationService.java
package eu.efti.authorityapp.service;

import eu.efti.authorityapp.dto.CmrDto;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class PdfGenerationService {

    public byte[] generatePdf(
            final String requestId,
            final byte[] xmlData) {

        try {
            log.info("Starting PDF generation for request ID: {}", requestId);

            // Step 1: Parse the raw XML into our CmrDto object using JAXB
            final JAXBContext jaxbContext = JAXBContext.newInstance(CmrDto.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            final CmrDto cmrDto = (CmrDto) jaxbUnmarshaller.unmarshal(new ByteArrayInputStream(xmlData));
            log.info("Successfully parsed XML data into CmrDto.");

            // Step 2: Load and compile the Jasper report template
            log.info("Loading JRXML template from classpath: reports/dataset_report.jrxml");
            final InputStream reportStream = new ClassPathResource("reports/dataset_report.jrxml").getInputStream();
            final JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            log.info("Report compiled successfully.");

            final Map<String, Object> parameters = new HashMap<>();

            // Box 1: Sender (Consignor)
            Optional.ofNullable(cmrDto.getConsignor()).ifPresent(consignor -> {
                parameters.put("senderCompanyName", consignor.getName());
                Optional.ofNullable(consignor.getPostalAddress()).ifPresent(address -> {
                    parameters.put("senderStreet", address.getStreetName());
                    parameters.put("senderCity", address.getCityName());
                    parameters.put("senderPostCode", address.getPostcode());
                    parameters.put("senderCountry", address.getCountryCode());
                });
            });

            // Box 2: Consignee
            Optional.ofNullable(cmrDto.getConsignee()).ifPresent(consignee -> {
                parameters.put("consigneeCompanyName", consignee.getName());
                // BUG FIX: Was using consignor's address, now correctly uses consignee's.
                Optional.ofNullable(consignee.getPostalAddress()).ifPresent(address -> {
                    parameters.put("consigneeStreet", address.getStreetName());
                    parameters.put("consigneeCity", address.getCityName());
                    parameters.put("consigneePostcode", address.getPostcode());
                    // BUG FIX: JRXML parameter is "consigneeCountryCode" not "consigneeCountry"
                    parameters.put("consigneeCountryCode", address.getCountryCode());
                });
            });

//            // Box 3: Taking over the Goods
//            Optional.ofNullable(cmrDto.getCarrierAcceptanceLocation())
//                    .ifPresent(loc -> parameters.put("takingOverTheGoodsPlace", loc.getName()));
//            // Note: The XML has one 'carrierAcceptanceDateTime'. We map it to 'Arrival'. 'Departure' is not in the XML.
//            parameters.put("logisticsTimeOfArrivalDateTime", cmrDto.getCarrierAcceptanceDateTime());
//
            // Box 4: Delivery of the Goods
            Optional.ofNullable(cmrDto.getConsigneeReceiptLocation())
                    .ifPresent(loc -> parameters.put("deliveryOfTheGoodsPlace", loc.getName()));
//
//            // Box 5: Sender's instructions
//            parameters.put("sendersInstructions", cmrDto.getSendersInstructions());
//
            // Box 6: Carrier
            Optional.ofNullable(cmrDto.getCarrier()).ifPresent(carrier -> {
                parameters.put("carrierCompanyName", carrier.getName());
                Optional.ofNullable(carrier.getPostalAddress()).ifPresent(address -> {
                    parameters.put("carrierStreet", address.getStreetName());
                    parameters.put("carrierCity", address.getCityName());
                    parameters.put("carrierPostcode", address.getPostcode());
                    parameters.put("carrierCountry", address.getCountryCode());
                });
            });
//            Optional.ofNullable(cmrDto.getMainCarriageTransportMovement())
//                    .map(CmrDto.MainCarriageTransportMovement::getUsedTransportMeans)
//                    .ifPresent(utm -> parameters.put("carrierLicensePlate", utm.getId()));
//
//
//            // Box 8: Carrier's reservations and observations
//            parameters.put("carrierReservationsObservations", cmrDto.getCarrierReservationsObservations());
//
//            // Box 9: Documents Remarks
//            Optional.ofNullable(cmrDto.getDocumentsRemarks())
//                    .ifPresent(doc -> {
//                        String remark = "Type: " + doc.getTypeCode() + ", ID: " + doc.getId();
//                        parameters.put("documentsRemarks", remark);
//                    });
//
            // Box 16: Special agreements
            parameters.put("customSpecialAgreement", cmrDto.getCustomSpecialAgreement());
//
//            // Box 18: Other useful particulars
//            parameters.put("customParticulars", cmrDto.getCustomParticulars());
//
//            // Box 19: Cash on delivery
//            Optional.ofNullable(cmrDto.getCustomCashOnDelivery())
//                    .ifPresent(cod -> parameters.put("customCashOnDelivery", cod.getValue()));
//
//            // Box 21: Established In / Date
//            // The XML does not provide a clear source for "Established In" city or the date.
//            // We are using the transportContractDocument ID as a placeholder to show data flow.
//            Optional.ofNullable(cmrDto.getTransportContractDocument())
//                    .ifPresent(tcd -> parameters.put("customEstablishedIn", "Ref Doc: " + tcd.getId()));
//
//            // Handle the item table (Dataset)
//            // The XML data is flat, not a list of items as the JRXML expects.
//            // We will construct a "best-effort" single-item list from the top-level consignment data.
//            final List<Map<String, Object>> items = new ArrayList<>();
//            final Map<String, Object> item = new HashMap<>();

//            if (cmrDto.getTransportPackages() != null && !cmrDto.getTransportPackages().isEmpty()) {
//                CmrDto.TransportPackage firstPackage = cmrDto.getTransportPackages().get(0);
//                item.put("logisticsPackageItemQuantity", firstPackage.getItemQuantity());
//                item.put("logisticsPackageType", firstPackage.getTypeCode());
//            }
//
//            Optional.ofNullable(cmrDto.getNatureIdentificationCargo())
//                    .ifPresent(cargo -> item.put("transportCargoIdentification", cargo.getIdentificationText()));
//
//            Optional.ofNullable(cmrDto.getGrossWeight())
//                    .ifPresent(weight -> item.put("supplyChainConsignmentItemGrossWeight", weight.getValue()));
//
//            Optional.ofNullable(cmrDto.getGrossVolume())
//                    .ifPresent(volume -> item.put("supplyChainConsignmentItemGrossVolume", volume.getValue()));
//
//
//            items.add(item);
//            parameters.put("items", new JRBeanCollectionDataSource(items));


            log.info("Populated report parameters.");

            // Step 4: Fill the report
            log.info("Filling the report with data...");
            final byte[] pdfBytes = JasperRunManager.runReportToPdf(jasperReport, parameters, new JREmptyDataSource());
            log.info("Report filled and exported to PDF successfully.");

            return pdfBytes;

        } catch (final Exception e) {
            log.error("Error during PDF generation for request ID: {}", requestId, e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }
}