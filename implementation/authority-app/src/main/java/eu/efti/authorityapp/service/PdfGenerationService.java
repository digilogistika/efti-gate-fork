// file: implementation/authority-app/src/main/java/eu/efti/authorityapp/service/PdfGenerationService.java
package eu.efti.authorityapp.service;

import eu.efti.authorityapp.dto.CmrDto;
import eu.efti.v1.consignment.common.SupplyChainConsignment;
import eu.efti.v1.types.DateTime;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperRunManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class PdfGenerationService {

    private OffsetDateTime fromDateTime(eu.efti.authorityapp.dto.CmrDto.DateTime dateTime) {
        if (dateTime == null || StringUtils.isBlank(dateTime.getValue())) return null;
        return switch (dateTime.getFormatId()) {
            case "102" -> {
                LocalDate localDate = LocalDate.parse(dateTime.getValue(), DateTimeFormatter.ofPattern("yyyyMMdd"));
                yield localDate.atStartOfDay().atOffset(ZoneOffset.UTC);
            }
            case "205" -> OffsetDateTime.parse(dateTime.getValue(), DateTimeFormatter.ofPattern("yyyyMMddHHmmZ"));
            default -> throw new UnsupportedOperationException("Unsupported formatId: " + dateTime.getFormatId());
        };
    }

    public byte[] generatePdf(
            final String requestId,
            final byte[] xmlData) {

        try {
            log.info("Starting PDF generation for request ID: {}", requestId);

            SupplyChainConsignment supplyChainConsignment = new SupplyChainConsignment();

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

            // Step 3: Populate the report parameters from the CmrDto
            final Map<String, Object> parameters = new HashMap<>();

            // Box 1: Consignor
            Optional.ofNullable(cmrDto.getConsignor()).ifPresent(consignor -> {
                parameters.put("consignorCompanyName", consignor.getName());
                Optional.ofNullable(consignor.getPostalAddress()).ifPresent(address -> {
                    parameters.put("consignorStreet", address.getStreetName());
                    parameters.put("consignorCity", address.getCityName());
                    parameters.put("consignorPostcode", address.getPostcode());
                    parameters.put("consignorCountryCode", address.getCountryCode());
                });
            });

            // Box 2: Consignee
            Optional.ofNullable(cmrDto.getConsignee()).ifPresent(consignee -> {
                parameters.put("consigneeCompanyName", consignee.getName());
                Optional.ofNullable(consignee.getPostalAddress()).ifPresent(address -> {
                    parameters.put("consigneeStreet", address.getStreetName());
                    parameters.put("consigneeCity", address.getCityName());
                    parameters.put("consigneePostcode", address.getPostcode());
                    parameters.put("consigneeCountryCode", address.getCountryCode());
                });
            });

            // Box 3: Taking over the Goods
            Optional.ofNullable(cmrDto.getCarrierAcceptanceLocation()).ifPresent(location -> {
                parameters.put("carrierAcceptanceLocationName", location.getName());
                Optional.ofNullable(location.getPostalAddress()).ifPresent(address -> {
                    parameters.put("carrierAcceptanceLocationStreet", address.getStreetName());
                    parameters.put("carrierAcceptanceLocationCity", address.getCityName());
                    parameters.put("carrierAcceptanceLocationPostcode", address.getPostcode());
                    parameters.put("carrierAcceptanceLocationCountryCode", address.getCountryCode());
                });
            });
            Optional.ofNullable(cmrDto.getCarrierAcceptanceDateTime()).ifPresent(dateTime -> parameters.put("carrierAcceptanceDateTime", dateTime));

            // Box 4: Delivery of the Goods
            Optional.ofNullable(cmrDto.getConsigneeReceiptLocation()).ifPresent(location -> {
                parameters.put("consigneeReceiptLocationName", location.getName());
                Optional.ofNullable(location.getPostalAddress()).ifPresent(address -> {
                    parameters.put("consigneeReceiptLocationStreet", address.getStreetName());
                    parameters.put("consigneeReceiptLocationCity", address.getCityName());
                    parameters.put("consigneeReceiptLocationPostcode", address.getPostcode());
                    parameters.put("consigneeReceiptLocationCountryCode", address.getCountryCode());
                });
            });

            // Box 5: Consignor's instructions
            Optional.ofNullable(cmrDto.getConsignorProvidedInformationText()).ifPresent(text -> parameters.put("consignorProvidedInformationText", text));

            // Box 6: Carrier
            Optional.ofNullable(cmrDto.getCarrier()).ifPresent(carrier -> {
                parameters.put("carrierCompanyName", carrier.getName());
                Optional.ofNullable(carrier.getPostalAddress()).ifPresent(address -> {
                    parameters.put("carrierStreet", address.getStreetName());
                    parameters.put("carrierCity", address.getCityName());
                    parameters.put("carrierPostcode", address.getPostcode());
                    parameters.put("carrierCountryCode", address.getCountryCode());
                });
            });



            // Box 6: Carrier License Plate
            Optional.ofNullable(cmrDto.getMainCarriageTransportMovement()).ifPresent(movement -> {
                parameters.put("mainCarriageDangerousGoodsIndicator", movement.getDangerousGoodsIndicator());
                parameters.put("mainCarriageModeCode", movement.getModeCode());
                Optional.ofNullable(movement.getUsedTransportMeans()).ifPresent(means -> {
                    Optional.ofNullable(means.getId()).ifPresent(id -> {
                        parameters.put("mainCarriageTransportMeansId", id.getValue());
                        parameters.put("mainCarriageTransportMeansIdScheme", id.getSchemeAgencyId());
                    });
                    Optional.ofNullable(means.getRegistrationCountry()).ifPresent(country -> {
                        parameters.put("mainCarriageTransportMeansCountry", country.getCode());
                    });
                });
            });

            // Box 7: Connecting Carriers
            Optional.ofNullable(cmrDto.getConnectingCarrier()).ifPresent(carrier -> {
                parameters.put("connectingCarrierCompanyName", carrier.getName());
                Optional.ofNullable(carrier.getPostalAddress()).ifPresent(address -> {
                    parameters.put("connectingCarrierStreet", address.getStreetName());
                    parameters.put("connectingCarrierCity", address.getCityName());
                    parameters.put("connectingCarrierPostcode", address.getPostcode());
                    parameters.put("connectingCarrierCountryCode", address.getCountryCode());
                });
            });

            // Box 8: Carrier's reservations and observations
            Optional.ofNullable(cmrDto.getInformation()).ifPresent(info -> parameters.put("information", info));

            // Box 9: Documents handed to the carrier
            Optional.ofNullable(cmrDto.getAssociatedDocument()).ifPresent(doc -> {
                parameters.put("associatedDocumentId", doc.getId());
                parameters.put("associatedDocumentTypeCode", doc.getTypeCode());
            });

            // Box 10-15: Consignment Item Details
            Optional.ofNullable(cmrDto.getTransportPackage()).ifPresent(pkg -> {
                parameters.put("transportPackageItemQuantity", pkg.getItemQuantity());
                parameters.put("transportPackageTypeCode", pkg.getTypeCode());
                parameters.put("transportPackageTypeText", pkg.getTypeText());
            });
            Optional.ofNullable(cmrDto.getNatureIdentificationCargo()).ifPresent(cargo -> parameters.put("natureIdentificationCargoText", cargo.getIdentificationText()));
            Optional.ofNullable(cmrDto.getGrossWeight()).ifPresent(measure -> {
                parameters.put("grossWeight", measure.getValue());
                parameters.put("grossWeightUnit", measure.getUnitId());
            });
            Optional.ofNullable(cmrDto.getGrossVolume()).ifPresent(measure -> {
                parameters.put("grossVolume", measure.getValue());
                parameters.put("grossVolumeUnit", measure.getUnitId());
            });
            Optional.ofNullable(cmrDto.getNetWeight()).ifPresent(measure -> {
                parameters.put("netWeight", measure.getValue());
                parameters.put("netWeightUnit", measure.getUnitId());
            });
            Optional.ofNullable(cmrDto.getNumberOfPackages()).ifPresent(num -> parameters.put("numberOfPackages", num));
            Optional.ofNullable(cmrDto.getIncludedConsignmentItem()).ifPresent(item -> Optional.ofNullable(item.getDimensions()).ifPresent(dims -> parameters.put("consignmentItemDimensionsDescription", dims.getDescription())));

            // Box 16: Special agreements
            Optional.ofNullable(cmrDto.getContractTermsText()).ifPresent(text -> parameters.put("contractTermsText", text));

            // Box 18: Other useful particulars
            Optional.ofNullable(cmrDto.getDeliveryInformation()).ifPresent(info -> parameters.put("deliveryInformation", info));

            // Box 19: Cash on delivery
            Optional.ofNullable(cmrDto.getCODAmount()).ifPresent(amount -> {
                parameters.put("codAmount", amount.getValue().floatValue());
                parameters.put("codAmountCurrency", amount.getCurrencyId());
            });

            // Box 21: Established In / Date
            Optional.ofNullable(cmrDto.getTransportContractDocument()).ifPresent(doc -> parameters.put("transportContractDocumentId", doc.getId()));

            // --- Other XML Elements ---
            Optional.ofNullable(cmrDto.getApplicableServiceCharge()).ifPresent(charge -> {
                parameters.put("serviceChargeId", charge.getId());
                parameters.put("serviceChargePayingPartyRoleCode", charge.getPayingPartyRoleCode());
                parameters.put("serviceChargePaymentArrangementCode", charge.getPaymentArrangementCode());
                Optional.ofNullable(charge.getAppliedAmount()).ifPresent(amount -> {
                    parameters.put("serviceChargeAppliedAmount", amount.getValue());
                    parameters.put("serviceChargeAppliedAmountCurrency", amount.getCurrencyId());
                });
                Optional.ofNullable(charge.getCalculationBasisPrice()).ifPresent(basis -> {
                    parameters.put("serviceChargeBasisQuantity", basis.getBasisQuantity());
                    parameters.put("serviceChargeCategoryTypeCode", basis.getCategoryTypeCode());
                    Optional.ofNullable(basis.getUnitAmount()).ifPresent(amount -> {
                        parameters.put("serviceChargeUnitAmount", amount.getValue());
                        parameters.put("serviceChargeUnitAmountCurrency", amount.getCurrencyId());
                    });
                });
            });
//            Optional.ofNullable(cmrDto.getAssociatedParty()).ifPresent(party -> {
//                parameters.put("associatedPartyName", party.getName());
//                Optional.ofNullable(party.getPostalAddress()).ifPresent(address -> {
//                    parameters.put("associatedPartyStreet", address.getStreetName());
//                    parameters.put("associatedPartyCity", address.getCityName());
//                    parameters.put("associatedPartyPostcode", address.getPostcode());
//                    parameters.put("associatedPartyCountryCode", address.getCountryCode());
//                });
//            });
            Optional.ofNullable(cmrDto.getCargoInsuranceInstructions()).ifPresent(text -> parameters.put("cargoInsuranceInstructions", text));
            Optional.ofNullable(cmrDto.getConsignorProvidedBorderClearanceInstructions()).ifPresent(instructions -> parameters.put("consignorProvidedBorderClearanceInstructions", instructions.getDescription()));
            Optional.ofNullable(cmrDto.getDeclaredValueForCarriageAmount()).ifPresent(amount -> {
                parameters.put("declaredValueAmount", amount.getValue());
                parameters.put("declaredValueAmountCurrency", amount.getCurrencyId());
            });
            Optional.ofNullable(cmrDto.getDeliveryEvent()).ifPresent(event -> {
                Optional.ofNullable(event.getActualOccurrenceDateTime()).ifPresent(dt -> {
                    fromDateTime(dt);
                    parameters.put("deliveryEventDateTime", dt.getValue());
                });
                Optional.ofNullable(event.getOccurrenceLocation()).ifPresent(loc -> {
                    parameters.put("deliveryEventLocationName", loc.getName());
                    Optional.ofNullable(loc.getPostalAddress()).ifPresent(address -> {
                        parameters.put("deliveryEventLocationStreet", address.getStreetName());
                        parameters.put("deliveryEventLocationCity", address.getCityName());
                        parameters.put("deliveryEventLocationPostcode", address.getPostcode());
                        parameters.put("deliveryEventLocationCountryCode", address.getCountryCode());
                    });
                });
            });
            Optional.ofNullable(cmrDto.getFreightForwarder()).ifPresent(party -> {
                parameters.put("freightForwarderName", party.getName());
                Optional.ofNullable(party.getPostalAddress()).ifPresent(address -> {
                    parameters.put("freightForwarderStreet", address.getStreetName());
                    parameters.put("freightForwarderCity", address.getCityName());
                    parameters.put("freightForwarderPostcode", address.getPostcode());
                    parameters.put("freightForwarderCountryCode", address.getCountryCode());
                });
            });
            Optional.ofNullable(cmrDto.getLogisticsRiskAnalysisResult()).ifPresent(risk -> {
                parameters.put("riskAnalysisCode", risk.getConsignmentRiskRelatedCode());
                parameters.put("riskAnalysisDescription", risk.getDescription());
                parameters.put("riskAnalysisLevelCode", risk.getLevelCode());
                parameters.put("riskAnalysisScreeningMethodCode", risk.getScreeningMethodCode());
            });
            Optional.ofNullable(cmrDto.getNotifiedWasteMaterial()).ifPresent(waste -> parameters.put("notifiedWasteMaterialId", waste.getId()));
            Optional.ofNullable(cmrDto.getOnCarriageTransportMovement()).ifPresent(movement -> parameters.put("onCarriageModeCode", movement.getModeCode()));
            Optional.ofNullable(cmrDto.getPaymentArrangementCode()).ifPresent(code -> parameters.put("paymentArrangementCode", code));
            Optional.ofNullable(cmrDto.getPreCarriageTransportMovement()).ifPresent(movement -> parameters.put("preCarriageModeCode", movement.getModeCode()));
            Optional.ofNullable(cmrDto.getRegulatoryProcedure()).ifPresent(proc -> {
                Optional.ofNullable(proc.getExportCustomsOfficeLocation()).ifPresent(loc -> parameters.put("exportCustomsOfficeId", loc.getId()));
                Optional.ofNullable(proc.getImportCustomsOfficeLocation()).ifPresent(loc -> parameters.put("importCustomsOfficeId", loc.getId()));
            });
            Optional.ofNullable(cmrDto.getSpecifiedTransportMovement()).ifPresent(movement -> parameters.put("specifiedTransportModeCode", movement.getModeCode()));
            Optional.ofNullable(cmrDto.getTransportEquipmentQuantity()).ifPresent(quantity -> parameters.put("transportEquipmentQuantity", quantity));
            Optional.ofNullable(cmrDto.getTransportEvent()).ifPresent(event -> {
                Optional.ofNullable(event.getEstimatedOccurrenceDateTime()).ifPresent(dt -> {
                    parameters.put("transportEventEstDateTime", dt.getValue());
                    parameters.put("transportEventEstDateTimeFormat", dt.getFormatId());
                });
                Optional.ofNullable(event.getOccurrenceLocation()).ifPresent(loc -> {
                    parameters.put("transportEventLocationName", loc.getName());
                    Optional.ofNullable(loc.getPostalAddress()).ifPresent(address -> {
                        parameters.put("transportEventLocationStreet", address.getStreetName());
                        parameters.put("transportEventLocationCity", address.getCityName());
                        parameters.put("transportEventLocationPostcode", address.getPostcode());
                        parameters.put("transportEventLocationCountryCode", address.getCountryCode());
                    });
                });
            });
            Optional.ofNullable(cmrDto.getTransportService()).ifPresent(service -> parameters.put("transportServiceDescription", service.getDescription()));
            Optional.ofNullable(cmrDto.getTransshipmentLocation()).ifPresent(loc -> {
                parameters.put("transshipmentLocationName", loc.getName());
                Optional.ofNullable(loc.getPostalAddress()).ifPresent(address -> {
                    parameters.put("transshipmentLocationStreet", address.getStreetName());
                    parameters.put("transshipmentLocationCity", address.getCityName());
                    parameters.put("transshipmentLocationPostcode", address.getPostcode());
                    parameters.put("transshipmentLocationCountryCode", address.getCountryCode());
                });
            });
            Optional.ofNullable(cmrDto.getTransshipmentPermittedIndicator()).ifPresent(indicator -> parameters.put("transshipmentPermittedIndicator", indicator));

            Optional.ofNullable(cmrDto.getUsedTransportEquipment()).ifPresent(equipmentList -> {
                AtomicInteger i = new AtomicInteger(0);
                equipmentList.forEach(equipment -> {
                    int index = i.getAndIncrement();
                    Optional.ofNullable(equipment.getId()).ifPresent(id -> {
                        parameters.put("usedTransportEquipment_" + index + "_id", id.getValue());
                        parameters.put("usedTransportEquipment_" + index + "_id_scheme", id.getSchemeAgencyId());
                    });
                    Optional.ofNullable(equipment.getRegistrationCountry()).ifPresent(country -> parameters.put("usedTransportEquipment_" + index + "_country", country.getCode()));
                    Optional.ofNullable(equipment.getSequenceNumber()).ifPresent(seq -> parameters.put("usedTransportEquipment_" + index + "_sequence", seq));
                });
            });

            log.info("Populated report parameters.");
            log.info("All parameters." + parameters.toString());
            // Step 4: Fill the report using the parameters and an empty data source for the main report
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