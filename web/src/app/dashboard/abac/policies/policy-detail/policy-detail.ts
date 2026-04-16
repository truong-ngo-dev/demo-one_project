import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
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
  PolicyService, PolicyView, RuleView, CombineAlgorithmName, EffectType, ExpressionNodeRequest,
} from '../../../../core/services/policy.service';
import { SimulateService } from '../../../../core/services/simulate.service';
import { CreateRuleDialogComponent, CreateRuleFormData } from './create-rule-dialog/create-rule-dialog';
import { EditRuleDialogComponent, EditRuleFormData } from './edit-rule-dialog/edit-rule-dialog';
import { RuleImpactDialogComponent } from './rule-impact-dialog/rule-impact-dialog';

@Component({
  selector: 'app-policy-detail',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
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
  templateUrl: './policy-detail.html',
  styleUrl: './policy-detail.css',
})
export class PolicyDetailComponent implements OnInit {
  private route           = inject(ActivatedRoute);
  private router          = inject(Router);
  private policyService   = inject(PolicyService);
  private simulateService = inject(SimulateService);
  private dialog          = inject(MatDialog);
  private snackBar        = inject(MatSnackBar);

  policy        = signal<PolicyView | null>(null);
  policySetName = signal<string>('...');
  isLoading     = signal(false);
  isSaving      = signal(false);
  saveError     = signal<string | null>(null);

  readonly ruleColumns = ['order', 'name', 'effect', 'actions'];

  readonly algorithms: CombineAlgorithmName[] = [
    'DENY_OVERRIDES', 'PERMIT_OVERRIDES', 'DENY_UNLESS_PERMIT',
    'PERMIT_UNLESS_DENY', 'FIRST_APPLICABLE', 'ONLY_ONE_APPLICABLE',
  ];

  form = new FormGroup({
    combineAlgorithm: new FormControl<CombineAlgorithmName>('DENY_UNLESS_PERMIT', { nonNullable: true, validators: [Validators.required] }),
    targetExpression: new FormControl(''),
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.load(id);
  }

  private load(id: number): void {
    this.isLoading.set(true);
    this.policyService.getPolicyById(id).subscribe({
      next: p => {
        this.policy.set(p);
        this.form.patchValue({
          combineAlgorithm: p.combineAlgorithm,
          targetExpression: p.targetExpression?.resolvedSpel ?? '',
        });
        this.isLoading.set(false);
        this.policyService.getPolicySetById(p.policySetId).subscribe({
          next: ps => this.policySetName.set(ps.name),
          error: () => {},
        });
      },
      error: () => { this.isLoading.set(false); this.router.navigate(['/admin/abac/policy-sets']); },
    });
  }

  save(): void {
    if (this.form.invalid || !this.policy()) return;
    this.isSaving.set(true);
    this.saveError.set(null);

    const { combineAlgorithm, targetExpression } = this.form.getRawValue();
    const targetExpressionNode: ExpressionNodeRequest | undefined = targetExpression?.trim()
      ? { type: 'INLINE', spel: targetExpression.trim() }
      : undefined;
    this.policyService.updatePolicy(this.policy()!.id, {
      combineAlgorithm,
      targetExpressionNode,
    }).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.snackBar.open('Policy updated.', 'Dismiss', { duration: 3000 });
        this.load(this.policy()!.id);
      },
      error: (err: HttpErrorResponse) => {
        this.isSaving.set(false);
        const code = err.error?.error?.code;
        this.saveError.set(code === '30011'
          ? 'Invalid SpEL expression syntax.'
          : 'Failed to save changes.');
      },
    });
  }

  // ── Rule dialogs ───────────────────────────────────────────────────────────

  openAddRuleDialog(): void {
    if (!this.policy()) return;
    const orderIndex = this.policy()!.rules.length;
    this.dialog.open(CreateRuleDialogComponent, {
      width: '600px',
      data: { policyId: this.policy()!.id, orderIndex },
    }).afterClosed().subscribe((result: CreateRuleFormData | null | undefined) => {
      if (!result) return;
      this.previewAndSave(result, 'create');
    });
  }

  openEditRuleDialog(rule: RuleView): void {
    if (!this.policy()) return;
    this.dialog.open(EditRuleDialogComponent, {
      width: '600px',
      data: { policyId: this.policy()!.id, rule },
    }).afterClosed().subscribe((result: EditRuleFormData | null | undefined) => {
      if (!result) return;
      this.previewAndSave(result, 'edit', rule.id);
    });
  }

  // ── Impact preview flow ────────────────────────────────────────────────────

  private previewAndSave(
    result: CreateRuleFormData | EditRuleFormData,
    mode: 'create' | 'edit',
    ruleId?: number,
  ): void {
    this.simulateService.getImpactPreview({
      targetExpression:    inlineSpel(result.targetExpression),
      conditionExpression: inlineSpel(result.conditionExpression),
    }).subscribe({
      next: impact => {
        this.dialog.open(RuleImpactDialogComponent, {
          width: '480px',
          data: { impact },
        }).afterClosed().subscribe(confirmed => {
          if (confirmed) this.doSave(result, mode, ruleId);
        });
      },
      error: () => this.doSave(result, mode, ruleId), // fallback: skip preview, proceed
    });
  }

  private doSave(
    result: CreateRuleFormData | EditRuleFormData,
    mode: 'create' | 'edit',
    ruleId?: number,
  ): void {
    const policyId = this.policy()!.id;

    if (mode === 'create') {
      const createData = result as CreateRuleFormData;
      this.policyService.createRule(policyId, {
        name:                createData.name,
        description:         createData.description,
        effect:              createData.effect as EffectType,
        targetExpression:    createData.targetExpression,
        conditionExpression: createData.conditionExpression,
        orderIndex:          createData.orderIndex,
      }).subscribe({
        next: () => {
          this.snackBar.open('Rule created.', 'Dismiss', { duration: 3000 });
          this.load(policyId);
        },
        error: (err: HttpErrorResponse) => {
          const code = err.error?.error?.code;
          this.snackBar.open(
            code === '30011' ? 'Invalid SpEL expression syntax.' : 'Failed to create rule.',
            'Dismiss', { duration: 4000 },
          );
        },
      });
    } else {
      this.policyService.updateRule(policyId, ruleId!, {
        name:                result.name,
        description:         result.description,
        effect:              result.effect as EffectType,
        targetExpression:    result.targetExpression,
        conditionExpression: result.conditionExpression,
      }).subscribe({
        next: () => {
          this.snackBar.open('Rule updated.', 'Dismiss', { duration: 3000 });
          this.load(policyId);
        },
        error: (err: HttpErrorResponse) => {
          const code = err.error?.error?.code;
          this.snackBar.open(
            code === '30011' ? 'Invalid SpEL expression syntax.' : 'Failed to update rule.',
            'Dismiss', { duration: 4000 },
          );
        },
      });
    }
  }

  // ── Rule table actions ─────────────────────────────────────────────────────

  deleteRule(rule: RuleView): void {
    if (!this.policy()) return;
    if (!confirm(`Delete rule "${rule.name}"?`)) return;
    this.policyService.deleteRule(this.policy()!.id, rule.id).subscribe({
      next: () => {
        this.snackBar.open(`Rule "${rule.name}" deleted.`, 'Dismiss', { duration: 3000 });
        this.load(this.policy()!.id);
      },
      error: () => this.snackBar.open('Failed to delete rule.', 'Dismiss', { duration: 3000 }),
    });
  }

  moveUp(rule: RuleView): void {
    const rules = [...this.policy()!.rules].sort((a, b) => a.orderIndex - b.orderIndex);
    const idx = rules.findIndex(r => r.id === rule.id);
    if (idx <= 0) return;
    [rules[idx - 1], rules[idx]] = [rules[idx], rules[idx - 1]];
    this.reorder(rules);
  }

  moveDown(rule: RuleView): void {
    const rules = [...this.policy()!.rules].sort((a, b) => a.orderIndex - b.orderIndex);
    const idx = rules.findIndex(r => r.id === rule.id);
    if (idx < 0 || idx >= rules.length - 1) return;
    [rules[idx], rules[idx + 1]] = [rules[idx + 1], rules[idx]];
    this.reorder(rules);
  }

  private reorder(ordered: RuleView[]): void {
    const ruleIds = ordered.map(r => r.id);
    this.policyService.reorderRules(this.policy()!.id, ruleIds).subscribe({
      next: () => this.load(this.policy()!.id),
      error: () => this.snackBar.open('Failed to reorder rules.', 'Dismiss', { duration: 3000 }),
    });
  }

  goToPolicySet(): void {
    if (this.policy()) this.router.navigate(['/admin/abac/policy-sets', this.policy()!.policySetId]);
  }

  goBack(): void {
    this.router.navigate(['/admin/abac/policy-sets']);
  }

  sortedRules(): RuleView[] {
    if (!this.policy()) return [];
    return [...this.policy()!.rules].sort((a, b) => a.orderIndex - b.orderIndex);
  }
}

/** Extracts a plain SpEL string from an ExpressionNodeRequest for impact-preview (best-effort). */
function inlineSpel(node: ExpressionNodeRequest | null | undefined): string | undefined {
  if (!node) return undefined;
  if (node.type === 'INLINE') return node.spel;
  return undefined;
}
