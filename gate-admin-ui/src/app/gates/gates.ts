import { Component } from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';

@Component({
  selector: 'app-gates',
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './gates.html',
})
export class Gates {
  addGateFrom = new FormGroup({
    country: new FormControl(''),
    gateId: new FormControl('')
  })

  onAddGateSubmit() {
    console.log(this.addGateFrom.value)
  }

  deleteGateForm = new FormGroup({
    gateId: new FormControl('')
  })

  onDeleteGateSubmit() {
    console.log(this.deleteGateForm.value)
  }
}
