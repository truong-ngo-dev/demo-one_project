import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { Subject, debounceTime, takeUntil } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { RoleService, RoleSummary } from '../../core/services/role.service';
import { CreateRoleDialogComponent } from './create-role-dialog/create-role-dialog';
import { EditRoleDialogComponent } from './edit-role-dialog/edit-role-dialog';

@Component({
  selector: 'app-roles',
  standalone: true,
  imports: [
    FormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTableModule,
  ],
  templateUrl: './roles.html',
  styleUrl: './roles.css',
})
export class RolesComponent implements OnInit, OnDestroy {
  private roleService = inject(RoleService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private destroy$ = new Subject<void>();
  private keywordSubject = new Subject<string>();

  roles = signal<RoleSummary[]>([]);
  isLoading = signal(false);
  keyword = signal('');

  readonly displayedColumns = ['name', 'description', 'actions'];

  ngOnInit(): void {
    this.keywordSubject.pipe(debounceTime(300), takeUntil(this.destroy$)).subscribe(kw => {
      this.loadRoles(kw);
    });
    this.loadRoles();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onKeywordChange(value: string): void {
    this.keyword.set(value);
    this.keywordSubject.next(value);
  }

  loadRoles(kw?: string): void {
    this.isLoading.set(true);
    this.roleService.getRoles({ keyword: (kw ?? this.keyword()) || undefined }).subscribe({
      next: result => {
        this.roles.set(result.data);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  openCreateDialog(): void {
    this.dialog
      .open(CreateRoleDialogComponent, { width: '480px' })
      .afterClosed()
      .subscribe(created => {
        if (created) this.loadRoles();
      });
  }

  openEditDialog(role: RoleSummary): void {
    this.dialog
      .open(EditRoleDialogComponent, { width: '480px', data: role })
      .afterClosed()
      .subscribe(updated => {
        if (updated) this.loadRoles();
      });
  }

  deleteRole(role: RoleSummary): void {
    const confirmed = this.dialog.open(ConfirmDeleteDialogComponent, {
      data: { roleName: role.name },
      width: '400px',
    });

    confirmed.afterClosed().subscribe(ok => {
      if (!ok) return;

      this.roleService.deleteRole(role.id).subscribe({
        next: () => {
          this.snackBar.open(`Role "${role.name}" deleted.`, 'Dismiss', { duration: 3000 });
          this.loadRoles();
        },
        error: (err: HttpErrorResponse) => {
          if (err.status === 409 && err.error?.error?.code === 'ROLE_IN_USE') {
            this.snackBar.open(
              `Cannot delete "${role.name}": role is currently assigned to one or more users.`,
              'Dismiss',
              { duration: 5000 },
            );
          } else {
            this.snackBar.open('Failed to delete role. Please try again.', 'Dismiss', {
              duration: 3000,
            });
          }
        },
      });
    });
  }
}

// ---------------------------------------------------------------------------
// Confirm delete dialog — defined here to keep it co-located with usage
// ---------------------------------------------------------------------------

import { Component as NgComponent, inject as ngInject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@NgComponent({
  selector: 'app-confirm-delete-dialog',
  standalone: true,
  imports: [MatButtonModule, MatDialogModule],
  template: `
    <h2 mat-dialog-title>Delete Role</h2>
    <mat-dialog-content>
      <p>Are you sure you want to delete role <strong>{{ data.roleName }}</strong>? This action cannot be undone.</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="dialogRef.close(false)">Cancel</button>
      <button mat-flat-button color="warn" (click)="dialogRef.close(true)">Delete</button>
    </mat-dialog-actions>
  `,
})
export class ConfirmDeleteDialogComponent {
  dialogRef = ngInject(MatDialogRef<ConfirmDeleteDialogComponent>);
  data: { roleName: string } = ngInject(MAT_DIALOG_DATA);
}
