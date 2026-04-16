import { Component, Inject, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { EffectType, ExpressionNodeRequest, RuleView } from '../../../../../core/services/policy.service';
import { ExpressionNodeEditorComponent } from '../expression-node-editor/expression-node-editor';

export interface EditRuleFormData {
  name: string;
  description?: string;
  effect: EffectType;
  targetExpression?: ExpressionNodeRequest;
  conditionExpression?: ExpressionNodeRequest;
}

@Component({
  selector: 'app-edit-rule-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    MatTooltipModule,
    ExpressionNodeEditorComponent,
  ],
  templateUrl: './edit-rule-dialog.html',
  styleUrl: './edit-rule-dialog.css',
})
export class EditRuleDialogComponent {
  private dialogRef = inject(MatDialogRef<EditRuleDialogComponent>);

  constructor(@Inject(MAT_DIALOG_DATA) public data: { policyId: number; rule: RuleView }) {
    this.form.patchValue({
      name:        data.rule.name,
      description: data.rule.description ?? '',
      effect:      data.rule.effect,
    });
  }

  readonly effects: EffectType[] = ['PERMIT', 'DENY'];

  form = new FormGroup({
    name:        new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    description: new FormControl(''),
    effect:      new FormControl<EffectType>('PERMIT', { nonNullable: true, validators: [Validators.required] }),
  });

  targetExpression    = signal<ExpressionNodeRequest | null>(null);
  conditionExpression = signal<ExpressionNodeRequest | null>(null);

  submit(): void {
    if (this.form.invalid) return;
    const { name, description, effect } = this.form.getRawValue();
    this.dialogRef.close({
      name,
      description:         description || undefined,
      effect,
      targetExpression:    this.targetExpression()    ?? undefined,
      conditionExpression: this.conditionExpression() ?? undefined,
    } satisfies EditRuleFormData);
  }

  cancel(): void { this.dialogRef.close(null); }
}
