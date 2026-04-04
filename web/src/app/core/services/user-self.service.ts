import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, of, tap, catchError } from 'rxjs';

export interface UserProfile {
  id: string;
  email: string;
  username: string;
  fullName: string | null;
  phoneNumber: string | null;
  usernameChanged: boolean;
  hasPassword: boolean;
}

export interface UpdateProfileData {
  username?: string;
  fullName?: string;
  phoneNumber?: string;
}

export interface UpdateProfileResult {
  id: string;
  username: string;
  fullName: string | null;
  phoneNumber: string | null;
}

export interface ChangePasswordData {
  currentPassword?: string;
  newPassword: string;
}

export interface ChangePasswordResult {
  changed: boolean;
  message?: string;
}

@Injectable({ providedIn: 'root' })
export class UserSelfService {
  private http = inject(HttpClient);
  private baseUrl = '/api/admin/v1/users/me';

  private _profile = signal<UserProfile | null | undefined>(undefined);
  readonly profile = this._profile.asReadonly();

  /**
   * Load profile của user hiện tại. Kết quả được cache trong signal.
   * undefined = chưa load, null = lỗi, UserProfile = đã load.
   */
  getMe(): Observable<UserProfile | null> {
    const cached = this._profile();
    if (cached !== undefined) return of(cached);

    return this.http.get<{ data: UserProfile }>(this.baseUrl).pipe(
      map(res => res.data),
      tap(profile => this._profile.set(profile)),
      catchError(() => {
        this._profile.set(null);
        return of(null);
      }),
    );
  }

  updateProfile(data: UpdateProfileData): Observable<UpdateProfileResult> {
    return this.http.patch<{ data: UpdateProfileResult }>(this.baseUrl, data).pipe(
      map(res => res.data),
      tap(result => {
        const current = this._profile();
        if (current) {
          this._profile.set({ ...current, ...result });
        }
      }),
    );
  }

  changePassword(data: ChangePasswordData): Observable<ChangePasswordResult> {
    return this.http.post<{ data: ChangePasswordResult }>(`${this.baseUrl}/password`, data).pipe(
      map(res => res.data),
    );
  }

  clearCache(): void {
    this._profile.set(undefined);
  }
}
