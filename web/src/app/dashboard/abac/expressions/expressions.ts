import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ExpressionService } from '../../../core/services/expression.service';
import { NamedExpressionView } from '../../../core/services/policy.service';

@Component({
  selector: 'app-expressions',
  standalone: true,
  imports: [
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTableModule,
    MatTooltipModule,
  ],
  templateUrl: './expressions.html',
  styleUrl: './expressions.css',
})
export class ExpressionsComponent implements OnInit {
  private expressionService = inject(ExpressionService);
  private snackBar          = inject(MatSnackBar);

  expressions = signal<NamedExpressionView[]>([]);
  isLoading   = signal(false);

  readonly columns = ['name', 'spel', 'actions'];

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.isLoading.set(true);
    this.expressionService.getNamedExpressions().subscribe({
      next: list => { this.expressions.set(list); this.isLoading.set(false); },
      error: () => { this.isLoading.set(false); },
    });
  }

  delete(expr: NamedExpressionView): void {
    if (!confirm(`Delete named expression "${expr.name}"?`)) return;
    this.expressionService.deleteNamedExpression(expr.id).subscribe({
      next: () => {
        this.snackBar.open(`"${expr.name}" deleted.`, 'Dismiss', { duration: 3000 });
        this.load();
      },
      error: (err: HttpErrorResponse) => {
        const code = err.error?.error?.code;
        this.snackBar.open(
          code === '30014' ? 'Expression is still referenced by a rule and cannot be deleted.' : 'Failed to delete.',
          'Dismiss', { duration: 4000 },
        );
      },
    });
  }
}
