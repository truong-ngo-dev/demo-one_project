import { Component, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { UIElementService, UIElementSummary, UncoveredUIElement } from '../../../core/services/ui-element.service';
import { ResourceService, ResourceSummaryView } from '../../../core/services/resource.service';
import { CreateUIElementDialogComponent } from './create-ui-element-dialog/create-ui-element-dialog';

@Component({
  selector: 'app-ui-elements',
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
    MatSelectModule,
    MatSnackBarModule,
    MatTableModule,
    MatTooltipModule,
  ],
  templateUrl: './ui-elements.html',
  styleUrl: './ui-elements.css',
})
export class UIElementsComponent implements OnInit {
  private uiElementService = inject(UIElementService);
  private resourceService  = inject(ResourceService);
  private router           = inject(Router);
  private dialog           = inject(MatDialog);
  private snackBar         = inject(MatSnackBar);

  elements         = signal<UIElementSummary[]>([]);
  resources        = signal<ResourceSummaryView[]>([]);
  isLoading        = signal(false);
  selectedResource = signal<number | null>(null);
  selectedType     = signal<string>('');
  uncoveredCount   = signal(0);
  showUncoveredOnly = signal(false);

  readonly displayedColumns = ['elementId', 'label', 'type', 'resourceName', 'actionName', 'coverage', 'actions'];
  readonly typeOptions = [
    { value: '', label: 'All Types' },
    { value: 'BUTTON', label: 'Button' },
    { value: 'TAB', label: 'Tab' },
    { value: 'MENU_ITEM', label: 'Menu Item' },
  ];

  ngOnInit(): void {
    this.loadResources();
    this.loadElements();
    this.loadUncoveredCount();
  }

  loadResources(): void {
    this.resourceService.getResources({ size: 200 }).subscribe({
      next: result => this.resources.set(result.data),
    });
  }

  loadElements(): void {
    this.isLoading.set(true);
    this.uiElementService.getUIElements({
      resourceId: this.selectedResource() ?? undefined,
      type: this.selectedType() || undefined,
    }).subscribe({
      next: result => {
        let data = result.data;
        if (this.showUncoveredOnly()) {
          data = data.filter(e => !e.hasPolicyCoverage);
        }
        this.elements.set(data);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  loadUncoveredCount(): void {
    this.uiElementService.getUncoveredUIElements().subscribe({
      next: items => this.uncoveredCount.set(items.length),
      error: () => {},
    });
  }

  onFilterChange(): void {
    this.loadElements();
  }

  filterUncovered(): void {
    this.showUncoveredOnly.set(true);
    this.loadElements();
  }

  openCreateDialog(): void {
    this.dialog
      .open(CreateUIElementDialogComponent, { width: '560px' })
      .afterClosed()
      .subscribe(created => {
        if (created) this.loadElements();
      });
  }

  goToDetail(element: UIElementSummary): void {
    this.router.navigate(['/admin/abac/ui-elements', element.id]);
  }

  deleteElement(element: UIElementSummary): void {
    if (!confirm(`Delete UI element "${element.elementId}"? This cannot be undone.`)) return;

    this.uiElementService.deleteUIElement(element.id).subscribe({
      next: () => {
        this.snackBar.open(`UI element "${element.elementId}" deleted.`, 'Dismiss', { duration: 3000 });
        this.loadElements();
      },
      error: (err: HttpErrorResponse) => {
        const code = err.error?.error?.code;
        if (err.status === 404 && code === '30012') {
          this.snackBar.open('UI element not found.', 'Dismiss', { duration: 3000 });
        } else {
          this.snackBar.open('Failed to delete UI element. Please try again.', 'Dismiss', { duration: 3000 });
        }
      },
    });
  }
}
