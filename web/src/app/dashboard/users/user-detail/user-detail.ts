import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AbacService } from '../../../core/services/abac.service';
import { UserService, UserDetail, RoleRef } from '../../../core/services/user.service';
import { AdminSessionService, AdminDeviceSessionView } from '../../../core/services/admin-session.service';
import { AdminActivityService, UserLoginActivityView, UserLoginActivityPage } from '../../../core/services/admin-activity.service';
import { AssignRolesDialogComponent } from '../assign-roles-dialog/assign-roles-dialog';
import { LockConfirmDialogComponent } from '../lock-confirm-dialog/lock-confirm-dialog';
import { ForceTerminateDialogComponent } from '../../active-sessions/force-terminate-dialog/force-terminate-dialog';
import { MatDivider } from '@angular/material/divider';

@Component({
  selector: 'app-user-detail',
  standalone: true,
  imports: [
    DatePipe,
    MatButtonModule,
    MatDialogModule,
    MatIconModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTabsModule,
    MatTableModule,
    MatTooltipModule,
    MatDivider,
  ],
  templateUrl: './user-detail.html',
  styleUrl: './user-detail.css',
})
export class UserDetailComponent implements OnInit {
  private route              = inject(ActivatedRoute);
  private router             = inject(Router);
  private userService        = inject(UserService);
  private adminSessionService  = inject(AdminSessionService);
  private adminActivityService = inject(AdminActivityService);
  private dialog             = inject(MatDialog);
  private snackBar           = inject(MatSnackBar);
  readonly abacService       = inject(AbacService);

  // ── Profile ──
  user       = signal<UserDetail | null>(null);
  isLoading  = signal(false);

  // ── Security tab ──
  securityLoaded    = signal(false);
  securityLoading   = signal(false);
  deviceSessions    = signal<AdminDeviceSessionView[]>([]);
  loginActivities   = signal<UserLoginActivityView[]>([]);
  activityTotal     = signal(0);
  activityPage      = signal(0);
  activityPageSize  = signal(20);
  terminatingIds    = signal<Set<string>>(new Set());

  readonly deviceColumns   = ['deviceName', 'ipAddress', 'lastSeenAt', 'sessionStatus', 'actions'];
  readonly activityColumns = ['createdAt', 'result', 'ipAddress', 'deviceName', 'provider'];

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.loadUser(id);
  }

  loadUser(id: string): void {
    this.isLoading.set(true);
    this.userService.getUserById(id).subscribe({
      next: u => { this.user.set(u); this.isLoading.set(false); },
      error: () => {
        this.isLoading.set(false);
        this.snackBar.open('User not found.', 'Dismiss', { duration: 3000 });
        this.router.navigate(['/admin/users']);
      },
    });
  }

  /** Lazy-load security data khi tab "Security & Devices" được chọn lần đầu */
  onTabChange(index: number): void {
    if (index === 1 && !this.securityLoaded()) {
      this.loadSecurityData();
    }
  }

  loadSecurityData(): void {
    const userId = this.user()?.id;
    if (!userId) return;

    this.securityLoading.set(true);
    this.securityLoaded.set(true);

    this.adminSessionService.getUserDeviceSessions(userId).subscribe({
      next: data => this.deviceSessions.set(data),
      error: () => this.snackBar.open('Failed to load device sessions.', 'Dismiss', { duration: 3000 }),
    });

    this.loadActivityPage(userId, 0);
  }

  loadActivityPage(userId: string, page: number): void {
    this.adminActivityService
      .getUserLoginActivities(userId, page, this.activityPageSize())
      .subscribe({
        next: (res: UserLoginActivityPage) => {
          this.loginActivities.set(res.data);
          this.activityTotal.set(res.meta.total);
          this.activityPage.set(res.meta.page);
          this.securityLoading.set(false);
        },
        error: () => {
          this.securityLoading.set(false);
          this.snackBar.open('Failed to load login history.', 'Dismiss', { duration: 3000 });
        },
      });
  }

  onActivityPage(event: PageEvent): void {
    const userId = this.user()?.id;
    if (!userId) return;
    this.activityPageSize.set(event.pageSize);
    this.loadActivityPage(userId, event.pageIndex);
  }

  openRevokeDialog(session: AdminDeviceSessionView): void {
    // Tái dụng ForceTerminateDialogComponent — map sang ActiveSessionView shape
    const sessionAsActive = {
      sessionId: session.sessionId!,
      userId: this.user()?.id ?? '',
      username: this.user()?.username ?? null,
      deviceName: session.deviceName ?? null,
      ipAddress: session.ipAddress ?? '',
      createdAt: session.lastSeenAt,
    };

    this.dialog
      .open(ForceTerminateDialogComponent, { data: { session: sessionAsActive }, width: '440px' })
      .afterClosed()
      .subscribe(confirmed => {
        if (confirmed) this.doRevoke(session);
      });
  }

  private doRevoke(session: AdminDeviceSessionView): void {
    if (!session.sessionId) return;

    const ids = new Set(this.terminatingIds());
    ids.add(session.sessionId);
    this.terminatingIds.set(ids);

    this.adminSessionService.forceTerminate(session.sessionId).subscribe({
      next: () => {
        this.deviceSessions.update(list =>
          list.map(d => d.deviceId === session.deviceId
            ? { ...d, sessionId: null, sessionStatus: null }
            : d)
        );
        const done = new Set(this.terminatingIds());
        done.delete(session.sessionId!);
        this.terminatingIds.set(done);
        this.snackBar.open('Session revoked.', 'Dismiss', { duration: 3000 });
      },
      error: (err: HttpErrorResponse) => {
        const done = new Set(this.terminatingIds());
        done.delete(session.sessionId!);
        this.terminatingIds.set(done);
        const code = err.error?.code;
        const msg = code === '02001' ? 'Session not found.' : 'Failed to revoke session.';
        this.snackBar.open(msg, 'Dismiss', { duration: 4000 });
        this.loadSecurityData();
      },
    });
  }

  isTerminating(sessionId: string): boolean {
    return this.terminatingIds().has(sessionId);
  }

  // ── Profile actions ──
  toggleLock(): void {
    const u = this.user();
    if (!u) return;
    const action = u.status === 'ACTIVE' ? 'lock' : 'unlock';
    this.dialog
      .open(LockConfirmDialogComponent, { data: { user: u, action }, width: '400px' })
      .afterClosed()
      .subscribe(confirmed => {
        if (!confirmed) return;
        const req$ = action === 'lock' ? this.userService.lockUser(u.id) : this.userService.unlockUser(u.id);
        req$.subscribe({
          next: () => {
            this.snackBar.open(`User ${action === 'lock' ? 'locked' : 'unlocked'}.`, 'Dismiss', { duration: 3000 });
            this.loadUser(u.id);
          },
          error: (err: HttpErrorResponse) => {
            const msg = err.error?.error?.code === 'INVALID_STATUS'
              ? 'Cannot perform this action: invalid user status.'
              : 'Operation failed.';
            this.snackBar.open(msg, 'Dismiss', { duration: 4000 });
          },
        });
      });
  }

  openAssignRoles(): void {
    const u = this.user();
    if (!u) return;
    this.dialog
      .open(AssignRolesDialogComponent, { data: { user: u, assignedRoles: u.roles }, width: '480px' })
      .afterClosed()
      .subscribe(assigned => { if (assigned) this.loadUser(u.id); });
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
        const msg = err.error?.error?.code === 'ROLE_NOT_FOUND'
          ? 'Role not found on this user.'
          : 'Failed to remove role.';
        this.snackBar.open(msg, 'Dismiss', { duration: 3000 });
      },
    });
  }

  goBack(): void {
    this.router.navigate(['/admin/users']);
  }
}
