import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
  PolicyService, PolicySetView, PolicyNestedSummary,
  CombineAlgorithmName, ScopeType,
} from '../../../../core/services/policy.service';
import { CreatePolicyDialogComponent } from './create-policy-dialog/create-policy-dialog';

@Component({
  selector: 'app-policy-set-detail',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatChipsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatSnackBarModule,
    MatTableModule,
    MatTooltipModule,
  ],
  templateUrl: './policy-set-detail.html',
  styleUrl: './policy-set-detail.css',
})
export class PolicySetDetailComponent implements OnInit {
  private route         = inject(ActivatedRoute);
  private router        = inject(Router);
  private policyService = inject(PolicyService);
  private dialog        = inject(MatDialog);
  private snackBar      = inject(MatSnackBar);

  policySet  = signal<PolicySetView | null>(null);
  isLoading  = signal(false);
  isSaving   = signal(false);
  saveError  = signal<string | null>(null);

  readonly displayedColumns = ['name', 'combineAlgorithm', 'actions'];

  readonly algorithms: CombineAlgorithmName[] = [
    'DENY_OVERRIDES', 'PERMIT_OVERRIDES', 'DENY_UNLESS_PERMIT',
    'PERMIT_UNLESS_DENY', 'FIRST_APPLICABLE', 'ONLY_ONE_APPLICABLE',
  ];

  readonly scopes: ScopeType[] = ['OPERATOR', 'TENANT'];

  form = new FormGroup({
    scope:            new FormControl<ScopeType>('OPERATOR', { nonNullable: true, validators: [Validators.required] }),
    combineAlgorithm: new FormControl<CombineAlgorithmName>('DENY_OVERRIDES', { nonNullable: true, validators: [Validators.required] }),
    isRoot:           new FormControl(false, { nonNullable: true }),
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.load(id);
  }

  private load(id: number): void {
    this.isLoading.set(true);
    this.policyService.getPolicySetById(id).subscribe({
      next: ps => {
        this.policySet.set(ps);
        this.form.patchValue({ scope: ps.scope, combineAlgorithm: ps.combineAlgorithm, isRoot: ps.isRoot });
        this.isLoading.set(false);
      },
      error: () => { this.isLoading.set(false); this.router.navigate(['/admin/abac/policy-sets']); },
    });
  }

  save(): void {
    if (this.form.invalid || !this.policySet()) return;
    this.isSaving.set(true);
    this.saveError.set(null);

    const { scope, combineAlgorithm, isRoot } = this.form.getRawValue();
    this.policyService.updatePolicySet(this.policySet()!.id, { scope, combineAlgorithm, isRoot }).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.snackBar.open('Policy set updated.', 'Dismiss', { duration: 3000 });
        this.load(this.policySet()!.id);
      },
      error: () => { this.isSaving.set(false); this.saveError.set('Failed to save changes.'); },
    });
  }

  openAddPolicyDialog(): void {
    if (!this.policySet()) return;
    this.dialog.open(CreatePolicyDialogComponent, {
      width: '560px',
      data: { policySetId: this.policySet()!.id },
    }).afterClosed().subscribe(created => { if (created) this.load(this.policySet()!.id); });
  }

  goToPolicy(p: PolicyNestedSummary): void {
    this.router.navigate(['/admin/abac/policies', p.id]);
  }

  deletePolicy(p: PolicyNestedSummary, event: Event): void {
    event.stopPropagation();
    this.policyService.getPolicyDeletePreview(p.id).subscribe({
      next: preview => {
        const msg = preview.ruleCount > 0
          ? `Delete "${p.name}"? This will also delete ${preview.ruleCount} rules.`
          : `Delete policy "${p.name}"?`;
        if (!confirm(msg)) return;
        this.policyService.deletePolicy(p.id).subscribe({
          next: () => {
            this.snackBar.open(`Policy "${p.name}" deleted.`, 'Dismiss', { duration: 3000 });
            this.load(this.policySet()!.id);
          },
          error: () => this.snackBar.open('Failed to delete policy.', 'Dismiss', { duration: 3000 }),
        });
      },
      error: () => this.snackBar.open('Failed to load delete preview.', 'Dismiss', { duration: 3000 }),
    });
  }

  goBack(): void {
    this.router.navigate(['/admin/abac/policy-sets']);
  }
}
