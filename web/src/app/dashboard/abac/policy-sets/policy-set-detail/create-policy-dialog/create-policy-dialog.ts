import { Component, Inject, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { PolicyService, CombineAlgorithmName } from '../../../../../core/services/policy.service';

@Component({
  selector: 'app-create-policy-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
  ],
  templateUrl: './create-policy-dialog.html',
  styleUrl: './create-policy-dialog.css',
})
export class CreatePolicyDialogComponent {
  private policyService = inject(PolicyService);
  private dialogRef     = inject(MatDialogRef<CreatePolicyDialogComponent>);

  constructor(@Inject(MAT_DIALOG_DATA) public data: { policySetId: number }) {}

  readonly algorithms: CombineAlgorithmName[] = [
    'DENY_OVERRIDES', 'PERMIT_OVERRIDES', 'DENY_UNLESS_PERMIT',
    'PERMIT_UNLESS_DENY', 'FIRST_APPLICABLE', 'ONLY_ONE_APPLICABLE',
  ];

  form = new FormGroup({
    name:             new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    combineAlgorithm: new FormControl<CombineAlgorithmName>('DENY_UNLESS_PERMIT', { nonNullable: true, validators: [Validators.required] }),
    targetExpression: new FormControl(''),
  });

  isSubmitting = signal(false);
  spelError    = signal<string | null>(null);
  generalError = signal<string | null>(null);

  submit(): void {
    if (this.form.invalid) return;
    this.isSubmitting.set(true);
    this.spelError.set(null);
    this.generalError.set(null);

    const { name, combineAlgorithm, targetExpression } = this.form.getRawValue();
    this.policyService.createPolicy({
      policySetId: this.data.policySetId,
      name,
      combineAlgorithm,
      targetExpression: targetExpression || undefined,
    }).subscribe({
      next: () => { this.isSubmitting.set(false); this.dialogRef.close(true); },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting.set(false);
        const code = err.error?.error?.code;
        if (err.status === 400 && code === '30011') {
          this.spelError.set('Invalid SpEL expression syntax.');
        } else {
          this.generalError.set('An unexpected error occurred. Please try again.');
        }
      },
    });
  }

  cancel(): void { this.dialogRef.close(false); }
}
