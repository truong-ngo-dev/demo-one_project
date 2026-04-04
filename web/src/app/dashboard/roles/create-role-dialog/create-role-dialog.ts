import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { HttpErrorResponse } from '@angular/common/http';
import { RoleService } from '../../../core/services/role.service';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatIcon } from '@angular/material/icon';

@Component({
  selector: 'app-create-role-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinner,
    MatIcon,
  ],
  templateUrl: './create-role-dialog.html',
  styleUrl: './create-role-dialog.css',
})
export class CreateRoleDialogComponent {
  private roleService = inject(RoleService);
  private dialogRef = inject(MatDialogRef<CreateRoleDialogComponent>);

  form = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    description: new FormControl(''),
  });

  isSubmitting = signal(false);
  errorMessage = signal<string | null>(null);

  submit(): void {
    if (this.form.invalid) return;

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    const { name, description } = this.form.getRawValue();

    this.roleService.createRole({ name, description: description ?? undefined }).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.dialogRef.close(true);
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting.set(false);
        if (err.status === 409 && err.error?.error?.code === 'ROLE_ALREADY_EXISTS') {
          this.errorMessage.set('Role name already exists. Please choose a different name.');
        } else {
          this.errorMessage.set('An unexpected error occurred. Please try again.');
        }
      },
    });
  }

  cancel(): void {
    this.dialogRef.close(false);
  }
}
