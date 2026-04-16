import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { UIElementService } from '../../../../core/services/ui-element.service';
import { ResourceService, ResourceSummaryView, ActionView } from '../../../../core/services/resource.service';

@Component({
  selector: 'app-create-ui-element-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
  ],
  templateUrl: './create-ui-element-dialog.html',
  styleUrl: './create-ui-element-dialog.css',
})
export class CreateUIElementDialogComponent implements OnInit {
  private uiElementService = inject(UIElementService);
  private resourceService  = inject(ResourceService);
  private dialogRef        = inject(MatDialogRef<CreateUIElementDialogComponent>);

  form = new FormGroup({
    elementId:    new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    label:        new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    type:         new FormControl<'BUTTON' | 'TAB' | 'MENU_ITEM'>('BUTTON', { nonNullable: true, validators: [Validators.required] }),
    elementGroup: new FormControl(''),
    orderIndex:   new FormControl(0, { nonNullable: true, validators: [Validators.required, Validators.min(0)] }),
    resourceId:   new FormControl<number | null>(null, Validators.required),
    actionId:     new FormControl<number | null>(null, Validators.required),
  });

  isSubmitting    = signal(false);
  elementIdError  = signal<string | null>(null);
  generalError    = signal<string | null>(null);
  resources       = signal<ResourceSummaryView[]>([]);
  availableActions = signal<ActionView[]>([]);

  readonly typeOptions: { value: 'BUTTON' | 'TAB' | 'MENU_ITEM'; label: string }[] = [
    { value: 'BUTTON', label: 'Button' },
    { value: 'TAB', label: 'Tab' },
    { value: 'MENU_ITEM', label: 'Menu Item' },
  ];

  ngOnInit(): void {
    this.resourceService.getResources({ size: 200 }).subscribe({
      next: result => this.resources.set(result.data),
    });
  }

  onResourceChange(resourceId: number | null): void {
    this.form.controls.actionId.reset(null);
    this.availableActions.set([]);
    if (resourceId == null) return;
    this.resourceService.getResourceById(resourceId).subscribe({
      next: r => this.availableActions.set(r.actions),
    });
  }

  submit(): void {
    if (this.form.invalid) return;
    this.isSubmitting.set(true);
    this.elementIdError.set(null);
    this.generalError.set(null);

    const v = this.form.getRawValue();
    this.uiElementService.createUIElement({
      elementId: v.elementId,
      label: v.label,
      type: v.type,
      group: v.elementGroup || null,
      orderIndex: v.orderIndex,
      resourceId: v.resourceId!,
      actionId: v.actionId!,
    }).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.dialogRef.close(true);
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting.set(false);
        const code = err.error?.error?.code;
        if (err.status === 409 && code === '30013') {
          this.elementIdError.set('Element ID already exists. Choose a different ID.');
        } else {
          this.generalError.set('An unexpected error occurred. Please try again.');
        }
      },
    });
  }

  cancel(): void {
    this.dialogRef.close(false);
  }
}
