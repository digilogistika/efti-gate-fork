package eu.efti.authorityapp.dto;

import eu.efti.v1.consignment.common.SupplyChainConsignment;
import lombok.Getter;

public record PdfGenerationResult(@Getter byte[] pdfBytes, @Getter SupplyChainConsignment consignment) {}