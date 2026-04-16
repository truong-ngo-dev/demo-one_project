import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { PolicyService, CombineAlgorithmName, ScopeType } from '../../../../core/services/policy.service';

@Component({
  selector: 'app-create-policy-set-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
  ],
  templateUrl: './create-policy-set-dialog.html',
  styleUrl: './create-policy-set-dialog.css',
})
export class CreatePolicySetDialogComponent {
  private policyService = inject(PolicyService);
  private dialogRef     = inject(MatDialogRef<CreatePolicySetDialogComponent>);

  readonly algorithms: CombineAlgorithmName[] = [
    'DENY_OVERRIDES', 'PERMIT_OVERRIDES', 'DENY_UNLESS_PERMIT',
    'PERMIT_UNLESS_DENY', 'FIRST_APPLICABLE', 'ONLY_ONE_APPLICABLE',
  ];

  readonly scopes: ScopeType[] = ['OPERATOR', 'TENANT'];

  form = new FormGroup({
    name:             new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    scope:            new FormControl<ScopeType>('OPERATOR', { nonNullable: true, validators: [Validators.required] }),
    combineAlgorithm: new FormControl<CombineAlgorithmName>('DENY_OVERRIDES', { nonNullable: true, validators: [Validators.required] }),
    isRoot:           new FormControl(false, { nonNullable: true }),
  });

  isSubmitting = signal(false);
  nameError    = signal<string | null>(null);
  generalError = signal<string | null>(null);

  submit(): void {
    if (this.form.invalid) return;
    this.isSubmitting.set(true);
    this.nameError.set(null);
    this.generalError.set(null);

    const { name, scope, combineAlgorithm, isRoot } = this.form.getRawValue();
    this.policyService.createPolicySet({ name, scope, combineAlgorithm, isRoot }).subscribe({
      next: () => { this.isSubmitting.set(false); this.dialogRef.close(true); },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting.set(false);
        const code = err.error?.error?.code;
        if (err.status === 409 && code === '30008') {
          this.nameError.set('Policy set name already exists.');
        } else {
          this.generalError.set('An unexpected error occurred. Please try again.');
        }
      },
    });
  }

  cancel(): void { this.dialogRef.close(false); }
}
