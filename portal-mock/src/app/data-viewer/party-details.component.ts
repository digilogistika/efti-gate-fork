import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TradeParty } from '../core/types';
import { DisplayFieldComponent } from './display-field.component';

@Component({
  selector: 'app-party-details',
  standalone: true,
  imports: [CommonModule, DisplayFieldComponent],
  templateUrl: './party-details.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PartyDetailsComponent {
  @Input({ required: true }) party!: TradeParty;
  @Input({ required: true }) title!: string;
}
