import {Component, Input} from '@angular/core';
import {IdentifierResponse} from '../core/types';
import {DatePipe, JsonPipe} from '@angular/common';
import {euCountries} from '../core/countries';

@Component({
  selector: 'app-identifiers-result',
  imports: [
    DatePipe
  ],
  templateUrl: './identifiers-result.html',
})
export class IdentifiersResult {
  @Input() identifierResponse: IdentifierResponse | null = null;

  getCountryName(isoCode: string): string {
    for (const euCountry of euCountries) {
      if (euCountry.isoCode === isoCode) {
        return euCountry.name;
      }
    }
    return "Unknown Country";
  }
}
