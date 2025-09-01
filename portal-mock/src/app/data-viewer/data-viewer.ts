import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SupplyChainConsignment } from '../core/types';
import { PartyDetailsComponent } from "./party-details.component";
import { TransportMovementComponent } from "./transport-movement.component";
import { DisplayFieldComponent } from './display-field.component';

@Component({
  selector: 'app-data-viewer',
  standalone: true,
  imports: [
    CommonModule,
    PartyDetailsComponent,
    TransportMovementComponent,
    DisplayFieldComponent
  ],
  templateUrl: './data-viewer.html',
})
export class DataViewerComponent {
  @Input() eftiData: SupplyChainConsignment | undefined | null;

  protected hasConsignmentOverviewData(): boolean {
    if (!this.eftiData) return false;
    const d = this.eftiData;
    return !!(
      d.paymentArrangementCode ||
      d.codamount ||
      d.declaredValueForCarriageAmount ||
      d.contractTermsText?.length ||
      d.cargoInsuranceInstructions?.length ||
      d.consignorProvidedInformationText?.length ||
      d.information?.length ||
      d.deliveryInformation ||
      d.consignorProvidedBorderClearanceInstructions?.length ||
      d.natureIdentificationCargo ||
      d.grossWeight?.length ||
      d.netWeight?.length ||
      d.grossVolume?.length ||
      d.numberOfPackages != null ||
      d.transportEquipmentQuantity?.length ||
      d.transshipmentPermittedIndicator !== null
    );
  }

  protected hasJourneyDetailsData(): boolean {
    if (!this.eftiData) return false;
    const d = this.eftiData;
    return [
      d.carrierAcceptanceLocation,
      d.consigneeReceiptLocation,
      d.deliveryEvent,
      d.transshipmentLocation?.length,
      d.regulatoryProcedure?.length,
      d.preCarriageTransportMovement?.length,
      d.mainCarriageTransportMovement?.length,
      d.onCarriageTransportMovement?.length,
      d.specifiedTransportMovement?.length,
    ].some(Boolean) || !!this.eftiData?.transportEvent?.length;
  }
}
