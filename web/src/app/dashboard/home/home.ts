import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { IamDashboardService, IamOverviewData } from '../../core/services/iam-dashboard.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [MatIconModule, MatProgressSpinnerModule, MatCardModule],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class HomeComponent implements OnInit {
  private iamDashboardService = inject(IamDashboardService);

  isLoading = signal(true);
  error = signal<string | null>(null);
  overviewData = signal<IamOverviewData | null>(null);

  kpiCards = computed(() => {
    const data = this.overviewData();
    return [
      {
        label: 'Người dùng đã đăng nhập',
        value: data?.totalUsers ?? 0,
        icon: 'person',
      },
      {
        label: 'Thiết bị',
        value: data?.totalDevices ?? 0,
        icon: 'devices',
      },
      {
        label: 'Phiên đang hoạt động',
        value: data?.activeSessions ?? 0,
        icon: 'wifi',
      },
      {
        label: 'Đăng nhập thất bại hôm nay',
        value: data?.failedLoginsToday ?? 0,
        icon: 'gpp_bad',
      },
    ];
  });

  ngOnInit(): void {
    this.iamDashboardService.getOverview().subscribe({
      next: data => {
        this.overviewData.set(data);
        this.isLoading.set(false);
      },
      error: err => {
        const status = err?.status;
        if (status === 403) {
          this.error.set('Bạn không có quyền truy cập trang này.');
        } else {
          this.error.set('Không thể tải dữ liệu. Vui lòng thử lại.');
        }
        this.isLoading.set(false);
      },
    });
  }
}
