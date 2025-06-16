import { Component } from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {PermissionLevel} from './permission-level.enum';
import {TitleCasePipe} from '@angular/common';

@Component({
  selector: 'app-authorities',
  imports: [
    ReactiveFormsModule,
    TitleCasePipe
  ],
  templateUrl: './authorities.html',
})
export class Authorities {
  permissionLevelKeys = Object.keys(PermissionLevel)
  .filter(key => isNaN(Number(key))) as Array<keyof typeof PermissionLevel>;

  formatPermissionLevel(level: string): string {
    return level.replace(/_/g, ' ');
  }

  registerAuthorityForm = new FormGroup({
    authorityId: new FormControl(''),
    permissionLevel: new FormControl<PermissionLevel | null>(null, Validators.required)
  })

  onRegisterAuthoritySubmit() {
    console.log(this.registerAuthorityForm.value)
  }

  protected readonly PermissionLevel = PermissionLevel;
}
