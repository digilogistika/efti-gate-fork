import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  Measure,
  PostalAddress,
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

  protected formatPostalAddress(pa: PostalAddress | undefined | null): string {
    if (!pa) {
      return 'N/A';
    }
    const addressParts = [
      pa.buildingNumber,
      pa.streetName?.[0],
      pa.additionalStreetName?.[0],
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
    return `${measure.value} ${measure.unitCode || ''}`.trim();
  }

  protected formatMeasureArray(measures: Measure[] | undefined | null): string {
    if (!measures || measures.length === 0) return 'N/A';
    return measures.map(m => this.formatMeasure(m)).join('; ');
  }
}
