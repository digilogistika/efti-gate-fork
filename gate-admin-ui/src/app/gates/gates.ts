import { Component } from "@angular/core";
import { FormControl, FormGroup, ReactiveFormsModule } from "@angular/forms";
import { GateService } from "./gate.service";
import { Gate } from "./gate.model";

@Component({
  selector: "app-gates",
  imports: [ReactiveFormsModule],
  templateUrl: "./gates.html",
})
export class Gates {
  registerGateForm = new FormGroup<{
    country: FormControl<string | null>;
    gateId: FormControl<string | null>;
  }>({
    country: new FormControl(""),
    gateId: new FormControl(""),
  });

  constructor(private gateService: GateService) {}

  onRegisterGateSubmit() {
    const gate: Gate = this.registerGateForm.value as Gate;
    this.gateService.registerGate(gate).subscribe((res) => {
      console.log(res.body);
      if (res.status === 200) {
        this.registerGateForm.reset();
      } else if (res.status === 409) {
        console.error("Bad request");
      } else {
        console.error("Unexpected error");
      }
    });
  }

  deleteGateForm = new FormGroup({
    gateId: new FormControl(""),
  });

  onDeleteGateSubmit() {
    const gateId = this.deleteGateForm.value.gateId;
    if (!gateId) {
      console.error("Gate ID is required");
      return;
    }
    this.gateService.deleteGate(gateId).subscribe((res) => {
      console.log(res.body);
      if (res.status === 200) {
        this.deleteGateForm.reset();
      } else if (res.status === 404) {
        console.error("Gate not found");
      } else {
        console.error("Unexpected error");
      }
    });
  }
}
