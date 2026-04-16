import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export type UIElementScope = 'ADMIN' | 'OPERATOR' | 'TENANT' | 'RESIDENT';

export interface UIElementView {
  id: number;
  elementId: string;
  label: string;
  type: 'BUTTON' | 'TAB' | 'MENU_ITEM';
  scope: UIElementScope;
  elementGroup: string | null;
  orderIndex: number;
  resourceId: number;
  resourceName: string;
  actionId: number;
  actionName: string;
  hasPolicyCoverage: boolean;
}

export type UIElementSummary = UIElementView;

export interface UncoveredUIElement {
  id: number;
  elementId: string;
  label: string;
  type: string;
  elementGroup: string | null;
  resourceId: number;
  resourceName: string;
  actionId: number;
  actionName: string | null;
}

export interface UIElementPage {
  data: UIElementSummary[];
  meta: { page: number; size: number; total: number };
}

export interface CreateUIElementRequest {
  elementId: string;
  label: string;
  type: 'BUTTON' | 'TAB' | 'MENU_ITEM';
  scope?: UIElementScope;  // defaults to ADMIN on backend when not specified
  group?: string | null;
  orderIndex: number;
  resourceId: number;
  actionId: number;
}

export interface UpdateUIElementRequest {
  label: string;
  type: 'BUTTON' | 'TAB' | 'MENU_ITEM';
  scope?: UIElementScope;  // keeps existing scope on backend when not specified
  group?: string | null;
  orderIndex: number;
  resourceId: number;
  actionId: number;
}

export interface EvaluateRequest {
  elementIds: string[];
}

export interface EvaluateResponse {
  results: Record<string, 'PERMIT' | 'DENY'>;
}

export interface GetUIElementsParams {
  resourceId?: number;
  type?: string;
  group?: string;
  scope?: UIElementScope;
  page?: number;
  size?: number;
}

@Injectable({ providedIn: 'root' })
export class UIElementService {
  private http = inject(HttpClient);
  private baseUrl = '/api/admin/v1/abac/ui-elements';

  getUIElements(params: GetUIElementsParams = {}): Observable<UIElementPage> {
    const queryParams: Record<string, string> = {};
    if (params.resourceId !== undefined) queryParams['resourceId'] = String(params.resourceId);
    if (params.type) queryParams['type'] = params.type;
    if (params.group) queryParams['group'] = params.group;
    if (params.scope) queryParams['scope'] = params.scope;
    if (params.page !== undefined) queryParams['page'] = String(params.page);
    if (params.size !== undefined) queryParams['size'] = String(params.size);
    return this.http.get<UIElementPage>(this.baseUrl, { params: queryParams });
  }

  getUIElementById(id: number): Observable<UIElementView> {
    return this.http.get<{ data: UIElementView }>(`${this.baseUrl}/${id}`).pipe(
      map(res => res.data),
    );
  }

  createUIElement(data: CreateUIElementRequest): Observable<{ id: number }> {
    return this.http.post<{ data: { id: number } }>(this.baseUrl, data).pipe(
      map(res => res.data),
    );
  }

  updateUIElement(id: number, data: UpdateUIElementRequest): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${id}`, data);
  }

  deleteUIElement(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  evaluateUIElements(request: EvaluateRequest): Observable<EvaluateResponse> {
    return this.http.post<{ data: EvaluateResponse }>(`${this.baseUrl}/evaluate`, request).pipe(
      map(res => res.data),
    );
  }

  getUncoveredUIElements(): Observable<UncoveredUIElement[]> {
    return this.http.get<{ data: UncoveredUIElement[] }>(`${this.baseUrl}/uncovered`).pipe(
      map(res => res.data),
    );
  }
}
