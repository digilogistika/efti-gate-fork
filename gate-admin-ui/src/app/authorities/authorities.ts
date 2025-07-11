import { Component, OnInit, OnDestroy } from "@angular/core";
import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { PermissionLevel } from "./permission-level.enum";
import { CommonModule, TitleCasePipe } from "@angular/common";
import { Authority } from "./authority.model";
import { AuthorityService } from "./authority.service";
import { catchError, of, Subject, takeUntil } from "rxjs";
import { NotificationService } from "../notification/notification.service";
import { Clipboard } from "@angular/cdk/clipboard";
import { GateService } from '../gates/gate.service';

@Component({
  selector: "app-authorities",
  standalone: true,
  imports: [ReactiveFormsModule, TitleCasePipe, CommonModule, FormsModule],
  templateUrl: "./authorities.html",
})
export class Authorities implements OnInit, OnDestroy {
  permissionLevel = PermissionLevel;
  apiKeyResponse: string | undefined = undefined;

  authorityNames: string[] = [];
  filteredAuthorityNames: string[] = [];

  isLoading = true;
  error: string | null = null;

  searchControl = new FormControl('');
  private destroy$ = new Subject<void>();

  permissionLevelKeys = Object.keys(PermissionLevel).filter((key) =>
    isNaN(Number(key)),
  ) as Array<keyof typeof PermissionLevel>;

  registerAuthorityForm = new FormGroup({
    authorityId: new FormControl(""),
    permissionLevel: new FormControl<PermissionLevel | null>(
      null,
      Validators.required,
    ),
  });

  constructor(
    private readonly authorityService: AuthorityService,
    private readonly notificationService: NotificationService,
    private clipboard: Clipboard,
    private readonly gateService: GateService
  ) {}

  ngOnInit(): void {
    this.fetchData();

    this.searchControl.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(value => {
        this.filterAuthorities(value || '');
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
        this.authorityNames = data.authorityNames;
        this.filteredAuthorityNames = data.authorityNames;
        this.isLoading = false;
      },
      error: (err) => {
        this.error = "Failed to load the list of authorities.";
        this.isLoading = false;
        console.error(err);
      }
    });
  }

  private filterAuthorities(searchTerm: string): void {
    const filterValue = searchTerm.toLowerCase().trim();
    if (!filterValue) {
      this.filteredAuthorityNames = this.authorityNames;
      return;
    }

    this.filteredAuthorityNames = this.authorityNames.filter(name =>
      name.toLowerCase().includes(filterValue)
    );
  }

  formatPermissionLevel(level: string): string {
    return level.replace(/_/g, " ");
  }

  onRegisterAuthoritySubmit() {
    const authority: Authority = this.registerAuthorityForm.value as Authority;
    this.authorityService
      .register(authority)
      .pipe(
        catchError((error) => {
          if (error.status === 409) {
            this.notificationService.showError("Authority already exists");
          } else if (error.status === 400) {
            this.notificationService.showError(
              "Invalid authority data provided",
            );
          } else {
            throw error;
          }
          return of(null);
        }),
      )
      .subscribe((res) => {
        if (res?.status === 200) {
          this.registerAuthorityForm.reset();
          this.notificationService.showSuccess(
            "Authority registered successfully",
          );
          this.apiKeyResponse = res.body?.apiKey;
          this.fetchData();
        }
      });
  }

  uploadNewAuthority() {
    this.apiKeyResponse = undefined;
  }

  copyApiKeyToClipboard() {
    if (this.apiKeyResponse) {
      this.clipboard.copy(this.apiKeyResponse);
      this.notificationService.showSuccess("API key copied to clipboard");
    }
  }
}
