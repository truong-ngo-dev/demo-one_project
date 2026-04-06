import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export interface IamOverviewData {
  totalUsers: number;        // users có ít nhất 1 device đăng ký (proxy local)
  totalDevices: number;      // tổng số thiết bị
  activeSessions: number;    // phiên đang ACTIVE
  failedLoginsToday: number; // login thất bại trong ngày hôm nay
}

interface IamOverviewResponse {
  data: IamOverviewData;
}

@Injectable({ providedIn: 'root' })
export class IamDashboardService {
  private http = inject(HttpClient);

  /** UC-011: GET /api/oauth2/v1/admin/iam/overview — yêu cầu ROLE_ADMIN */
  getOverview(): Observable<IamOverviewData> {
    return this.http
      .get<IamOverviewResponse>('/api/oauth2/v1/admin/iam/overview')
      .pipe(map(res => res.data));
  }
}
