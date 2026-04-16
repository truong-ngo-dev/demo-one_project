import { Component, Inject, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { EffectType, ExpressionNodeRequest } from '../../../../../core/services/policy.service';
import { ExpressionNodeEditorComponent } from '../expression-node-editor/expression-node-editor';

export interface CreateRuleFormData {
  name: string;
  description?: string;
  effect: EffectType;
  targetExpression?: ExpressionNodeRequest;
  conditionExpression?: ExpressionNodeRequest;
  orderIndex: number;
}

@Component({
  selector: 'app-create-rule-dialog',
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
  templateUrl: './create-rule-dialog.html',
  styleUrl: './create-rule-dialog.css',
})
export class CreateRuleDialogComponent {
  private dialogRef = inject(MatDialogRef<CreateRuleDialogComponent>);

  constructor(@Inject(MAT_DIALOG_DATA) public data: { policyId: number; orderIndex: number }) {}

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
      orderIndex:          this.data.orderIndex,
    } satisfies CreateRuleFormData);
  }

  cancel(): void { this.dialogRef.close(null); }
}
