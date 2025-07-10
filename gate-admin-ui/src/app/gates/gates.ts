import { Component, OnDestroy, OnInit } from "@angular/core";
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from "@angular/forms";
import { GateService } from "./gate.service";
import { Gate } from "./gate.model";
import { NotificationService } from "../notification/notification.service";
import { catchError, of, Subject, takeUntil } from "rxjs";
import { CommonModule } from '@angular/common';
import { NgSelectModule } from '@ng-select/ng-select';

@Component({
  selector: "app-gates",
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule,NgSelectModule],
  templateUrl: "./gates.html",
})
export class Gates implements OnInit, OnDestroy {
  gateIds: string[] = [];
  filteredGateIds: string[] = [];

  isLoading = true;
  error: string | null = null;

  searchControl = new FormControl('');
  private destroy$ = new Subject<void>();

  countries: string[] = [
    'AT', 'BE', 'BG', 'BO', 'CY', 'CZ', 'DE', 'DK', 'EE', 'ES', 'FI',
    'FR', 'GR', 'HR', 'HU', 'IE', 'IT', 'LI', 'LT', 'LU', 'LV', 'MT',
    'NL', 'PL', 'PT', 'RO', 'SE', 'SI', 'SK', 'SY'
  ];

  registerGateForm = new FormGroup({
    country: new FormControl<string | null>(null, [Validators.required]),
    gateId: new FormControl<string | null>("", [Validators.required]),
  });

  constructor(
    private readonly gateService: GateService,
    private readonly notificationService: NotificationService,
  ) {}

  ngOnInit(): void {
    this.fetchData();

    this.searchControl.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(value => {
        this.filterGates(value || '');
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  fetchData(): void {
    this.isLoading = true;
    this.error = null;
    this.gateService.getMetaData().subscribe({
      next: (data) => {
        this.gateIds = data.gateIds;
        this.filteredGateIds = data.gateIds;
        this.isLoading = false;
      },
      error: (err) => {
        this.error = "Failed to load the list of gates.";
        this.isLoading = false;
        console.error(err);
      }
    });
  }

  private filterGates(searchTerm: string): void {
    const filterValue = searchTerm.toLowerCase().trim();
    if (!filterValue) {
      this.filteredGateIds = this.gateIds;
      return;
    }

    this.filteredGateIds = this.gateIds.filter(id =>
      id.toLowerCase().includes(filterValue)
    );
  }

  onRegisterGateSubmit() {
    if (this.registerGateForm.invalid) {
      this.notificationService.showError("Please fill out all required fields.");
      return;
    }
    const gate: Gate = this.registerGateForm.value as Gate;
    this.gateService.registerGate(gate).pipe(
      catchError(error => {
        if (error.status === 409) {
          this.notificationService.showError("Gate already exists");
        } else if (error.status === 400) {
          this.notificationService.showError("Invalid gate data provided");
        } else {
          throw error;
        }
        return of(null);
      })
    ).subscribe((res) => {
      if (res?.status === 200) {
        this.registerGateForm.reset();
        this.notificationService.showSuccess("Gate registered successfully");
        this.fetchData();
      }
    });
  }

  deleteGate(gateId: string): void {
    if (!confirm(`Are you sure you want to delete gate: ${gateId}?`)) {
      return;
    }

    this.gateService.deleteGate(gateId).pipe(
      catchError(error => {
        if (error.status === 404) {
          this.notificationService.showError("Gate not found");
        } else {
          this.notificationService.showError("An unexpected error occurred during deletion.");
        }
        return of(null);
      })
    ).subscribe((res) => {
      if (res?.status === 200) {
        this.notificationService.showSuccess("Gate deleted successfully");
        this.fetchData();
        this.searchControl.reset();
      }
    });
  }
}
