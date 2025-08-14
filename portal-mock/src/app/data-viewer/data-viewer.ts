import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  Amount,
  LogisticsTransportEquipment,
  Measure,
  PostalAddress,
  SupplyChainConsignment,
  SupplyChainConsignmentItem,
  TradeParty
} from '../core/types';

@Component({
  selector: 'app-efti-data-viewer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './data-viewer.html',
})
export class EftiDataViewerComponent {
  @Input() eftiData: SupplyChainConsignment | undefined | null;

  protected hasConsignmentOverviewData(): boolean {
    if (!this.eftiData) return false;
    const d = this.eftiData;
    return !!(
      d.paymentArrangementCode ||
      !!d.codamount ||
      !!d.declaredValueForCarriageAmount ||
      (d.contractTermsText && d.contractTermsText.length > 0) ||
      (d.cargoInsuranceInstructions && d.cargoInsuranceInstructions.length > 0) ||
      (d.consignorProvidedInformationText && d.consignorProvidedInformationText.length > 0) ||
      !!d.natureIdentificationCargo ||
      (d.grossWeight && d.grossWeight.length > 0) ||
      (d.netWeight && d.netWeight.length > 0) ||
      (d.grossVolume && d.grossVolume.length > 0) ||
      d.numberOfPackages != null ||
      (d.transportEquipmentQuantity && d.transportEquipmentQuantity.length > 0) ||
      d.transshipmentPermittedIndicator !== null
    );
  }

  protected hasParticipantsData(): boolean {
    if (!this.eftiData) return false;
    const d = this.eftiData;
    return !!(!!d.consignor || !!d.consignee || !!d.carrier || (d.connectingCarrier && d.connectingCarrier.length > 0) || !!d.freightForwarder || (d.associatedParty && d.associatedParty.length > 0));
  }

  protected hasDocumentsData(): boolean {
    if (!this.eftiData) return false;
    const d = this.eftiData;
    return !!(!!d.transportContractDocument || (d.associatedDocument && d.associatedDocument.length > 0));
  }

  protected hasJourneyDetailsData(): boolean {
    if (!this.eftiData) return false;
    const d = this.eftiData;
    const conditions = [
      !!d.carrierAcceptanceLocation,
      !!d.consigneeReceiptLocation,
      !!d.deliveryEvent,
      !!(d.transshipmentLocation && d.transshipmentLocation.length > 0),
      !!(d.regulatoryProcedure && d.regulatoryProcedure.length > 0),
      !!(d.preCarriageTransportMovement && d.preCarriageTransportMovement.length > 0),
      !!(d.mainCarriageTransportMovement && d.mainCarriageTransportMovement.length > 0),
      !!(d.onCarriageTransportMovement && d.onCarriageTransportMovement.length > 0)
    ];
    return conditions.some(c => c);
  }

  protected hasServiceChargesData(): boolean {
    if (!this.eftiData) return false;
    return !!(this.eftiData.applicableServiceCharge && this.eftiData.applicableServiceCharge.length > 0);
  }

  protected hasDangerousGoodsData(): boolean {
    if (!this.eftiData) return false;
    return !!this.eftiData.dangerousGoods;
  }

  protected hasTransportEquipmentData(): boolean {
    if (!this.eftiData) return false;
    return !!(this.eftiData.usedTransportEquipment && this.eftiData.usedTransportEquipment.length > 0);
  }

  protected hasLogisticsAndSecurityData(): boolean {
    if (!this.eftiData) return false;
    return !!(this.eftiData.logisticsRiskAnalysisResult && this.eftiData.logisticsRiskAnalysisResult.length > 0);
  }

  protected formatPostalAddress(pa: PostalAddress | undefined | null): string {
    if (!pa) {
      return 'N/A';
    }
    const addressParts = [
      pa.buildingNumber,
      pa.streetName?.[0],
      pa.additionalStreetName,
      pa.cityName?.[0],
      pa.postcode?.[0],
      pa.countrySubDivisionName?.[0],
      pa.countryCode
    ];
    const formatted = addressParts.filter(p => p).join(', ');
    return formatted || 'N/A';
  }

  protected formatAddress(party: TradeParty | undefined | null): string {
    return this.formatPostalAddress(party?.postalAddress);
  }

  protected formatMeasure(measure: Measure | undefined | null): string {
    if (!measure || measure.value === undefined || measure.value === null) {
      return 'N/A';
    }
    return `${measure.value} ${measure.unitId || ''}`.trim();
  }

  protected formatMeasureArray(measures: Measure[] | undefined | null): string {
    if (!measures || measures.length === 0) return 'N/A';
    return measures.map(m => this.formatMeasure(m)).join('; ');
  }

  protected formatAmount(amount: Amount | undefined | null): string {
    if (!amount || amount.value === undefined || amount.value === null) {
      return 'N/A';
    }
    return `${amount.value.toFixed(2)} ${amount.currencyId || ''}`.trim();
  }

  protected isItemAssociatedWithEquipment(item: SupplyChainConsignmentItem, equip: LogisticsTransportEquipment): boolean {
    if (!item.associatedTransportEquipment || !equip.id?.value) {
      return false;
    }
    return item.associatedTransportEquipment.some(assoc => assoc.id?.value === equip.id?.value);
  }
}
