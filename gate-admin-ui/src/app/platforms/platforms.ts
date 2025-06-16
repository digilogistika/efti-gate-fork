import { Component } from '@angular/core';
import {FormArray, FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';

@Component({
  selector: 'app-platforms',
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './platforms.html',
})
export class Platforms {
  registerPlatformForm = new FormGroup({
    platformId: new FormControl(''),
    requestBaseUrl: new FormControl(''),
    headers: new FormArray([])
  })

  get headers() {
    return this.registerPlatformForm.get('headers') as FormArray;
  }

  addHeader() {
    const headerGroup = new FormGroup({
      key: new FormControl(''),
      value: new FormControl('')
    });
    this.headers.push(headerGroup);
  }

  removeHeader(index: number) {
    this.headers.removeAt(index);
  }

  onRegisterPlatformSubmit() {
    console.log(this.registerPlatformForm.value);
  }
}
