import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { HttpErrorResponse } from '@angular/common/http';
import { UserService } from '../../../core/services/user.service';
import { RoleService, RoleSummary } from '../../../core/services/role.service';

@Component({
  selector: 'app-create-user-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatIconModule,
  ],
  templateUrl: './create-user-dialog.html',
  styleUrl: './create-user-dialog.css',
})
export class CreateUserDialogComponent implements OnInit {
  private userService = inject(UserService);
  private roleService = inject(RoleService);
  private dialogRef = inject(MatDialogRef<CreateUserDialogComponent>);

  form = new FormGroup({
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    username: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    fullName: new FormControl(''),
    roleIds: new FormControl<string[]>([], { nonNullable: true, validators: [Validators.required] }),
  });

  availableRoles = signal<RoleSummary[]>([]);
  isSubmitting = signal(false);
  errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.roleService.getRoles({ size: 100 }).subscribe({
      next: result => this.availableRoles.set(result.data),
    });
  }

  submit(): void {
    if (this.form.invalid) return;

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    const { email, username, fullName, roleIds } = this.form.getRawValue();

    this.userService.createUser({ email, username, fullName: fullName ?? undefined, roleIds }).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.dialogRef.close(true);
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting.set(false);
        const code = err.error?.error?.code;
        if (code === 'EMAIL_ALREADY_EXISTS') {
          this.errorMessage.set('Email is already in use.');
        } else if (code === 'USERNAME_ALREADY_EXISTS') {
          this.errorMessage.set('Username is already taken.');
        } else if (code === 'ROLE_NOT_FOUND') {
          this.errorMessage.set('One or more selected roles no longer exist.');
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
