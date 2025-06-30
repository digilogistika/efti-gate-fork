import { Component } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {HttpClient} from '@angular/common/http';

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './login.html',
})
export class Login {
  public readonly loginForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private readonly http: HttpClient
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit() {
    if (this.loginForm.valid) {
      const formData = this.loginForm.value;
      console.log('Form Data:', formData);

      // Access individual values
      console.log('Email:', formData.email);
      console.log('Password:', formData.password);

      this.http.post("/api/public/authority-user/verify", {
        email: formData.email,
        password: formData.password
      }).subscribe(
        v => {
          console.log(v)
        }
      )
    }
  }
}
