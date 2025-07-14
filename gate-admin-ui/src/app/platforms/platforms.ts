import { Component, inject, OnDestroy, OnInit } from "@angular/core";
import {
  FormArray,
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
} from "@angular/forms";
import { Platform } from "./platform.model";
import { catchError, of, Subject, takeUntil } from "rxjs";
import { PlatformService } from "./platform.service";
import { NotificationService } from "../notification/notification.service";
import { Clipboard } from "@angular/cdk/clipboard";
import { GateService } from '../gates/gate.service';

@Component({
  selector: "app-platforms",
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule],
  templateUrl: "./platforms.html",
})
export class Platforms implements OnInit, OnDestroy {
  apiKeyResponse: string | undefined = undefined;

  platformIds: string[] = [];
  filteredPlatformIds: string[] = [];

  isLoading = true;
  error: string | null = null;

  searchControl = new FormControl('');
  private destroy$ = new Subject<void>();

  registerPlatformForm = new FormGroup({
    platformId: new FormControl(""),
    requestBaseUrl: new FormControl(""),
    headers: new FormArray([]),
  });

  private readonly gateService = inject(GateService);
  constructor(
    private readonly platformService: PlatformService,
    private readonly notificationService: NotificationService,
    private clipboard: Clipboard,
  ) {}

  ngOnInit(): void {
    this.fetchData();
    this.searchControl.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(value => {
        this.filterPlatforms(value || '');
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
        this.platformIds = data.platformIds;
        this.filteredPlatformIds = data.platformIds;
        this.isLoading = false;
      },
      error: (err) => {
        this.error = "Failed to load the list of platforms.";
        this.isLoading = false;
        console.error(err);
      }
    });
  }

  private filterPlatforms(searchTerm: string): void {
    const filterValue = searchTerm.toLowerCase().trim();
    if (!filterValue) {
      this.filteredPlatformIds = this.platformIds;
      return;
    }

    this.filteredPlatformIds = this.platformIds.filter(id =>
      id.toLowerCase().includes(filterValue)
    );
  }

  get headers() {
    return this.registerPlatformForm.get("headers") as FormArray;
  }

  addHeader() {
    const headerGroup = new FormGroup({
      key: new FormControl(""),
      value: new FormControl(""),
    });
    this.headers.push(headerGroup);
  }

  removeHeader(index: number) {
    this.headers.removeAt(index);
  }

  onRegisterPlatformSubmit() {
    const platform: Platform = this.registerPlatformForm.value as Platform;
    this.platformService
      .registerPlatform(platform)
      .pipe(
        catchError((error) => {
          if (error.status === 409) {
            this.notificationService.showError("Platform already exists");
          } else if (error.status === 400) {
            this.notificationService.showError(
              "Invalid platform data provided",
            );
          } else {
            throw error;
          }
          return of(null);
        }),
      )
      .subscribe((res) => {
        if (res?.status === 200) {
          this.registerPlatformForm.reset();
          this.headers.clear();
          this.notificationService.showSuccess(
            "Platform registered successfully",
          );
          this.apiKeyResponse = res.body?.apiKey;
          this.fetchData();
        }
      });
  }

  uploadNewPlatform() {
    this.apiKeyResponse = undefined;
  }

  copyApiKeyToClipboard() {
    if (this.apiKeyResponse) {
      this.clipboard.copy(this.apiKeyResponse);
      this.notificationService.showSuccess("API key copied to clipboard");
    }
  }
}
