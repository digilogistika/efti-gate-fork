// file: implementation/authority-app/src/main/java/eu/efti/authorityapp/dto/CmrDto.java
package eu.efti.authorityapp.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@XmlRootElement(name = "consignment", namespace = "http://efti.eu/v1/consignment/common")
@XmlAccessorType(XmlAccessType.FIELD)
public class CmrDto {

    // Box 1: Corresponds to "Sender" in the report
    @XmlElement(name = "consignor", namespace = "http://efti.eu/v1/consignment/common")
    private Party consignor;

    // Box 2: Consignee
    @XmlElement(name = "consignee", namespace = "http://efti.eu/v1/consignment/common")
    private Party consignee;

    // Box 3: Taking over the Goods
    @XmlElement(name = "carrierAcceptanceLocation", namespace = "http://efti.eu/v1/consignment/common")
    private Location carrierAcceptanceLocation;

    @XmlElement(name = "carrierAcceptanceDateTime", namespace = "http://efti.eu/v1/consignment/common")
    private String carrierAcceptanceDateTime;

    // Box 4: Delivery of the Goods
    @XmlElement(name = "consigneeReceiptLocation", namespace = "http://efti.eu/v1/consignment/common")
    private Location consigneeReceiptLocation;

    // Box 5: Sender's instructions
    @XmlElement(name = "consignorProvidedInformationText", namespace = "http://efti.eu/v1/consignment/common")
    private String sendersInstructions;

    // Box 6: Carrier
    @XmlElement(name = "carrier", namespace = "http://efti.eu/v1/consignment/common")
    private Party carrier;

    // Box 6: Carrier License Plate
    @XmlElement(name = "mainCarriageTransportMovement", namespace = "http://efti.eu/v1/consignment/common")
    private MainCarriageTransportMovement mainCarriageTransportMovement;

    // Box 7: Successive Carriers
    @XmlElement(name = "connectingCarrier", namespace = "http://efti.eu/v1/consignment/common")
    private Party successiveCarrier;

    // Box 8: Carrier's reservations and observations
    @XmlElement(name = "information", namespace = "http://efti.eu/v1/consignment/common")
    private String carrierReservationsObservations;

    // Box 9: Documents handed to the carrier
    @XmlElement(name = "associatedDocument", namespace = "http://efti.eu/v1/consignment/common")
    private AssociatedDocument documentsRemarks;

    // Box 10-15: Consignment Item Details
    @XmlElement(name = "transportPackage", namespace = "http://efti.eu/v1/consignment/common")
    private List<TransportPackage> transportPackages;

    @XmlElement(name = "natureIdentificationCargo", namespace = "http://efti.eu/v1/consignment/common")
    private NatureIdentificationCargo natureIdentificationCargo;

    @XmlElement(name = "grossWeight", namespace = "http://efti.eu/v1/consignment/common")
    private Measure grossWeight;

    @XmlElement(name = "grossVolume", namespace = "http://efti.eu/v1/consignment/common")
    private Measure grossVolume;

    // Box 16: Special agreements
    @XmlElement(name = "contractTermsText", namespace = "http://efti.eu/v1/consignment/common")
    private String customSpecialAgreement;

    // Box 18: Other useful particulars
    @XmlElement(name = "deliveryInformation", namespace = "http://efti.eu/v1/consignment/common")
    private String customParticulars;

    // Box 19: Cash on delivery
    @XmlElement(name = "cODAmount", namespace = "http://efti.eu/v1/consignment/common")
    private Amount customCashOnDelivery;

    // Box 21: Established In / Date
    @XmlElement(name = "transportContractDocument", namespace = "http://efti.eu/v1/consignment/common")
    private TransportContractDocument transportContractDocument;


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
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class MainCarriageTransportMovement {
        @XmlElement(name = "usedTransportMeans", namespace = "http://efti.eu/v1/consignment/common")
        private UsedTransportMeans usedTransportMeans;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class UsedTransportMeans {
        @XmlElement(name = "id", namespace = "http://efti.eu/v1/consignment/common")
        private String id;
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
        @jakarta.xml.bind.annotation.XmlAttribute(name = "unitId")
        private String unitId;
        @jakarta.xml.bind.annotation.XmlValue
        private java.math.BigDecimal value;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Amount {
        @jakarta.xml.bind.annotation.XmlAttribute(name = "currencyId")
        private String currencyId;
        @jakarta.xml.bind.annotation.XmlValue
        private java.math.BigDecimal value;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TransportContractDocument {
        @XmlElement(name = "id", namespace = "http://efti.eu/v1/consignment/common")
        private String id;
    }
}