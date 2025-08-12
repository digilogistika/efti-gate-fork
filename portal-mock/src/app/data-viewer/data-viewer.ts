import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  Measure,
  SupplyChainConsignment,
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

  protected formatAddress(party: TradeParty | undefined | null): string {
    if (!party?.postalAddress) {
      return 'N/A';
    }
    const pa = party.postalAddress;
    const addressParts = [
      pa.buildingNumber?.[0],
      pa.streetName?.[0],
      pa.cityName?.[0],
      pa.postcode?.[0],
      pa.countryCode?.value
    ];
    const formatted = addressParts.filter(p => p).join(', ');
    return formatted || 'N/A';
  }

  protected formatMeasure(measure: Measure | undefined | null): string {
    if (!measure || measure.value === undefined || measure.value === null) {
      return 'N/A';
    }
    return `${measure.value} ${measure.unitCode || ''}`.trim();
  }

  protected formatMeasureArray(measures: Measure[] | undefined | null): string {
    if (!measures || measures.length === 0) return 'N/A';
    return measures.map(m => this.formatMeasure(m)).join('; ');
  }
}
