import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export type UserStatus = 'ACTIVE' | 'LOCKED';

export interface UserSummary {
  id: string;
  email: string;
  username: string;
  fullName: string | null;
  status: UserStatus;
}

export interface RoleRef {
  id: string;
  name: string;
}

export interface SocialConnectionRef {
  provider: string;
  connectedAt: number;
}

export interface UserDetail {
  id: string;
  email: string;
  username: string;
  fullName: string | null;
  status: UserStatus;
  roles: RoleRef[];
  socialConnections: SocialConnectionRef[];
  createdAt: string;
}

export interface UserPage {
  data: UserSummary[];
  meta: { page: number; size: number; total: number };
}

export interface GetUsersParams {
  keyword?: string;
  status?: UserStatus;
  roleId?: string;
  page?: number;
  size?: number;
  sort?: string;
}

@Injectable({ providedIn: 'root' })
export class UserService {
  private http = inject(HttpClient);
  private baseUrl = '/api/admin/v1/users';

  getUsers(params: GetUsersParams = {}): Observable<UserPage> {
    const queryParams: Record<string, string> = {};
    if (params.keyword) queryParams['keyword'] = params.keyword;
    if (params.status) queryParams['status'] = params.status;
    if (params.roleId) queryParams['roleId'] = params.roleId;
    if (params.page !== undefined) queryParams['page'] = String(params.page);
    if (params.size !== undefined) queryParams['size'] = String(params.size);
    if (params.sort) queryParams['sort'] = params.sort;
    return this.http.get<UserPage>(this.baseUrl, { params: queryParams });
  }

  getUserById(id: string): Observable<UserDetail> {
    return this.http.get<{ data: UserDetail }>(`${this.baseUrl}/${id}`).pipe(
      map(res => res.data),
    );
  }

  createUser(data: { email: string; username: string; fullName?: string; roleIds: string[] }): Observable<{ id: string; username: string }> {
    return this.http.post<{ data: { id: string; username: string } }>(this.baseUrl, data).pipe(
      map(res => res.data),
    );
  }

  lockUser(id: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${id}/lock`, null);
  }

  unlockUser(id: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${id}/unlock`, null);
  }

  assignRoles(userId: string, roleIds: string[]): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${userId}/roles`, { roleIds });
  }

  removeRole(userId: string, roleId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${userId}/roles/${roleId}`);
  }
}
