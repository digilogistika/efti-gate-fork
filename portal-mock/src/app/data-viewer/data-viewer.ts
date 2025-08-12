import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  AssociatedTransportEquipment,
  CrossBorderRegulatoryProcedure,
  LogisticsServiceCharge,
  LogisticsTransportEquipment,
  LogisticsTransportMovement,
  ReferencedDocument,
  SupplyChainConsignment,
  TradeParty,
  TransportDangerousGoods,
  TransportEvent
} from '../core/types';

@Component({
  selector: 'app-efti-data-viewer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './data-viewer.html',
})
export class EftiDataViewerComponent {
  @Input() eftiData: SupplyChainConsignment | undefined | null;

  // State management for modals
  protected activeModal: string | null = null;
  protected modalDetailView: 'list' | 'detail' = 'list'; // For drill-down

  // State for master-detail views within modals
  protected selectedEquipment: LogisticsTransportEquipment | null = null;
  protected selectedCarriedEquipment: AssociatedTransportEquipment | null = null; // For drill-down
  protected selectedDocument: ReferencedDocument | null = null;
  protected selectedServiceCharge: LogisticsServiceCharge | null = null;
  protected selectedMovement: LogisticsTransportMovement | null = null;
  protected selectedDangerousGoods: TransportDangerousGoods | null = null;
  protected selectedRegulatoryProcedure: CrossBorderRegulatoryProcedure | null = null;
  protected selectedAssociatedParty: TradeParty | null = null;
  protected selectedEvent: TransportEvent | null = null;
  protected participantTab: string = 'consignor'; // Default tab for the participants modal

  protected openModal(modalName: string): void {
    this.activeModal = modalName;
    this.modalDetailView = 'list'; // Reset to list view
    // Reset selections when opening a new modal
    this.selectedEquipment = null;
    this.selectedCarriedEquipment = null;
    this.selectedDocument = null;
    this.selectedServiceCharge = null;
    this.selectedMovement = null;
    this.selectedDangerousGoods = null;
    this.selectedRegulatoryProcedure = null;
    this.selectedAssociatedParty = null;
    this.selectedEvent = null;
    this.participantTab = 'consignor'; // Reset to the first tab
  }

  protected closeModal(): void {
    this.activeModal = null;
  }

  protected selectEquipment(item: LogisticsTransportEquipment): void {
    this.selectedEquipment = item;
    this.selectedCarriedEquipment = null; // Clear any previous drill-down
    this.modalDetailView = 'detail';
  }

  protected selectCarriedEquipment(item: AssociatedTransportEquipment): void {
    this.selectedCarriedEquipment = item;
  }

  protected goBackToMainEquipment(): void {
    this.selectedCarriedEquipment = null;
  }

  protected goBackToList(): void {
    this.modalDetailView = 'list';
    this.selectedEquipment = null;
    this.selectedDocument = null;
    this.selectedServiceCharge = null;
    this.selectedMovement = null;
  }

  protected selectDocument(item: ReferencedDocument): void {
    this.selectedDocument = item;
    this.modalDetailView = 'detail';
  }

  protected selectServiceCharge(item: LogisticsServiceCharge): void {
    this.selectedServiceCharge = item;
    this.modalDetailView = 'detail';
  }

  protected selectMovement(item: LogisticsTransportMovement): void {
    this.selectedMovement = item;
    this.modalDetailView = 'detail';
  }

  /**
   * NEW: Changes the active tab in the participants modal.
   * @param tabName The name of the tab to switch to.
   */
  protected selectParticipantTab(tabName: string): void {
    this.participantTab = tabName;
  }

  // Helper to format addresses
  protected formatAddress(party: TradeParty | undefined | null): string {
    if (!party?.postalAddress) {
      return 'Address not provided';
    }
    const pa = party.postalAddress;
    const addressParts = [
      pa.streetName?.[0],
      pa.cityName?.[0],
      pa.postcode?.[0],
      pa.countryCode?.value,
    ];
    const formatted = addressParts.filter(p => p).join(', ');
    return formatted || 'Address not provided';
  }

  // Helper to count additional parties
  get otherPartiesCount(): number {
    let count = 0;
    if(this.eftiData?.freightForwarder) count++;
    if(this.eftiData?.connectingCarrier?.length) count += this.eftiData.connectingCarrier.length;
    if(this.eftiData?.associatedParty) count += this.eftiData.associatedParty.length;
    return count;
  }

  // Helper to get all transport movements in one array
  get allMovements(): LogisticsTransportMovement[] {
    if (!this.eftiData) return [];
    return [
      ...(this.eftiData.preCarriageTransportMovement || []),
      ...(this.eftiData.mainCarriageTransportMovement || []),
      ...(this.eftiData.onCarriageTransportMovement || []),
      ...(this.eftiData.specifiedTransportMovement || []),
    ];
  }

  /**
   * NEW: A getter to consolidate all other parties into a single array for easier rendering.
   */
  get allOtherParties(): TradeParty[] {
    if (!this.eftiData) return [];
    const parties: TradeParty[] = [];
    if (this.eftiData.freightForwarder) {
      parties.push(this.eftiData.freightForwarder);
    }
    if (this.eftiData.connectingCarrier) {
      parties.push(...this.eftiData.connectingCarrier);
    }
    if (this.eftiData.associatedParty) {
      parties.push(...this.eftiData.associatedParty);
    }
    return parties;
  }
}
