import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export interface ActiveSessionView {
  sessionId: string;
  userId: string;
  username: string | null;
  deviceName: string | null;
  ipAddress: string;
  createdAt: string;
}

export interface AdminDeviceSessionView {
  deviceId: string;
  deviceName: string;
  ipAddress: string | null;
  lastSeenAt: string;
  sessionId: string | null;
  sessionStatus: 'ACTIVE' | null;
}

interface ActiveSessionsResponse {
  data: ActiveSessionView[];
}

@Injectable({ providedIn: 'root' })
export class AdminSessionService {
  private http = inject(HttpClient);

  /** UC-013: GET /api/oauth2/v1/sessions/admin/active */
  getActiveSessions(): Observable<ActiveSessionView[]> {
    return this.http
      .get<ActiveSessionsResponse>('/api/oauth2/v1/sessions/admin/active')
      .pipe(map(res => res.data));
  }

  /** UC-013: DELETE /api/oauth2/v1/sessions/admin/{sessionId} */
  forceTerminate(sessionId: string): Observable<void> {
    return this.http.delete<void>(`/api/oauth2/v1/sessions/admin/${sessionId}`);
  }

  /** UC-014: GET /api/oauth2/v1/admin/users/{userId}/sessions */
  getUserDeviceSessions(userId: string): Observable<AdminDeviceSessionView[]> {
    return this.http.get<AdminDeviceSessionView[]>(
      `/api/oauth2/v1/admin/users/${userId}/sessions`
    );
  }
}
