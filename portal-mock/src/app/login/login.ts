import { Component } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../auth/auth.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './login.html',
})
export class Login {
  public readonly loginForm: FormGroup;
  public loginError: string | null = null;

  constructor(
    private fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router,
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit() {
    if (!this.loginForm.valid) {
      return
    }

    const formData = this.loginForm.value;
    this.authService.login(formData.email, formData.password).subscribe({
        next: () => {
          this.loginError = null; // Clear any previous error
          // Redirect user to identifiers search page
          this.router.navigate(['/identifiers-search']);
        },
        error: (error) => {
          console.error('Login failed:', error);
          this.loginError = error.message || $localize`:A generic error message displayed on the login page when an API call fails without a specific error message.:An unknown error occurred. Please try again.`;
        }
      }
    )
  }
}
