import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AuditLogEntry {
  id: number;
  entityType: 'POLICY_SET' | 'POLICY' | 'RULE' | 'UI_ELEMENT';
  entityId: number;
  entityName: string | null;
  actionType: 'CREATED' | 'UPDATED' | 'DELETED';
  performedBy: string | null;
  changedAt: number;
  snapshotJson: string | null;
}

export interface AuditLogParams {
  entityType?: string;
  entityId?: number;
  performedBy?: string;
  page?: number;
  size?: number;
}

export interface AuditLogPage {
  data: AuditLogEntry[];
  meta: { page: number; size: number; total: number };
}

@Injectable({ providedIn: 'root' })
export class AuditLogService {
  private http = inject(HttpClient);
  private baseUrl = '/api/admin/v1/abac/audit-log';

  getAuditLog(params: AuditLogParams = {}): Observable<AuditLogPage> {
    const queryParams: Record<string, string> = {};
    if (params.entityType) queryParams['entityType'] = params.entityType;
    if (params.entityId !== undefined) queryParams['entityId'] = String(params.entityId);
    if (params.performedBy) queryParams['performedBy'] = params.performedBy;
    if (params.page !== undefined) queryParams['page'] = String(params.page);
    if (params.size !== undefined) queryParams['size'] = String(params.size);
    return this.http.get<AuditLogPage>(this.baseUrl, { params: queryParams });
  }
}
