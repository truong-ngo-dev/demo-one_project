import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ResourceService } from '../../../../core/services/resource.service';

@Component({
  selector: 'app-create-resource-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './create-resource-dialog.html',
  styleUrl: './create-resource-dialog.css',
})
export class CreateResourceDialogComponent {
  private resourceService = inject(ResourceService);
  private dialogRef       = inject(MatDialogRef<CreateResourceDialogComponent>);

  form = new FormGroup({
    name:        new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    description: new FormControl(''),
    serviceName: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  isSubmitting   = signal(false);
  nameError      = signal<string | null>(null);
  generalError   = signal<string | null>(null);

  submit(): void {
    if (this.form.invalid) return;
    this.isSubmitting.set(true);
    this.nameError.set(null);
    this.generalError.set(null);

    const { name, description, serviceName } = this.form.getRawValue();
    this.resourceService.createResource({ name, serviceName, description: description ?? undefined }).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.dialogRef.close(true);
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting.set(false);
        const code = err.error?.error?.code;
        if (err.status === 409 && code === 'RESOURCE_NAME_DUPLICATE') {
          this.nameError.set('Resource name already exists. Choose a different name.');
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
