import { Component } from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {euCountries} from './countries';
import {MatFormField, MatLabel, MatOption, MatSelect} from '@angular/material/select';
import {IdentifierResponse} from './types';
import {HttpClient} from '@angular/common/http';

@Component({
  selector: 'app-identifiers-search',
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MatSelect,
    MatLabel,
    MatFormField,
    MatOption,
  ],
  templateUrl: './identifiers-search.html',
})
export class IdentifiersSearch {
  public readonly identifiersSearchForm: FormGroup;
  protected readonly euCountries;

  protected searchResults: IdentifierResponse | unknown

  constructor(
    private fb: FormBuilder,
    private readonly http: HttpClient
  ) {
    this.identifiersSearchForm = this.fb.group({
      identifier: ['', [Validators.required]],
      modeCode: [''],
      identifierType: [[]],
      registrationCountryCode: [''],
      dangerousGoodsIndicator: [''],
      eftiGateIndicator: [[]]
    });
    this.euCountries = euCountries;
  }

  onSubmit() {
    if (!this.identifiersSearchForm.valid) {
      return
    }
    // request the identifiers endpoint for results
    const formValues = this.identifiersSearchForm.value;

    let identifiersQuery: string = `/api/v1/identifiers/${formValues.identifier}`;
    const queryParams: string[] = [];
    if (formValues.modeCode) queryParams.push(`modeCode=${formValues.modeCode}`);
    if (formValues.identifierType) queryParams.push(`identifierType=${formValues.identifierType}`);
    if (formValues.registrationCountryCode) queryParams.push(`registrationCountryCode=${formValues.registrationCountryCode}`);
    if (formValues.dangerousGoodsIndicator) queryParams.push(`dangerousGoodsIndicator=${formValues.dangerousGoodsIndicator}`);
    if (formValues.eftiGateIndicator) queryParams.push(`eftiGateIndicator=${formValues.eftiGateIndicator}`);
    if (queryParams.length > 0) {
      identifiersQuery += '?' + queryParams.join('&');
    }

    this.http.get(identifiersQuery)
      .subscribe(v => {
        console.log(v)
      })
  }
}
