import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../auth/auth.service';
import { Router } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,
    TranslatePipe
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
    protected readonly translate: TranslateService
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit() {
    if (!this.loginForm.valid) {
      return;
    }

    const formData = this.loginForm.value;
    this.authService.login(formData.email, formData.password).subscribe({
      next: () => {
        this.loginError = null;
        this.router.navigate(['/identifiers-search']);
      },
      error: (error) => {
        console.error('Login failed:', error);
        this.loginError = error.message ?? this.translate.instant('login.unknownError');
      }
    });
  }
}
