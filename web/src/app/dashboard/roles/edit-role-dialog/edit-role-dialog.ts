import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { HttpErrorResponse } from '@angular/common/http';
import { RoleService, RoleSummary } from '../../../core/services/role.service';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatIcon } from '@angular/material/icon';

@Component({
  selector: 'app-edit-role-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIcon,
    MatProgressSpinner,
  ],
  templateUrl: './edit-role-dialog.html',
  styleUrl: './edit-role-dialog.css',
})
export class EditRoleDialogComponent {
  private roleService = inject(RoleService);
  private dialogRef = inject(MatDialogRef<EditRoleDialogComponent>);
  protected role: RoleSummary = inject(MAT_DIALOG_DATA);

  form = new FormGroup({
    description: new FormControl<string | null>(null),
  });

  isSubmitting = signal(false);
  errorMessage = signal<string | null>(null);

  submit(): void {
    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    const { description } = this.form.getRawValue();

    this.roleService.updateRole(this.role.id, { description }).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.dialogRef.close(true);
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting.set(false);
        if (err.status === 404 && err.error?.error?.code === 'ROLE_NOT_FOUND') {
          this.errorMessage.set('Role no longer exists. It may have been deleted.');
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
