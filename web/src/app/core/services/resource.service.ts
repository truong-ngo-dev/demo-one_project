import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface ActionView {
  id: number;
  name: string;
  description: string | null;
  isStandard: boolean;
}

export interface ResourceView {
  id: number;
  name: string;
  description: string | null;
  serviceName: string;
  actions: ActionView[];
}

export interface ResourceSummaryView {
  id: number;
  name: string;
  serviceName: string;
  actionCount: number;
}

export interface ResourcePage {
  data: ResourceSummaryView[];
  meta: { page: number; size: number; total: number };
}

export interface CreateResourceRequest {
  name: string;
  description?: string;
  serviceName: string;
}

export interface UpdateResourceRequest {
  description?: string;
  serviceName: string;
}

export interface AddActionRequest {
  name: string;
  description?: string;
  isStandard: boolean;
}

export interface UpdateActionRequest {
  description?: string;
}

export interface GetResourcesParams {
  keyword?: string;
  page?: number;
  size?: number;
}

@Injectable({ providedIn: 'root' })
export class ResourceService {
  private http = inject(HttpClient);
  private baseUrl = '/api/admin/v1/abac/resources';

  getResources(params: GetResourcesParams = {}): Observable<ResourcePage> {
    const queryParams: Record<string, string> = {};
    if (params.keyword) queryParams['keyword'] = params.keyword;
    if (params.page !== undefined) queryParams['page'] = String(params.page);
    if (params.size !== undefined) queryParams['size'] = String(params.size);
    return this.http.get<ResourcePage>(this.baseUrl, { params: queryParams });
  }

  getResourceById(id: number): Observable<ResourceView> {
    return this.http.get<{ data: ResourceView }>(`${this.baseUrl}/${id}`).pipe(
      map(res => res.data),
    );
  }

  createResource(data: CreateResourceRequest): Observable<{ id: number }> {
    return this.http.post<{ data: { id: number } }>(this.baseUrl, data).pipe(
      map(res => res.data),
    );
  }

  updateResource(id: number, data: UpdateResourceRequest): Observable<{ id: number }> {
    return this.http.put<{ data: { id: number } }>(`${this.baseUrl}/${id}`, data).pipe(
      map(res => res.data),
    );
  }

  deleteResource(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  addAction(resourceId: number, data: AddActionRequest): Observable<{ actionId: number }> {
    return this.http
      .post<{ data: { actionId: number } }>(`${this.baseUrl}/${resourceId}/actions`, data)
      .pipe(map(res => res.data));
  }

  updateAction(resourceId: number, actionId: number, data: UpdateActionRequest): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/${resourceId}/actions/${actionId}`, data);
  }

  removeAction(resourceId: number, actionId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${resourceId}/actions/${actionId}`);
  }
}
