import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface SimulateSubjectRequest {
  userId?: string | null;
  roles: string[];
  attributes?: Record<string, unknown>;
}

export interface SimulateResourceRequest {
  name: string;
  data?: unknown;
}

export interface SimulateRequest {
  subject: SimulateSubjectRequest;
  resource: SimulateResourceRequest;
  action: string;
  policySetId?: number | null;
}

export interface RuleTraceEntry {
  ruleId: string;
  ruleDescription: string | null;
  effect: 'PERMIT' | 'DENY';
  targetMatched: boolean;
  conditionMatched: boolean | null;
  wasDeciding: boolean;
}

export interface SimulateResponse {
  decision: 'PERMIT' | 'DENY';
  timestamp: number;
  policySetId: number | null;
  policySetName: string | null;
  details: unknown;
  trace: RuleTraceEntry[];
}

export interface NavigationSimulateRequest {
  subject: SimulateSubjectRequest;
  resourceName: string;
  policySetId?: number | null;
}

export interface NavigationActionDecision {
  action: string;
  decision: 'PERMIT' | 'DENY';
  matchedRuleName: string | null;
}

export interface NavigationSimulateResult {
  resourceName: string;
  policySetId: number | null;
  policySetName: string | null;
  decisions: NavigationActionDecision[];
}

export interface ImpactPreviewRequest {
  targetExpression?: string;
  conditionExpression?: string;
}

export interface ImpactPreviewResult {
  requiredRoles: string[];
  requiredAttributes: string[];
  specificActions: string[];
  navigableWithoutData: boolean;
  hasInstanceCondition: boolean;
  parseWarning: string | null;
}

export interface RuleCoverage {
  ruleId: number | null;
  ruleName: string;
  policyName: string;
  effect: 'PERMIT' | 'DENY';
  requiredRoles: string[];
  requiredAttributes: string[];
  hasInstanceCondition: boolean;
  userCountByRole: number | null;
  userCountNote: string | null;
}

export interface ReverseLookupResult {
  resourceName: string;
  actionName: string;
  permitRules: RuleCoverage[];
  denyRules: RuleCoverage[];
}

@Injectable({ providedIn: 'root' })
export class SimulateService {
  private http = inject(HttpClient);
  private baseUrl = '/api/admin/v1/abac/simulate';
  private rulesUrl = '/api/admin/v1/abac/rules';

  simulate(request: SimulateRequest): Observable<SimulateResponse> {
    return this.http.post<{ data: SimulateResponse }>(this.baseUrl, request).pipe(
      map(res => res.data),
    );
  }

  simulateNavigation(request: NavigationSimulateRequest): Observable<NavigationSimulateResult> {
    return this.http.post<{ data: NavigationSimulateResult }>(`${this.baseUrl}/navigation`, request).pipe(
      map(res => res.data),
    );
  }

  getImpactPreview(request: ImpactPreviewRequest): Observable<ImpactPreviewResult> {
    return this.http.post<{ data: ImpactPreviewResult }>(`${this.rulesUrl}/impact-preview`, request).pipe(
      map(res => res.data),
    );
  }

  getReverseLookup(resourceName: string, actionName: string, policySetId?: number): Observable<ReverseLookupResult> {
    const params: Record<string, string | number> = { resourceName, actionName };
    if (policySetId != null) params['policySetId'] = policySetId;
    return this.http.get<{ data: ReverseLookupResult }>(`${this.baseUrl}/reverse`, { params }).pipe(
      map(res => res.data),
    );
  }
}
