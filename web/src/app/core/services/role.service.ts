import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface RoleSummary {
  id: string;
  name: string;
  description: string | null;
}

export interface RoleDetail {
  id: string;
  name: string;
  description: string | null;
  createdAt: string;
}

export interface RolePage {
  data: RoleSummary[];
  meta: { page: number; size: number; total: number };
}

export interface GetRolesParams {
  keyword?: string;
  page?: number;
  size?: number;
  sort?: string;
}

@Injectable({ providedIn: 'root' })
export class RoleService {
  private http = inject(HttpClient);
  private baseUrl = '/api/admin/v1/roles';

  getRoles(params: GetRolesParams = {}): Observable<RolePage> {
    const queryParams: Record<string, string> = {};
    if (params.keyword) queryParams['keyword'] = params.keyword;
    if (params.page !== undefined) queryParams['page'] = String(params.page);
    if (params.size !== undefined) queryParams['size'] = String(params.size);
    if (params.sort) queryParams['sort'] = params.sort;
    return this.http.get<RolePage>(this.baseUrl, { params: queryParams });
  }

  getRoleById(id: string): Observable<RoleDetail> {
    return this.http.get<{ data: RoleDetail }>(`${this.baseUrl}/${id}`).pipe(
      map(res => res.data),
    );
  }

  createRole(data: { name: string; description?: string }): Observable<{ id: string }> {
    return this.http.post<{ data: { id: string } }>(this.baseUrl, data).pipe(
      map(res => res.data),
    );
  }

  updateRole(id: string, data: { description: string | null }): Observable<RoleDetail> {
    return this.http.patch<{ data: RoleDetail }>(`${this.baseUrl}/${id}`, data).pipe(
      map(res => res.data),
    );
  }

  deleteRole(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
