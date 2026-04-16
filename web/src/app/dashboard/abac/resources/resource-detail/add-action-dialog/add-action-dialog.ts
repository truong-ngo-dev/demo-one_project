import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ResourceService } from '../../../../../core/services/resource.service';

@Component({
  selector: 'app-add-action-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './add-action-dialog.html',
  styleUrl: './add-action-dialog.css',
})
export class AddActionDialogComponent {
  private resourceService = inject(ResourceService);
  private dialogRef       = inject(MatDialogRef<AddActionDialogComponent>);
  private data: { resourceId: number } = inject(MAT_DIALOG_DATA);

  form = new FormGroup({
    name:        new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    description: new FormControl(''),
    isStandard:  new FormControl(false, { nonNullable: true }),
  });

  isSubmitting = signal(false);
  nameError    = signal<string | null>(null);
  generalError = signal<string | null>(null);

  submit(): void {
    if (this.form.invalid) return;
    this.isSubmitting.set(true);
    this.nameError.set(null);
    this.generalError.set(null);

    const { name, description, isStandard } = this.form.getRawValue();
    this.resourceService.addAction(this.data.resourceId, {
      name, isStandard, description: description ?? undefined,
    }).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.dialogRef.close(true);
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting.set(false);
        const code = err.error?.error?.code;
        if (err.status === 409 && code === 'ACTION_NAME_DUPLICATE') {
          this.nameError.set('Action name already exists in this resource.');
        } else {
          this.generalError.set('An unexpected error occurred. Please try again.');
        }
      },
    });
  }

  cancel(): void {
    this.dialogRef.close(false);
  }
}
