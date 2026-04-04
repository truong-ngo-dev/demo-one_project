import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DatePipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { HttpErrorResponse } from '@angular/common/http';
import { UserService, UserDetail, RoleRef } from '../../../core/services/user.service';
import { AssignRolesDialogComponent } from '../assign-roles-dialog/assign-roles-dialog';
import { LockConfirmDialogComponent } from '../lock-confirm-dialog/lock-confirm-dialog'

@Component({
  selector: 'app-user-detail',
  standalone: true,
  imports: [
    DatePipe,
    MatButtonModule,
    MatDialogModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTooltipModule,
  ],
  templateUrl: './user-detail.html',
  styleUrl: './user-detail.css',
})
export class UserDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private userService = inject(UserService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  user = signal<UserDetail | null>(null);
  isLoading = signal(false);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.loadUser(id);
  }

  loadUser(id: string): void {
    this.isLoading.set(true);
    this.userService.getUserById(id).subscribe({
      next: u => {
        this.user.set(u);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.snackBar.open('User not found.', 'Dismiss', { duration: 3000 });
        this.router.navigate(['/admin/users']);
      },
    });
  }

  toggleLock(): void {
    const u = this.user();
    if (!u) return;

    const action = u.status === 'ACTIVE' ? 'lock' : 'unlock';
    const dialogRef = this.dialog.open(LockConfirmDialogComponent, {
      data: { user: u, action },
      width: '400px',
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (!confirmed) return;

      const request$ = action === 'lock'
        ? this.userService.lockUser(u.id)
        : this.userService.unlockUser(u.id);

      request$.subscribe({
        next: () => {
          this.snackBar.open(`User ${action === 'lock' ? 'locked' : 'unlocked'}.`, 'Dismiss', { duration: 3000 });
          this.loadUser(u.id);
        },
        error: (err: HttpErrorResponse) => {
          if (err.error?.error?.code === 'INVALID_STATUS') {
            this.snackBar.open('Cannot perform this action: invalid user status.', 'Dismiss', { duration: 4000 });
          } else {
            this.snackBar.open('Operation failed.', 'Dismiss', { duration: 3000 });
          }
        },
      });
    });
  }

  openAssignRoles(): void {
    const u = this.user();
    if (!u) return;

    this.dialog
      .open(AssignRolesDialogComponent, {
        data: { user: u, assignedRoles: u.roles },
        width: '480px',
      })
      .afterClosed()
      .subscribe(assigned => {
        if (assigned) this.loadUser(u.id);
      });
  }

  removeRole(role: RoleRef): void {
    const u = this.user();
    if (!u) return;

    this.userService.removeRole(u.id, role.id).subscribe({
      next: () => {
        this.snackBar.open(`Role "${role.name}" removed.`, 'Dismiss', { duration: 3000 });
        this.loadUser(u.id);
      },
      error: (err: HttpErrorResponse) => {
        if (err.error?.error?.code === 'ROLE_NOT_FOUND') {
          this.snackBar.open('Role not found on this user.', 'Dismiss', { duration: 3000 });
        } else {
          this.snackBar.open('Failed to remove role.', 'Dismiss', { duration: 3000 });
        }
      },
    });
  }

  goBack(): void {
    this.router.navigate(['/admin/users']);
  }
}
