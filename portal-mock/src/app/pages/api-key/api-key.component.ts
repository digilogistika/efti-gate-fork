import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import { ApiKeyService } from '../../core/services/api-key.service';
import {NgClass, NgIf} from "@angular/common";
import {TranslateModule} from "@ngx-translate/core";

@Component({
  selector: 'app-api-key',
  templateUrl: './api-key.component.html',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    NgIf,
    TranslateModule,
    NgClass
  ],
  styleUrls: ['./api-key.component.css']
})
export class ApiKeyComponent implements OnInit {
  apiKeyForm: FormGroup;
  hasApiKey = false;
  currentApiKey = '';
  showApiKey = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly apiKeyService: ApiKeyService
  ) {
    this.apiKeyForm = this.fb.group({
      apiKey: ['', [Validators.required, Validators.minLength(1)]]
    });
  }

  ngOnInit(): void {
    this.apiKeyService.apiKey$.subscribe(apiKey => {
      this.hasApiKey = apiKey.length > 0;
      this.currentApiKey = apiKey;
      if (apiKey) {
        this.apiKeyForm.patchValue({ apiKey: apiKey });
      }
    });
  }

  onSubmit(): void {
    if (this.apiKeyForm.valid) {
      const apiKey = this.apiKeyForm.get('apiKey')?.value;
      this.apiKeyService.setApiKey(apiKey);
      alert('API Key saved successfully!');
    }
  }

  clearApiKey(): void {
    this.apiKeyService.clearApiKey();
    this.apiKeyForm.reset();
    this.showApiKey = false;
    alert('API Key cleared successfully!');
  }

  toggleVisibility(): void {
    this.showApiKey = !this.showApiKey;
    const inputElement = document.getElementById('apiKey') as HTMLInputElement;
    if (inputElement) {
      inputElement.type = this.showApiKey ? 'text' : 'password';
    }
  }
}
