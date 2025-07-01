import {Component, Input} from '@angular/core';
import {IdentifierResponse} from '../core/types';
import {JsonPipe} from '@angular/common';

@Component({
  selector: 'app-identifiers-result',
  imports: [
    JsonPipe
  ],
  templateUrl: './identifiers-result.html',
})
export class IdentifiersResult {
  @Input() searchResults: IdentifierResponse | unknown;
}
