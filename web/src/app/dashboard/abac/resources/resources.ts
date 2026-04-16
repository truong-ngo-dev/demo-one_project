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
import { ResourceService, ResourceSummaryView } from '../../../core/services/resource.service';
import { CreateResourceDialogComponent } from './create-resource-dialog/create-resource-dialog';

@Component({
  selector: 'app-resources',
  standalone: true,
  imports: [
    FormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTableModule,
    MatTooltipModule,
  ],
  templateUrl: './resources.html',
  styleUrl: './resources.css',
})
export class ResourcesComponent implements OnInit, OnDestroy {
  private resourceService = inject(ResourceService);
  private router          = inject(Router);
  private dialog          = inject(MatDialog);
  private snackBar        = inject(MatSnackBar);
  private destroy$        = new Subject<void>();
  private keywordSubject  = new Subject<string>();

  resources  = signal<ResourceSummaryView[]>([]);
  isLoading  = signal(false);
  keyword    = signal('');

  readonly displayedColumns = ['name', 'serviceName', 'actionCount', 'actions'];

  ngOnInit(): void {
    this.keywordSubject.pipe(debounceTime(300), takeUntil(this.destroy$)).subscribe(kw => {
      this.loadResources(kw);
    });
    this.loadResources();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onKeywordChange(value: string): void {
    this.keyword.set(value);
    this.keywordSubject.next(value);
  }

  loadResources(kw?: string): void {
    this.isLoading.set(true);
    this.resourceService.getResources({ keyword: (kw ?? this.keyword()) || undefined }).subscribe({
      next: result => {
        this.resources.set(result.data);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  openCreateDialog(): void {
    this.dialog
      .open(CreateResourceDialogComponent, { width: '520px' })
      .afterClosed()
      .subscribe(created => {
        if (created) this.loadResources();
      });
  }

  goToDetail(resource: ResourceSummaryView): void {
    this.router.navigate(['/admin/abac/resources', resource.id]);
  }

  deleteResource(resource: ResourceSummaryView): void {
    if (!confirm(`Delete resource "${resource.name}"? This cannot be undone.`)) return;

    this.resourceService.deleteResource(resource.id).subscribe({
      next: () => {
        this.snackBar.open(`Resource "${resource.name}" deleted.`, 'Dismiss', { duration: 3000 });
        this.loadResources();
      },
      error: (err: HttpErrorResponse) => {
        const code = err.error?.error?.code;
        if (err.status === 409 && code === 'RESOURCE_IN_USE') {
          this.snackBar.open(
            `Cannot delete "${resource.name}": resource is used by policies or UI elements.`,
            'Dismiss',
            { duration: 5000 },
          );
        } else {
          this.snackBar.open('Failed to delete resource. Please try again.', 'Dismiss', { duration: 3000 });
        }
      },
    });
  }
}
