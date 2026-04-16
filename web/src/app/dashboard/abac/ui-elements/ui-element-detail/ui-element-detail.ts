import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { UIElementService, UIElementView } from '../../../../core/services/ui-element.service';

@Component({
  selector: 'app-ui-element-detail',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatSnackBarModule,
    MatTooltipModule
  ],
  templateUrl: './ui-element-detail.html',
  styleUrl: './ui-element-detail.css',
})
export class UIElementDetailComponent implements OnInit {
  private route            = inject(ActivatedRoute);
  private router           = inject(Router);
  private uiElementService = inject(UIElementService);
  private snackBar         = inject(MatSnackBar);

  element   = signal<UIElementView | null>(null);
  isLoading = signal(false);
  isSaving  = signal(false);
  saveError = signal<string | null>(null);

  readonly typeOptions: { value: 'BUTTON' | 'TAB' | 'MENU_ITEM'; label: string }[] = [
    { value: 'BUTTON', label: 'Button' },
    { value: 'TAB', label: 'Tab' },
    { value: 'MENU_ITEM', label: 'Menu Item' },
  ];

  form = new FormGroup({
    label:        new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    type:         new FormControl<'BUTTON' | 'TAB' | 'MENU_ITEM'>('BUTTON', { nonNullable: true, validators: [Validators.required] }),
    elementGroup: new FormControl(''),
    orderIndex:   new FormControl(0, { nonNullable: true, validators: [Validators.required, Validators.min(0)] }),
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadElement(id);
  }

  private loadElement(id: number): void {
    this.isLoading.set(true);
    this.uiElementService.getUIElementById(id).subscribe({
      next: e => {
        this.element.set(e);
        this.form.patchValue({
          label: e.label,
          type: e.type,
          elementGroup: e.elementGroup ?? '',
          orderIndex: e.orderIndex,
        });
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.router.navigate(['/admin/abac/ui-elements']);
      },
    });
  }

  save(): void {
    if (this.form.invalid || !this.element()) return;
    this.isSaving.set(true);
    this.saveError.set(null);

    const el = this.element()!;
    const v = this.form.getRawValue();
    this.uiElementService.updateUIElement(el.id, {
      label: v.label,
      type: v.type,
      group: v.elementGroup || null,
      orderIndex: v.orderIndex,
      resourceId: el.resourceId,
      actionId: el.actionId,
    }).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.snackBar.open('UI element updated.', 'Dismiss', { duration: 3000 });
        this.loadElement(el.id);
      },
      error: (err: HttpErrorResponse) => {
        this.isSaving.set(false);
        this.saveError.set('Failed to save changes. Please try again.');
      },
    });
  }

  goBack(): void {
    this.router.navigate(['/admin/abac/ui-elements']);
  }
}
