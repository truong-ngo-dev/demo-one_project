import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, debounceTime, takeUntil } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { PolicyService, PolicySetSummary } from '../../../core/services/policy.service';
import { CreatePolicySetDialogComponent } from './create-policy-set-dialog/create-policy-set-dialog';

@Component({
  selector: 'app-policy-sets',
  standalone: true,
  imports: [
    FormsModule,
    MatButtonModule,
    MatChipsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTableModule,
    MatTooltipModule,
  ],
  templateUrl: './policy-sets.html',
  styleUrl: './policy-sets.css',
})
export class PolicySetsComponent implements OnInit, OnDestroy {
  private policyService = inject(PolicyService);
  private router        = inject(Router);
  private dialog        = inject(MatDialog);
  private snackBar      = inject(MatSnackBar);
  private destroy$      = new Subject<void>();
  private kw$           = new Subject<string>();

  policySets = signal<PolicySetSummary[]>([]);
  isLoading  = signal(false);
  keyword    = signal('');

  readonly displayedColumns = ['name', 'scope', 'combineAlgorithm', 'actions'];

  ngOnInit(): void {
    this.kw$.pipe(debounceTime(300), takeUntil(this.destroy$)).subscribe(kw => this.load(kw));
    this.load();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onKeywordChange(value: string): void {
    this.keyword.set(value);
    this.kw$.next(value);
  }

  load(kw?: string): void {
    this.isLoading.set(true);
    this.policyService.getPolicySets({ keyword: (kw ?? this.keyword()) || undefined }).subscribe({
      next: res => { this.policySets.set(res.data); this.isLoading.set(false); },
      error: () => this.isLoading.set(false),
    });
  }

  openCreateDialog(): void {
    this.dialog.open(CreatePolicySetDialogComponent, { width: '520px' })
      .afterClosed().subscribe(created => { if (created) this.load(); });
  }

  goToDetail(ps: PolicySetSummary): void {
    this.router.navigate(['/admin/abac/policy-sets', ps.id]);
  }

  delete(ps: PolicySetSummary): void {
    this.policyService.getPolicySetDeletePreview(ps.id).subscribe({
      next: preview => {
        const msg = preview.policyCount > 0
          ? `Delete "${ps.name}"? This will also delete ${preview.policyCount} policies and ${preview.ruleCount} rules.`
          : `Delete policy set "${ps.name}"?`;
        if (!confirm(msg)) return;
        this.policyService.deletePolicySet(ps.id).subscribe({
          next: () => {
            this.snackBar.open(`Policy set "${ps.name}" deleted.`, 'Dismiss', { duration: 3000 });
            this.load();
          },
          error: () => this.snackBar.open('Failed to delete. Please try again.', 'Dismiss', { duration: 3000 }),
        });
      },
      error: () => this.snackBar.open('Failed to load delete preview.', 'Dismiss', { duration: 3000 }),
    });
  }
}
