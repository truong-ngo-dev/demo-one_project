import { Injectable, inject, signal } from '@angular/core';
import { Observable, map, tap } from 'rxjs';
import { UIElementService } from './ui-element.service';

export const ADMIN_ROUTE_ELEMENT_IDS = [
  'route:users',
  'route:roles',
  'route:abac:resources',
  'route:abac:policy-sets',
  'route:abac:ui-elements',
  'route:abac:simulator',
  'route:abac:audit-log',
  'route:active-sessions',
  'route:login-activities',
  'btn:user:lock',
] as const;

@Injectable({ providedIn: 'root' })
export class AbacService {
  private uiElementService = inject(UIElementService);

  private visibilityMap = signal<Record<string, 'PERMIT' | 'DENY'> | null>(null);

  loadVisibility(elementIds: string[]): Observable<void> {
    return this.uiElementService.evaluateUIElements({ elementIds }).pipe(
      tap(res => this.visibilityMap.set(res.results)),
      map(() => void 0),
    );
  }

  isPermitted(elementId: string): boolean {
    const map = this.visibilityMap();
    if (map === null) return false;
    return map[elementId] === 'PERMIT';
  }

  clearVisibility(): void {
    this.visibilityMap.set(null);
  }
}
