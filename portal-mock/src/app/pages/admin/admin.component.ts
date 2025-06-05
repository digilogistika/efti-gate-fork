import {Component, OnInit} from "@angular/core";
import {CommonModule} from "@angular/common";
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {AuthorityUserModel} from "../../core/models/authority-user.model";
import {CreateUserService} from "../../core/services/create-user.service";

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NgbModule],
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css']
})

export class AdminComponent implements OnInit {
  createUserForm!: FormGroup;
  isSubmitting = false;
  errorMessage = '';
  showSuccessMessage = false;

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly createUserService: CreateUserService,
  ) {}

  ngOnInit(): void {
    this.initForm();
  }

  private initForm(): void {
    this.createUserForm = this.formBuilder.group({
      masterApiKey: ['', [Validators.required]],
      userEmail: ['', [Validators.required, Validators.email]],
      userPassword: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit(): void {
    if (this.createUserForm.invalid) {
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.showSuccessMessage = false;

    const userModel: AuthorityUserModel = this.createUserForm.value;

    this.createUserService.create(userModel)
      .subscribe({
        next: (response) => {
          this.showSuccessMessage = true;
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

  get emailControl() { return this.createUserForm.get('userEmail'); }
  get passwordControl() { return this.createUserForm.get('userPassword'); }
}
