import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { NamedExpressionView } from './policy.service';

@Injectable({ providedIn: 'root' })
export class ExpressionService {
  private http = inject(HttpClient);
  private base = '/api/admin/v1/abac/expressions';

  getNamedExpressions(): Observable<NamedExpressionView[]> {
    return this.http.get<{ data: { items: NamedExpressionView[] } }>(this.base).pipe(
      map(res => res.data.items),
    );
  }

  deleteNamedExpression(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
