import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, map, of, switchMap, tap } from 'rxjs';
import { Router } from '@angular/router';

interface SessionInfo {
  sub: string;
  requiresProfileCompletion: boolean;
}

export interface SessionUser {
  id: string;
  roles: { id: string; name: string }[];
  requiresProfileCompletion: boolean;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private _currentUser = signal<SessionUser | null | undefined>(undefined);

  get currentUser(): SessionUser | null | undefined {
    return this._currentUser();
  }

  /**
   * Load user từ session + admin service. Kết quả được cache trong signal.
   * undefined = chưa load, null = chưa xác thực, SessionUser = đã xác thực.
   */
  loadCurrentUser(): Observable<SessionUser | null> {
    const cached = this._currentUser();
    if (cached !== undefined) {
      return of(cached);
    }

    return this.http.get<SessionInfo>('/webgw/auth/session').pipe(
      switchMap(session =>
        this.http.get<{ data: SessionUser }>(`/api/admin/v1/users/${session.sub}`).pipe(
          map(res => ({ ...res.data, requiresProfileCompletion: session.requiresProfileCompletion ?? false })),
        ),
      ),
      tap(user => this._currentUser.set(user)),
      catchError(() => {
        this._currentUser.set(null);
        return of(null);
      }),
    );
  }

  /** Kiểm tra xác thực: 200 → có session hợp lệ, 401 → chưa xác thực. */
  checkAuth(): Observable<boolean> {
    return this.http.get('/webgw/auth/session').pipe(
      map(() => true),
      catchError(() => of(false)),
    );
  }

  /**
   * Đăng xuất: POST /webgw/auth/logout → nhận 202 + Location → navigate.
   * Web Gateway trả 202 thay vì 302 vì Angular gọi qua HttpClient (XHR).
   */
  logout(): void {
    this._currentUser.set(undefined);
    this.http
      .post('/webgw/auth/logout', {}, { observe: 'response' })
      .subscribe({
        next: (response) => {
          const location = response.headers.get('Location');
          if (location) {
            window.location.href = location;
          } else {
            this.router.navigate(['/']);
          }
        },
        error: () => {
          this.router.navigate(['/']);
        },
      });
  }
}
