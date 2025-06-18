import { Component } from "@angular/core";
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
import { catchError, of } from "rxjs";
import { NotificationService } from "../notification/notification.service";
import { Clipboard } from "@angular/cdk/clipboard";

@Component({
  selector: "app-authorities",
  imports: [ReactiveFormsModule, TitleCasePipe, CommonModule, FormsModule],
  templateUrl: "./authorities.html",
})
export class Authorities {
  permissionLevel = PermissionLevel;
  apiKeyResponse: string | undefined = undefined;

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
  ) {}

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
