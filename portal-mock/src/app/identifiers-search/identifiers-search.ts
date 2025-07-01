import { Component } from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {euCountries} from '../core/countries';
import {MatFormField, MatLabel, MatOption, MatSelect} from '@angular/material/select';
import {IdentifierResponse} from '../core/types';
import {HttpClient} from '@angular/common/http';
import {IdentifiersResult} from '../identifiers-result/identifiers-result';

@Component({
  selector: 'app-identifiers-search',
  imports: [
    FormsModule,
    ReactiveFormsModule,
    MatSelect,
    MatLabel,
    MatFormField,
    MatOption,
    IdentifiersResult,
  ],
  templateUrl: './identifiers-search.html',
})
export class IdentifiersSearch {
  public readonly identifiersSearchForm: FormGroup;
  protected readonly euCountries;

  protected identifierResponse: IdentifierResponse | null = null;
  protected isLoading: boolean = false;
  private readonly selectedIdentifierTypes = new Set<string>();
  private readonly selectedCountries = new Set<string>();

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
    if (this.selectedIdentifierTypes.size > 0) queryParams.push(`identifierType=${Array.from(this.selectedIdentifierTypes).join(",")}`);
    if (formValues.registrationCountryCode) queryParams.push(`registrationCountryCode=${formValues.registrationCountryCode}`);
    if (formValues.dangerousGoodsIndicator) queryParams.push(`dangerousGoodsIndicator=${formValues.dangerousGoodsIndicator}`);
    if (this.selectedCountries.size > 0) queryParams.push(`eftiGateIndicator=${Array.from(this.selectedCountries).join(",")}`);
    if (queryParams.length > 0) {
      identifiersQuery += '?' + queryParams.join('&');
    }

    this.isLoading = true;
    this.http.get<IdentifierResponse>(identifiersQuery)
      .subscribe(v => {
        this.identifierResponse = v;
        this.isLoading = false;
      })
  }

  onIdentifierTypeChange(type: string, event: any) {
    // Handle identifier type checkbox changes
    if (event.target.checked) {
      this.selectedIdentifierTypes.add(type);
    } else {
      this.selectedIdentifierTypes.delete(type);
    }
  }

  onCountryChange(countryCode: string, event: any) {
    // Handle country selection changes
    if (event.target.checked) {
      this.selectedCountries.add(countryCode);
    } else {
      this.selectedCountries.delete(countryCode);
    }
  }
}
