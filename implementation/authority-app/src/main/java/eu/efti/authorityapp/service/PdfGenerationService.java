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
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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

            // Step 3: Populate the parameters map from the CmrDto
            final Map<String, Object> parameters = new HashMap<>();

            if (cmrDto.getConsignor() != null) {
                parameters.put("consignorName", cmrDto.getConsignor().getName());
                if (cmrDto.getConsignor().getPostalAddress() != null) {
                    CmrDto.PostalAddress address = cmrDto.getConsignor().getPostalAddress();
                    parameters.put("consignorStreet", address.getStreetName());
                    parameters.put("consignorCity", address.getCityName());
                    parameters.put("consignorPostcode", address.getPostcode());
                    parameters.put("consignorCountry", address.getCountryCode());
                }
            }

            if (cmrDto.getConsignee() != null) {
                parameters.put("consigneeName", cmrDto.getConsignee().getName());
                if (cmrDto.getConsignor().getPostalAddress() != null) {
                    CmrDto.PostalAddress address = cmrDto.getConsignor().getPostalAddress();
                    parameters.put("consigneeStreet", address.getStreetName());
                    parameters.put("consigneeCity", address.getCityName());
                    parameters.put("consigneePostcode", address.getPostcode());
                    parameters.put("consigneeCountry", address.getCountryCode());
                }
            }

            log.info("Populated report parameters.");

            // Step 4: Fill the report using the parameters and an EMPTY data source
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