import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { HttpErrorResponse } from '@angular/common/http';
import {UserService, RoleRef, UserSummary} from '../../../core/services/user.service';
import { RoleService, RoleSummary } from '../../../core/services/role.service';

export interface AssignRolesDialogData {
  user: UserSummary;
  assignedRoles: RoleRef[];
}

@Component({
  selector: 'app-assign-roles-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatIconModule,
  ],
  templateUrl: './assign-roles-dialog.html',
  styleUrl: './assign-roles-dialog.css',
})
export class AssignRolesDialogComponent implements OnInit {
  private userService = inject(UserService);
  private roleService = inject(RoleService);
  private dialogRef = inject(MatDialogRef<AssignRolesDialogComponent>);
  protected data: AssignRolesDialogData = inject(MAT_DIALOG_DATA);

  form = new FormGroup({
    roleIds: new FormControl<string[]>([], { nonNullable: true, validators: [Validators.required] }),
  });

  availableRoles = signal<RoleSummary[]>([]);
  isSubmitting = signal(false);
  errorMessage = signal<string | null>(null);
  noRolesInSystem = signal(false);

  ngOnInit(): void {// ĐÃ SỬA: Dùng assignedRoles
    const assignedIds = new Set(this.data.assignedRoles.map(r => r.id));
    this.roleService.getRoles({ size: 100 }).subscribe({
      next: result => {
        if (result.data.length === 0) {
          this.noRolesInSystem.set(true); // THÊM DÒNG NÀY
        } else {
          this.availableRoles.set(result.data.filter(r => !assignedIds.has(r.id)));
        }
      },
    });
  }

  submit(): void {
    if (this.form.invalid) return;

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    const { roleIds } = this.form.getRawValue();

    this.userService.assignRoles(this.data.user.id, roleIds).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.dialogRef.close(true);
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting.set(false);
        if (err.error?.error?.code === 'ROLE_NOT_FOUND') {
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
