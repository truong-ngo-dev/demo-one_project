import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export type LoginResult = 'SUCCESS' | 'FAILED_WRONG_PASSWORD' | 'FAILED_ACCOUNT_LOCKED' | 'FAILED_SOCIAL';
export type LoginProvider = 'LOCAL' | 'GOOGLE';

export interface AdminLoginActivityView {
  id: string;
  userId: string | null;
  username: string;
  result: LoginResult;
  ipAddress: string;
  userAgent: string;
  deviceId: string | null;
  deviceName: string | null;
  provider: LoginProvider;
  createdAt: string;
}

export interface AdminLoginActivityPage {
  data: AdminLoginActivityView[];
  meta: { page: number; size: number; total: number };
}

export interface AdminLoginActivityFilter {
  ip?: string;
  result?: LoginResult | '';
  username?: string;
  page?: number;
  size?: number;
}

export interface UserLoginActivityView {
  result: LoginResult;
  ipAddress: string;
  deviceName: string | null;
  provider: LoginProvider;
  createdAt: string;
}

export interface UserLoginActivityPage {
  data: UserLoginActivityView[];
  meta: { page: number; size: number; total: number };
}

@Injectable({ providedIn: 'root' })
export class AdminActivityService {
  private http = inject(HttpClient);

  /** UC-012: GET /api/oauth2/v1/login-activities/admin */
  getGlobalActivities(filter: AdminLoginActivityFilter = {}): Observable<AdminLoginActivityPage> {
    let params = new HttpParams()
      .set('page', String(filter.page ?? 0))
      .set('size', String(filter.size ?? 20));

    if (filter.ip?.trim())       params = params.set('ip', filter.ip.trim());
    if (filter.result?.trim())   params = params.set('result', filter.result.trim());
    if (filter.username?.trim()) params = params.set('username', filter.username.trim());

    return this.http.get<AdminLoginActivityPage>('/api/oauth2/v1/login-activities/admin', { params });
  }

  /** UC-015: GET /api/oauth2/v1/admin/users/{userId}/login-activities */
  getUserLoginActivities(userId: string, page = 0, size = 20): Observable<UserLoginActivityPage> {
    const params = { page: String(page), size: String(Math.min(size, 50)) };
    return this.http.get<UserLoginActivityPage>(
      `/api/oauth2/v1/admin/users/${userId}/login-activities`,
      { params }
    );
  }
}
