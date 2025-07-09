import { Component } from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {HttpClient} from '@angular/common/http';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
  selector: 'app-admin',
  imports: [
    FormsModule,
    ReactiveFormsModule,
    TranslatePipe
  ],
  templateUrl: './admin.html',
})
export class Admin {
  public readonly createAuthorityUserForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private readonly http: HttpClient
  ) {
    this.createAuthorityUserForm = this.fb.group({
      'admin-api-key': ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit() {
    if (!this.createAuthorityUserForm.valid) {
      return
    }

    const formData = this.createAuthorityUserForm.value;
    this.http.post("/api/admin/authority-user/create", {
      email: formData.email,
      password: formData.password
    }, {
      headers: {
        "X-API-Key": formData["admin-api-key"]
      }
    }).subscribe(
      v => {
        console.log(v)
      }
    )
  }
}
