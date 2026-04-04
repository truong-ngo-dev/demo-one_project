import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DeviceSession {
  deviceId: string;
  deviceName: string;
  ipAddress: string;
  lastSeenAt: string;
  sessionId: string | null;
  sessionStatus: 'ACTIVE' | null;
  isCurrent: boolean;
}

export interface LoginActivityItem {
  result: string;
  ipAddress: string;
  userAgent: string;
  provider: string;
  createdAt: string;
  deviceId: string | null;
  deviceName: string | null;
}

export interface LoginActivityPage {
  content: LoginActivityItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

@Injectable({ providedIn: 'root' })
export class SessionService {
  private http = inject(HttpClient);

  /** UC-007: GET /api/oauth2/v1/sessions/me */
  getMyDevices(): Observable<DeviceSession[]> {
    return this.http.get<DeviceSession[]>('/api/oauth2/v1/sessions/me');
  }

  /** UC-008: DELETE /api/oauth2/v1/sessions/me/{sessionId} */
  revokeSession(sessionId: string): Observable<void> {
    return this.http.delete<void>(`/api/oauth2/v1/sessions/me/${sessionId}`);
  }

  /** UC-012: GET /api/oauth2/v1/login-activities/me */
  getMyLoginActivities(page = 0, size = 20): Observable<LoginActivityPage> {
    return this.http.get<LoginActivityPage>('/api/oauth2/v1/login-activities/me', {
      params: { page: String(page), size: String(size) },
    });
  }
}
