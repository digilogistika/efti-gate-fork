import { Component } from "@angular/core";
import { FormControl, FormGroup, ReactiveFormsModule } from "@angular/forms";
import { GateService } from "./gate.service";
import { Gate } from "./gate.model";
import { NotificationService } from "../notification/notification.service";
import { catchError, of } from "rxjs";

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

  constructor(
    private gateService: GateService,
    private notificationService: NotificationService,
  ) {}

  onRegisterGateSubmit() {
    const gate: Gate = this.registerGateForm.value as Gate;
    this.gateService.registerGate(gate).pipe(
      catchError(error => {
        if (error.status === 409) {
          this.notificationService.showError("Gate already exists");
        } else if (error.status === 400) {
          this.notificationService.showError("Invalid gate data provided");
        } else {
          // Let other errors propagate to the error handler
          throw error;
        }
        return of(null);
      })
    ).subscribe((res) => {
      if (res?.status === 200) {
        this.registerGateForm.reset();
        this.notificationService.showSuccess("Gate registered successfully");
      }
    });
  }

  deleteGateForm = new FormGroup({
    gateId: new FormControl(""),
  });

  onDeleteGateSubmit() {
    const gateId = this.deleteGateForm.value.gateId;
    if (!gateId) {
      this.notificationService.showError("Gate ID is required");
      return;
    }
    this.gateService.deleteGate(gateId).pipe(
      catchError(error => {
        if (error.status === 404) {
          this.notificationService.showError("Gate not found");
        } else if (error.status === 400) {
          this.notificationService.showError("Invalid gate ID format");
        } else {
          // Let other errors propagate to the error handler
          throw error;
        }
        return of(null);
      })
    ).subscribe((res) => {
      if (res?.status === 200) {
        this.deleteGateForm.reset();
        this.notificationService.showSuccess("Gate deleted successfully");
      }
    });
  }
}
