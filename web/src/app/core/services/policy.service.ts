import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export type CombineAlgorithmName =
  | 'DENY_OVERRIDES'
  | 'PERMIT_OVERRIDES'
  | 'DENY_UNLESS_PERMIT'
  | 'PERMIT_UNLESS_DENY'
  | 'FIRST_APPLICABLE'
  | 'ONLY_ONE_APPLICABLE';

export type ScopeType = 'OPERATOR' | 'TENANT' | 'ADMIN';
export type EffectType = 'PERMIT' | 'DENY';

// ── Expression System ──────────────────────────────────────────────────────────
export type ExpressionNodeType = 'INLINE' | 'LIBRARY_REF' | 'COMPOSITION';
export type CompositionOperator = 'AND' | 'OR';

export interface ExpressionNodeView {
  type: ExpressionNodeType;
  name: string | null;
  resolvedSpel: string | null;
  refId: number | null;
  operator: CompositionOperator | null;
  children: ExpressionNodeView[] | null;
}

export interface ExpressionNodeRequest {
  type: ExpressionNodeType;
  name?: string;
  spel?: string;
  refId?: number;
  operator?: CompositionOperator;
  children?: ExpressionNodeRequest[];
}

export interface NamedExpressionView {
  id: number;
  name: string;
  spel: string;
}

// ── PolicySet ─────────────────────────────────────────────────────────────────
export interface PolicySetSummary {
  id: number;
  name: string;
  scope: ScopeType;
  combineAlgorithm: CombineAlgorithmName;
  isRoot: boolean;
  tenantId: string | null;
  createdAt: number;
  updatedAt: number;
}

export interface PolicySetView extends PolicySetSummary {
  policies: PolicyNestedSummary[];
}

export interface PolicyNestedSummary {
  id: number;
  name: string;
  combineAlgorithm: CombineAlgorithmName;
}

export interface PolicySetPage {
  data: PolicySetSummary[];
  meta: { page: number; size: number; total: number };
}

export interface CreatePolicySetRequest {
  name: string;
  scope: ScopeType;
  combineAlgorithm: CombineAlgorithmName;
  isRoot: boolean;
  tenantId?: string;
}

export interface UpdatePolicySetRequest {
  scope: ScopeType;
  combineAlgorithm: CombineAlgorithmName;
  isRoot: boolean;
  tenantId?: string;
}

// ── Policy ────────────────────────────────────────────────────────────────────
export interface PolicySummary {
  id: number;
  name: string;
  combineAlgorithm: CombineAlgorithmName;
  targetExpression: string | null;   // resolved SpEL — from list endpoint
  createdAt: number;
  updatedAt: number;
}

export interface PolicyView {
  id: number;
  policySetId: number;
  name: string;
  combineAlgorithm: CombineAlgorithmName;
  targetExpression: ExpressionNodeView | null;
  createdAt: number;
  updatedAt: number;
  rules: RuleView[];
}

export interface CreatePolicyRequest {
  policySetId: number;
  name: string;
  targetExpression?: string;
  combineAlgorithm: CombineAlgorithmName;
}

export interface UpdatePolicyRequest {
  targetExpressionNode?: ExpressionNodeRequest;
  combineAlgorithm: CombineAlgorithmName;
}

// ── Rule ──────────────────────────────────────────────────────────────────────
export interface RuleView {
  id: number;
  name: string;
  description: string | null;
  targetExpression: ExpressionNodeView | null;
  conditionExpression: ExpressionNodeView | null;
  effect: EffectType;
  orderIndex: number;
}

export interface CreateRuleRequest {
  name: string;
  description?: string;
  targetExpression?: ExpressionNodeRequest;
  conditionExpression?: ExpressionNodeRequest;
  effect: EffectType;
  orderIndex: number;
}

export interface UpdateRuleRequest {
  name: string;
  description?: string;
  targetExpression?: ExpressionNodeRequest;
  conditionExpression?: ExpressionNodeRequest;
  effect: EffectType;
}

export interface ReorderRequest {
  ruleIds: number[];
}

export interface PolicySetDeletePreview {
  policyCount: number;
  ruleCount: number;
}

export interface PolicyDeletePreview {
  ruleCount: number;
}

// ── Service ───────────────────────────────────────────────────────────────────
@Injectable({ providedIn: 'root' })
export class PolicyService {
  private http = inject(HttpClient);
  private policySetBase = '/api/admin/v1/abac/policy-sets';
  private policyBase    = '/api/admin/v1/abac/policies';

  // PolicySet
  getPolicySets(params: { keyword?: string; page?: number; size?: number } = {}): Observable<PolicySetPage> {
    const q: Record<string, string> = {};
    if (params.keyword) q['keyword'] = params.keyword;
    if (params.page !== undefined) q['page'] = String(params.page);
    if (params.size !== undefined) q['size'] = String(params.size);
    return this.http.get<PolicySetPage>(this.policySetBase, { params: q });
  }

  getPolicySetById(id: number): Observable<PolicySetView> {
    return this.http.get<{ data: PolicySetView }>(`${this.policySetBase}/${id}`).pipe(
      map(res => res.data),
    );
  }

  createPolicySet(data: CreatePolicySetRequest): Observable<{ id: number }> {
    return this.http.post<{ data: { id: number } }>(this.policySetBase, data).pipe(
      map(res => res.data),
    );
  }

  updatePolicySet(id: number, data: UpdatePolicySetRequest): Observable<void> {
    return this.http.put<void>(`${this.policySetBase}/${id}`, data);
  }

  deletePolicySet(id: number): Observable<void> {
    return this.http.delete<void>(`${this.policySetBase}/${id}`);
  }

  getPolicySetDeletePreview(id: number): Observable<PolicySetDeletePreview> {
    return this.http.get<{ data: PolicySetDeletePreview }>(`${this.policySetBase}/${id}/delete-preview`).pipe(
      map(res => res.data),
    );
  }

  // Policy
  getPolicies(policySetId: number): Observable<PolicySummary[]> {
    return this.http.get<{ data: PolicySummary[] }>(this.policyBase, { params: { policySetId: String(policySetId) } }).pipe(
      map(res => res.data),
    );
  }

  getPolicyById(id: number): Observable<PolicyView> {
    return this.http.get<{ data: PolicyView }>(`${this.policyBase}/${id}`).pipe(
      map(res => res.data),
    );
  }

  createPolicy(data: CreatePolicyRequest): Observable<{ id: number }> {
    return this.http.post<{ data: { id: number } }>(this.policyBase, data).pipe(
      map(res => res.data),
    );
  }

  updatePolicy(id: number, data: UpdatePolicyRequest): Observable<void> {
    return this.http.put<void>(`${this.policyBase}/${id}`, data);
  }

  deletePolicy(id: number): Observable<void> {
    return this.http.delete<void>(`${this.policyBase}/${id}`);
  }

  getPolicyDeletePreview(id: number): Observable<PolicyDeletePreview> {
    return this.http.get<{ data: PolicyDeletePreview }>(`${this.policyBase}/${id}/delete-preview`).pipe(
      map(res => res.data),
    );
  }

  // Rule
  getRules(policyId: number): Observable<RuleView[]> {
    return this.http.get<{ data: RuleView[] }>(`${this.policyBase}/${policyId}/rules`).pipe(
      map(res => res.data),
    );
  }

  getRuleById(policyId: number, ruleId: number): Observable<RuleView> {
    return this.http.get<{ data: RuleView }>(`${this.policyBase}/${policyId}/rules/${ruleId}`).pipe(
      map(res => res.data),
    );
  }

  createRule(policyId: number, data: CreateRuleRequest): Observable<{ id: number }> {
    return this.http.post<{ data: { id: number } }>(`${this.policyBase}/${policyId}/rules`, data).pipe(
      map(res => res.data),
    );
  }

  updateRule(policyId: number, ruleId: number, data: UpdateRuleRequest): Observable<void> {
    return this.http.put<void>(`${this.policyBase}/${policyId}/rules/${ruleId}`, data);
  }

  deleteRule(policyId: number, ruleId: number): Observable<void> {
    return this.http.delete<void>(`${this.policyBase}/${policyId}/rules/${ruleId}`);
  }

  reorderRules(policyId: number, ruleIds: number[]): Observable<void> {
    return this.http.put<void>(`${this.policyBase}/${policyId}/rules/reorder`, { ruleIds });
  }
}
