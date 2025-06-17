import { Component } from "@angular/core";
import { FormControl, FormGroup, ReactiveFormsModule } from "@angular/forms";

@Component({
  selector: "app-gates",
  imports: [ReactiveFormsModule],
  templateUrl: "./gates.html",
})
export class Gates {
  registerGateFrom = new FormGroup({
    country: new FormControl(""),
    gateId: new FormControl(""),
  });

  onRegisterGateSubmit() {}

  deleteGateForm = new FormGroup({
    gateId: new FormControl(""),
  });

  onDeleteGateSubmit() {
    console.log(this.deleteGateForm.value);
  }
}
