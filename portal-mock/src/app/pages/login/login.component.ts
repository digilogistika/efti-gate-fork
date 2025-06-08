import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { LoginService } from '../../core/services/login.service';
import { LocalStorageService } from '../../core/services/local-storage.service';
import { AuthorityUserModel } from "../../core/models/authority-user.model";
import {TranslateModule} from "@ngx-translate/core";

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NgbModule, TranslateModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  isSubmitting = false;
  errorMessage = '';

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly loginService: LoginService,
    private readonly localStorageService: LocalStorageService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
  }

  private initForm(): void {
    this.loginForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';

    const credentials: AuthorityUserModel = this.loginForm.value;

    this.loginService.login(credentials)
      .subscribe({
        next: (response) => {
          this.localStorageService.saveAuthToken(response.token);
          this.router.navigate(['/identifiers']).then(() => {
            location.reload();
          });
        },
        error: (error) => {
          this.isSubmitting = false;
          this.errorMessage = error.error?.message ?? 'Invalid credentials. Please try again.';
        },
        complete: () => {
          this.isSubmitting = false;
        }
      });
  }

  get emailControl() { return this.loginForm.get('email'); }
  get passwordControl() { return this.loginForm.get('password'); }
}
