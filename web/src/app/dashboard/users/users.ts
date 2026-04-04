import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, debounceTime, takeUntil } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { HttpErrorResponse } from '@angular/common/http';
import { UserService, UserStatus, UserSummary } from '../../core/services/user.service';
import { CreateUserDialogComponent } from './create-user-dialog/create-user-dialog';
import {LockConfirmDialogComponent} from './lock-confirm-dialog/lock-confirm-dialog';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [
    FormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatSnackBarModule,
    MatTableModule,
    MatTooltipModule,
  ],
  templateUrl: './users.html',
  styleUrl: './users.css',
})
export class UsersComponent implements OnInit, OnDestroy {
  private userService = inject(UserService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);
  private destroy$ = new Subject<void>();
  private keywordSubject = new Subject<string>();

  users = signal<UserSummary[]>([]);
  isLoading = signal(false);
  keyword = signal('');
  statusFilter = signal<UserStatus | ''>('');

  readonly displayedColumns = ['username', 'email', 'fullName', 'status', 'actions'];
  readonly statusOptions: Array<{ value: UserStatus | ''; label: string }> = [
    { value: '', label: 'All' },
    { value: 'ACTIVE', label: 'Active' },
    { value: 'LOCKED', label: 'Locked' },
  ];

  ngOnInit(): void {
    this.keywordSubject.pipe(debounceTime(300), takeUntil(this.destroy$)).subscribe(kw => {
      this.loadUsers(kw);
    });
    this.loadUsers();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onKeywordChange(value: string): void {
    this.keyword.set(value);
    this.keywordSubject.next(value);
  }

  onStatusChange(value: UserStatus | ''): void {
    this.statusFilter.set(value);
    this.loadUsers();
  }

  loadUsers(kw?: string): void {
    this.isLoading.set(true);
    const status = this.statusFilter() || undefined;
    this.userService.getUsers({ keyword: (kw ?? this.keyword()) || undefined, status }).subscribe({
      next: result => {
        this.users.set(result.data);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  viewUser(user: UserSummary): void {
    this.router.navigate(['/admin/users', user.id]);
  }

  openCreateDialog(): void {
    this.dialog
      .open(CreateUserDialogComponent, { width: '520px' })
      .afterClosed()
      .subscribe(created => {
        if (created) this.loadUsers();
      });
  }

  toggleLock(user: UserSummary): void {
    const action = user.status === 'ACTIVE' ? 'lock' : 'unlock';
    const dialogRef = this.dialog.open(LockConfirmDialogComponent, {
      data: { user, action },
      width: '400px',
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (!confirmed) return;

      const request$ = action === 'lock'
        ? this.userService.lockUser(user.id)
        : this.userService.unlockUser(user.id);

      request$.subscribe({
        next: () => {
          this.snackBar.open(
            `User "${user.username}" ${action === 'lock' ? 'locked' : 'unlocked'} successfully.`,
            'Dismiss',
            { duration: 3000 },
          );
          this.loadUsers();
        },
        error: (err: HttpErrorResponse) => {
          if (err.error?.error?.code === 'INVALID_STATUS') {
            this.snackBar.open(
              `Cannot ${action} user: current status does not allow this operation.`,
              'Dismiss',
              { duration: 4000 },
            );
          } else {
            this.snackBar.open(`Failed to ${action} user.`, 'Dismiss', { duration: 3000 });
          }
        },
      });
    });
  }
}
