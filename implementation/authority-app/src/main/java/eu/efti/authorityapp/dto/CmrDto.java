// file: implementation/authority-app/src/main/java/eu/efti/authorityapp/dto/CmrDto.java
package eu.efti.authorityapp.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@XmlRootElement(name = "consignment", namespace = "http://efti.eu/v1/consignment/common")
@XmlAccessorType(XmlAccessType.FIELD)
public class CmrDto {

    @XmlElement(name = "consignor", namespace = "http://efti.eu/v1/consignment/common")
    private Consignor consignor;

    @XmlElement(name = "consignee", namespace = "http://efti.eu/v1/consignment/common")
    private Consignee consignee;

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Consignor {
        @XmlElement(name = "name", namespace = "http://efti.eu/v1/consignment/common")
        private String name;

        @XmlElement(name = "postalAddress", namespace = "http://efti.eu/v1/consignment/common")
        private PostalAddress postalAddress;
    }

    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Consignee {
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
}