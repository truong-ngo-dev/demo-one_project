import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, debounceTime, takeUntil } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
  AdminActivityService,
  AdminLoginActivityView,
  LoginResult,
} from '../../core/services/admin-activity.service';

@Component({
  selector: 'app-login-activities',
  standalone: true,
  imports: [
    DatePipe,
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatTableModule,
    MatTooltipModule,
  ],
  templateUrl: './login-activities.html',
  styleUrl: './login-activities.css',
})
export class LoginActivitiesComponent implements OnInit, OnDestroy {
  private adminActivityService = inject(AdminActivityService);
  private destroy$ = new Subject<void>();
  private filterSubject = new Subject<void>();

  activities = signal<AdminLoginActivityView[]>([]);
  total = signal(0);
  isLoading = signal(false);

  page = signal(0);
  pageSize = signal(20);
  filterIp = signal('');
  filterUsername = signal('');
  filterResult = signal<LoginResult | ''>('');

  readonly displayedColumns = ['createdAt', 'username', 'result', 'ipAddress', 'deviceName', 'provider'];
  readonly resultOptions: Array<{ value: LoginResult | ''; label: string }> = [
    { value: '', label: 'All' },
    { value: 'SUCCESS', label: 'Success' },
    { value: 'FAILED_WRONG_PASSWORD', label: 'Wrong Password' },
    { value: 'FAILED_ACCOUNT_LOCKED', label: 'Account Locked' },
    { value: 'FAILED_SOCIAL', label: 'Social Failed' },
  ];

  ngOnInit(): void {
    this.filterSubject.pipe(debounceTime(300), takeUntil(this.destroy$)).subscribe(() => {
      this.page.set(0);
      this.load();
    });
    this.load();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onFilterChange(): void {
    this.filterSubject.next();
  }

  onResultChange(value: LoginResult | ''): void {
    this.filterResult.set(value);
    this.page.set(0);
    this.load();
  }

  onPage(event: PageEvent): void {
    this.page.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.load();
  }

  load(): void {
    this.isLoading.set(true);
    this.adminActivityService
      .getGlobalActivities({
        ip: this.filterIp(),
        username: this.filterUsername(),
        result: this.filterResult(),
        page: this.page(),
        size: this.pageSize(),
      })
      .subscribe({
        next: res => {
          this.activities.set(res.data);
          this.total.set(res.meta.total);
          this.isLoading.set(false);
        },
        error: () => this.isLoading.set(false),
      });
  }
}
