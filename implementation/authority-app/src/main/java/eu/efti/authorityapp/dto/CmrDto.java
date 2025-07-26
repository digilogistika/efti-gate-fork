// file: implementation/authority-app/src/main/java/eu/efti/authorityapp/dto/CmrDto.java
package eu.efti.authorityapp.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlValue;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@XmlRootElement(name = "consignment", namespace = "http://efti.eu/v1/consignment/common")
@XmlAccessorType(XmlAccessType.FIELD)
public class CmrDto {

    // Box 1: Consignor
    @XmlElement(name = "consignor", namespace = "http://efti.eu/v1/consignment/common")
    private Party consignor;

    // Box 2: Consignee
    @XmlElement(name = "consignee", namespace = "http://efti.eu/v1/consignment/common")
    private Party consignee;

    // Box 3: Taking over the Goods
    @XmlElement(name = "carrierAcceptanceLocation", namespace = "http://efti.eu/v1/consignment/common")
    private Location carrierAcceptanceLocation;

    @XmlElement(name = "carrierAcceptanceDateTime", namespace = "http://efti.eu/v1/consignment/common")
    private DateTime carrierAcceptanceDateTime;

    // Box 4: Delivery of the Goods
    @XmlElement(name = "consigneeReceiptLocation", namespace = "http://efti.eu/v1/consignment/common")
    private Location consigneeReceiptLocation;

    // Box 5: Consignor's instructions
    @XmlElement(name = "consignorProvidedInformationText", namespace = "http://efti.eu/v1/consignment/common")
    private String consignorProvidedInformationText;

    // Box 6: Carrier
    @XmlElement(name = "carrier", namespace = "http://efti.eu/v1/consignment/common")
    private Party carrier;

    // Box 6: Carrier License Plate
    @XmlElement(name = "mainCarriageTransportMovement", namespace = "http://efti.eu/v1/consignment/common")
    private MainCarriageTransportMovement mainCarriageTransportMovement;

    // Box 7: Successive Carriers
    @XmlElement(name = "connectingCarrier", namespace = "http://efti.eu/v1/consignment/common")
    private Party connectingCarrier;

    // Box 8: Carrier's reservations and observations
    @XmlElement(name = "information", namespace = "http://efti.eu/v1/consignment/common")
    private String information;

    // Box 9: Documents handed to the carrier
    @XmlElement(name = "associatedDocument", namespace = "http://efti.eu/v1/consignment/common")
    private AssociatedDocument associatedDocument;

    // Box 10-15: Consignment Item Details
    @XmlElement(name = "transportPackage", namespace = "http://efti.eu/v1/consignment/common")
    private TransportPackage transportPackage;

    @XmlElement(name = "natureIdentificationCargo", namespace = "http://efti.eu/v1/consignment/common")
    private NatureIdentificationCargo natureIdentificationCargo;

    @XmlElement(name = "grossWeight", namespace = "http://efti.eu/v1/consignment/common")
    private Measure grossWeight;

    @XmlElement(name = "grossVolume", namespace = "http://efti.eu/v1/consignment/common")
    private Measure grossVolume;

    @XmlElement(name = "netWeight", namespace = "http://efti.eu/v1/consignment/common")
    private Measure netWeight;

    @XmlElement(name = "numberOfPackages", namespace = "http://efti.eu/v1/consignment/common")
    private Integer numberOfPackages;

    @XmlElement(name = "includedConsignmentItem", namespace = "http://efti.eu/v1/consignment/common")
    private IncludedConsignmentItem includedConsignmentItem;

    // Box 16: Special agreements
    @XmlElement(name = "contractTermsText", namespace = "http://efti.eu/v1/consignment/common")
    private String contractTermsText;

    // Box 18: Other useful particulars
    @XmlElement(name = "deliveryInformation", namespace = "http://efti.eu/v1/consignment/common")
    private String deliveryInformation;

    // Box 19: Cash on delivery
    @XmlElement(name = "cODAmount", namespace = "http://efti.eu/v1/consignment/common")
    private Amount cODAmount;

    // Box 21: Established In / Date
    @XmlElement(name = "transportContractDocument", namespace = "http://efti.eu/v1/consignment/common")
    private TransportContractDocument transportContractDocument;

    // --- Other XML Elements ---

    @XmlElement(name = "applicableServiceCharge", namespace = "http://efti.eu/v1/consignment/common")
    private ApplicableServiceCharge applicableServiceCharge;

    @XmlElement(name = "associatedParty", namespace = "http://efti.eu/v1/consignment/common")
    private Party associatedParty;

    @XmlElement(name = "cargoInsuranceInstructions", namespace = "http://efti.eu/v1/consignment/common")
    private String cargoInsuranceInstructions;

    @XmlElement(name = "consignorProvidedBorderClearanceInstructions", namespace = "http://efti.eu/v1/consignment/common")
    private ConsignorProvidedBorderClearanceInstructions consignorProvidedBorderClearanceInstructions;

    @XmlElement(name = "declaredValueForCarriageAmount", namespace = "http://efti.eu/v1/consignment/common")
    private Amount declaredValueForCarriageAmount;

    @XmlElement(name = "deliveryEvent", namespace = "http://efti.eu/v1/consignment/common")
    private DeliveryEvent deliveryEvent;

    @XmlElement(name = "freightForwarder", namespace = "http://efti.eu/v1/consignment/common")
    private Party freightForwarder;

    @XmlElement(name = "logisticsRiskAnalysisResult", namespace = "http://efti.eu/v1/consignment/common")
    private LogisticsRiskAnalysisResult logisticsRiskAnalysisResult;

    @XmlElement(name = "notifiedWasteMaterial", namespace = "http://efti.eu/v1/consignment/common")
    private NotifiedWasteMaterial notifiedWasteMaterial;

    @XmlElement(name = "onCarriageTransportMovement", namespace = "http://efti.eu/v1/consignment/common")
    private TransportMovement onCarriageTransportMovement;

    @XmlElement(name = "paymentArrangementCode", namespace = "http://efti.eu/v1/consignment/common")
    private String paymentArrangementCode;

    @XmlElement(name = "preCarriageTransportMovement", namespace = "http://efti.eu/v1/consignment/common")
    private TransportMovement preCarriageTransportMovement;

    @XmlElement(name = "regulatoryProcedure", namespace = "http://efti.eu/v1/consignment/common")
    private RegulatoryProcedure regulatoryProcedure;

    @XmlElement(name = "specifiedTransportMovement", namespace = "http://efti.eu/v1/consignment/common")
    private TransportMovement specifiedTransportMovement;

    @XmlElement(name = "transportEquipmentQuantity", namespace = "http://efti.eu/v1/consignment/common")
    private Integer transportEquipmentQuantity;

    @XmlElement(name = "transportEvent", namespace = "http://efti.eu/v1/consignment/common")
    private TransportEvent transportEvent;

    @XmlElement(name = "transportService", namespace = "http://efti.eu/v1/consignment/common")
    private TransportService transportService;

    @XmlElement(name = "transshipmentLocation", namespace = "http://efti.eu/v1/consignment/common")
    private TransshipmentLocation transshipmentLocation;

    @XmlElement(name = "transshipmentPermittedIndicator", namespace = "http://efti.eu/v1/consignment/common")
    private Boolean transshipmentPermittedIndicator;

    @XmlElement(name = "usedTransportEquipment", namespace = "http://efti.eu/v1/consignment/common")
    private List<UsedTransportEquipment> usedTransportEquipment;


    // --- Nested classes for JAXB mapping ---

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Party {
        @XmlElement(name = "name", namespace = "http://efti.eu/v1/consignment/common")
        private String name;

        @XmlElement(name = "postalAddress", namespace = "http://efti.eu/v1/consignment/common")
        private PostalAddress postalAddress;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PostalAddress {
        @XmlElement(name = "streetName", namespace = "http://efti.eu/v1/consignment/common")
        private String streetName;

        @XmlElement(name = "cityName", namespace = "http://efti.eu/v1/consignment/common")
        private String cityName;

        @XmlElement(name = "postcode", namespace = "http://efti.eu/v1/consignment/common")
        private String postcode;

        @XmlElement(name = "countryCode", namespace = "http://efti.eu/v1/consignment/common")
        private String countryCode;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Location {
        @XmlElement(name = "name", namespace = "http://efti.eu/v1/consignment/common")
        private String name;

        @XmlElement(name = "postalAddress", namespace = "http://efti.eu/v1/consignment/common")
        private PostalAddress postalAddress;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class MainCarriageTransportMovement {
        @XmlElement(name = "dangerousGoodsIndicator", namespace = "http://efti.eu/v1/consignment/common")
        private Boolean dangerousGoodsIndicator;

        @XmlElement(name = "modeCode", namespace = "http://efti.eu/v1/consignment/common")
        private String modeCode;

        @XmlElement(name = "usedTransportMeans", namespace = "http://efti.eu/v1/consignment/common")
        private UsedTransportMeans usedTransportMeans;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class UsedTransportMeans {
        @XmlElement(name = "id", namespace = "http://efti.eu/v1/consignment/common")
        private IdElement id;

        @XmlElement(name = "registrationCountry", namespace = "http://efti.eu/v1/consignment/common")
        private RegistrationCountry registrationCountry;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class AssociatedDocument {
        @XmlElement(name = "id", namespace = "http://efti.eu/v1/consignment/common")
        private String id;
        @XmlElement(name = "typeCode", namespace = "http://efti.eu/v1/consignment/common")
        private String typeCode;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TransportPackage {
        @XmlElement(name = "itemQuantity", namespace = "http://efti.eu/v1/consignment/common")
        private Integer itemQuantity;
        @XmlElement(name = "typeCode", namespace = "http://efti.eu/v1/consignment/common")
        private String typeCode;
        @XmlElement(name = "typeText", namespace = "http://efti.eu/v1/consignment/common")
        private String typeText;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class NatureIdentificationCargo {
        @XmlElement(name = "identificationText", namespace = "http://efti.eu/v1/consignment/common")
        private String identificationText;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Measure {
        @XmlAttribute(name = "unitId")
        private String unitId;
        @XmlValue
        private BigDecimal value;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Amount {
        @XmlAttribute(name = "currencyId")
        private String currencyId;
        @XmlValue
        private BigDecimal value;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TransportContractDocument {
        @XmlElement(name = "id", namespace = "http://efti.eu/v1/consignment/common")
        private String id;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ApplicableServiceCharge {
        @XmlElement(name = "appliedAmount", namespace = "http://efti.eu/v1/consignment/common")
        private Amount appliedAmount;

        @XmlElement(name = "calculationBasisPrice", namespace = "http://efti.eu/v1/consignment/common")
        private CalculationBasisPrice calculationBasisPrice;

        @XmlElement(name = "id", namespace = "http://efti.eu/v1/consignment/common")
        private String id;

        @XmlElement(name = "payingPartyRoleCode", namespace = "http://efti.eu/v1/consignment/common")
        private String payingPartyRoleCode;

        @XmlElement(name = "paymentArrangementCode", namespace = "http://efti.eu/v1/consignment/common")
        private String paymentArrangementCode;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CalculationBasisPrice {
        @XmlElement(name = "basisQuantity", namespace = "http://efti.eu/v1/consignment/common")
        private Integer basisQuantity;

        @XmlElement(name = "categoryTypeCode", namespace = "http://efti.eu/v1/consignment/common")
        private String categoryTypeCode;

        @XmlElement(name = "unitAmount", namespace = "http://efti.eu/v1/consignment/common")
        private Amount unitAmount;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ConsignorProvidedBorderClearanceInstructions {
        @XmlElement(name = "description", namespace = "http://efti.eu/v1/consignment/common")
        private String description;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DeliveryEvent {
        @XmlElement(name = "actualOccurrenceDateTime", namespace = "http://efti.eu/v1/consignment/common")
        private DateTime actualOccurrenceDateTime;

        @XmlElement(name = "occurrenceLocation", namespace = "http://efti.eu/v1/consignment/common")
        private Location occurrenceLocation;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DateTime {
        @XmlAttribute(name = "formatId")
        private String formatId;
        @XmlValue
        private String value;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class IncludedConsignmentItem {
        @XmlElement(name = "dimensions", namespace = "http://efti.eu/v1/consignment/common")
        private Dimensions dimensions;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Dimensions {
        @XmlElement(name = "description", namespace = "http://efti.eu/v1/consignment/common")
        private String description;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class LogisticsRiskAnalysisResult {
        @XmlElement(name = "consignmentRiskRelatedCode", namespace = "http://efti.eu/v1/consignment/common")
        private String consignmentRiskRelatedCode;

        @XmlElement(name = "description", namespace = "http://efti.eu/v1/consignment/common")
        private String description;

        @XmlElement(name = "levelCode", namespace = "http://efti.eu/v1/consignment/common")
        private String levelCode;

        @XmlElement(name = "screeningMethodCode", namespace = "http://efti.eu/v1/consignment/common")
        private String screeningMethodCode;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class NotifiedWasteMaterial {
        @XmlElement(name = "id", namespace = "http://efti.eu/v1/consignment/common")
        private String id;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TransportMovement {
        @XmlElement(name = "modeCode", namespace = "http://efti.eu/v1/consignment/common")
        private String modeCode;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RegulatoryProcedure {
        @XmlElement(name = "exportCustomsOfficeLocation", namespace = "http://efti.eu/v1/consignment/common")
        private CustomsOfficeLocation exportCustomsOfficeLocation;

        @XmlElement(name = "importCustomsOfficeLocation", namespace = "http://efti.eu/v1/consignment/common")
        private CustomsOfficeLocation importCustomsOfficeLocation;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CustomsOfficeLocation {
        @XmlElement(name = "id", namespace = "http://efti.eu/v1/consignment/common")
        private String id;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TransportEvent {
        @XmlElement(name = "estimatedOccurrenceDateTime", namespace = "http://efti.eu/v1/consignment/common")
        private DateTime estimatedOccurrenceDateTime;

        @XmlElement(name = "occurrenceLocation", namespace = "http://efti.eu/v1/consignment/common")
        private Location occurrenceLocation;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TransportService {
        @XmlElement(name = "description", namespace = "http://efti.eu/v1/consignment/common")
        private String description;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TransshipmentLocation {
        @XmlElement(name = "name", namespace = "http://efti.eu/v1/consignment/common")
        private String name;

        @XmlElement(name = "postalAddress", namespace = "http://efti.eu/v1/consignment/common")
        private PostalAddress postalAddress;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class UsedTransportEquipment {
        @XmlElement(name = "id", namespace = "http://efti.eu/v1/consignment/common")
        private IdElement id;

        @XmlElement(name = "registrationCountry", namespace = "http://efti.eu/v1/consignment/common")
        private RegistrationCountry registrationCountry;

        @XmlElement(name = "sequenceNumber", namespace = "http://efti.eu/v1/consignment/common")
        private Integer sequenceNumber;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class IdElement {
        @XmlAttribute(name = "schemeAgencyId")
        private String schemeAgencyId;
        @XmlValue
        private String value;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RegistrationCountry {
        @XmlElement(name = "code", namespace = "http://efti.eu/v1/consignment/common")
        private String code;
    }
}