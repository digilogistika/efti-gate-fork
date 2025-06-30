import { Component } from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {euCountries} from './countries';
import {MatFormField, MatLabel, MatOption, MatSelect} from '@angular/material/select';

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

  constructor(
    private fb: FormBuilder,
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
    console.log(this.identifiersSearchForm.value)
  }
}
