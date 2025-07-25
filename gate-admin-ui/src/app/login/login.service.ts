import {Component, inject, OnDestroy} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {CommonModule} from '@angular/common';
import {AuthService} from '../authentication/auth.service';
import {Subject} from 'rxjs';
import {finalize, takeUntil} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
})
export class LoginComponent implements OnDestroy {
  apiKeyValue = '';
  isLoading = false;
  errorMessage: string | null = null;
  version: string = ""

  private authService = inject(AuthService);
  private router = inject(Router);
  private destroy$ = new Subject<void>();

  constructor(private http: HttpClient) {
    http.get<any>("/v3/api-docs").subscribe(apiDocs => {
      this.version = apiDocs.info.version
    })
  }

  onSubmit(): void {
    if (!this.apiKeyValue.trim() || this.isLoading) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;

    this.authService.login(this.apiKeyValue)
      .pipe(
        finalize(() => this.isLoading = false),
        takeUntil(this.destroy$)
      )
      .subscribe(success => {
        if (success) {
          this.router.navigate(['/']);
        } else {
          this.errorMessage = 'Invalid API Key. Please check the key and try again.';
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
