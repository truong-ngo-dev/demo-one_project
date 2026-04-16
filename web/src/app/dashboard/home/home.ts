import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';
import { IamDashboardService } from '../../core/services/iam-dashboard.service';
import { UserService } from '../../core/services/user.service';
import { UIElementService, UncoveredUIElement } from '../../core/services/ui-element.service';
import { AuditLogService, AuditLogEntry } from '../../core/services/audit-log.service';

interface DashboardData {
  activeSessions: number;
  failedLoginsToday: number;
  totalUsers: number;
  activeUsers: number;
  lockedUsers: number;
  uncoveredElements: UncoveredUIElement[];
  recentAudit: AuditLogEntry[];
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [MatIconModule, MatProgressSpinnerModule, MatCardModule, MatTooltipModule],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class HomeComponent implements OnInit {
  private iamDashboardService = inject(IamDashboardService);
  private userService         = inject(UserService);
  private uiElementService    = inject(UIElementService);
  private auditLogService     = inject(AuditLogService);
  private router              = inject(Router);

  isLoading = signal(true);
  error     = signal<string | null>(null);
  data      = signal<DashboardData | null>(null);

  uncoveredCount = computed(() => this.data()?.uncoveredElements.length ?? 0);

  ngOnInit(): void {
    forkJoin({
      overview:     this.iamDashboardService.getOverview(),
      allUsers:     this.userService.getUsers({ size: 1 }),
      activeUsers:  this.userService.getUsers({ status: 'ACTIVE', size: 1 }),
      lockedUsers:  this.userService.getUsers({ status: 'LOCKED', size: 1 }),
      uncovered:    this.uiElementService.getUncoveredUIElements(),
      recentAudit:  this.auditLogService.getAuditLog({ size: 5 }),
    }).subscribe({
      next: results => {
        this.data.set({
          activeSessions:    results.overview.activeSessions,
          failedLoginsToday: results.overview.failedLoginsToday,
          totalUsers:        results.allUsers.meta.total,
          activeUsers:       results.activeUsers.meta.total,
          lockedUsers:       results.lockedUsers.meta.total,
          uncoveredElements: results.uncovered,
          recentAudit:       results.recentAudit.data,
        });
        this.isLoading.set(false);
      },
      error: () => {
        this.error.set('Không thể tải dữ liệu. Vui lòng thử lại.');
        this.isLoading.set(false);
      },
    });
  }

  navigateToUncovered(): void {
    this.router.navigate(['/admin/abac/ui-elements'], { queryParams: { filter: 'uncovered' } });
  }

  navigateToAuditLog(): void {
    this.router.navigate(['/admin/abac/audit-log']);
  }

  navigateToLockedUsers(): void {
    this.router.navigate(['/admin/users'], { queryParams: { status: 'LOCKED' } });
  }

  formatAuditEntry(entry: AuditLogEntry): string {
    const action = entry.actionType.toLowerCase();
    const entity = entry.entityType.replace('_', ' ').toLowerCase();
    const name   = entry.entityName ? `"${entry.entityName}"` : `#${entry.entityId}`;
    return `${entity} ${name} ${action}`;
  }

  formatTimestamp(ts: number): string {
    return new Date(ts).toLocaleString('vi-VN', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit',
    });
  }
}
