import { Component } from "@angular/core";
import {
  FormArray,
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
} from "@angular/forms";
import { Platform } from "./platform.model";
import { catchError, of } from "rxjs";
import { PlatformService } from "./platform.service";
import { NotificationService } from "../notification/notification.service";
import { Clipboard } from "@angular/cdk/clipboard";

@Component({
  selector: "app-platforms",
  imports: [ReactiveFormsModule, FormsModule],
  templateUrl: "./platforms.html",
})
export class Platforms {
  apiKeyResponse: string | undefined = undefined;

  registerPlatformForm = new FormGroup({
    platformId: new FormControl(""),
    requestBaseUrl: new FormControl(""),
    headers: new FormArray([]),
  });

  constructor(
    private readonly platformService: PlatformService,
    private readonly notificationService: NotificationService,
    private clipboard: Clipboard,
  ) {}

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
          this.notificationService.showSuccess(
            "Platform registered successfully",
          );
          this.apiKeyResponse = res.body?.apiKey;
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
