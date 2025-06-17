import { Component } from '@angular/core';
import {FormArray, FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {Platform} from './platform.model';
import {catchError, of} from 'rxjs';
import {PlatformService} from './platform.service';
import {NotificationService} from '../notification/notification.service';

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

  constructor(
    private readonly platformService: PlatformService,
    private readonly notificationService: NotificationService
  ) {}

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
    const platform: Platform = this.registerPlatformForm.value as Platform
    this.platformService.registerPlatform(platform).pipe(
      catchError(error => {
        if (error.status === 409) {
          this.notificationService.showError("Platform already exists");
        } else if (error.status === 400) {
          this.notificationService.showError("Invalid gate data provided");
        } else {
          throw error;
        }
        return of(null);
      })
    )
  }
}
