import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LogisticsTransportMovement, TransportEvent } from '../core/types';
import { DisplayFieldComponent } from './display-field.component';

@Component({
  selector: 'app-transport-movement',
  standalone: true,
  imports: [CommonModule, DisplayFieldComponent],
  templateUrl: './transport-movement.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TransportMovementComponent {
  @Input({ required: true }) movement!: LogisticsTransportMovement;
  @Input({ required: true }) title!: string;

  protected asArray<T>(value: T | T[] | undefined | null): T[] {
    if (value === null || value === undefined) return [];
    return Array.isArray(value) ? value : [value];
  }

  protected get allEvents(): { type: string, event: TransportEvent }[] {
    const events = [];
    if (this.movement.departureEvent) events.push(...this.asArray(this.movement.departureEvent).map(e => ({ type: 'Departure', event: e })));
    if (this.movement.arrivalEvent) events.push(...this.asArray(this.movement.arrivalEvent).map(e => ({ type: 'Arrival', event: e })));
    if (this.movement.loadingEvent) events.push(...this.asArray(this.movement.loadingEvent).map(e => ({ type: 'Loading', event: e })));
    if (this.movement.unloadingEvent) events.push(...this.asArray(this.movement.unloadingEvent).map(e => ({ type: 'Unloading', event: e })));
    if (this.movement.borderCrossingEvent) events.push(...this.asArray(this.movement.borderCrossingEvent).map(e => ({ type: 'Border Crossing', event: e })));
    if (this.movement.callEvent) events.push(...this.asArray(this.movement.callEvent).map(e => ({ type: 'Call', event: e })));
    if (this.movement.event) events.push(...this.asArray(this.movement.event).map(e => ({ type: e.typeCode || 'General', event: e })));
    return events;
  }
}
