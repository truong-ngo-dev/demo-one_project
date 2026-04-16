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
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ResourceService, ResourceView, ActionView } from '../../../../core/services/resource.service';
import { AddActionDialogComponent } from './add-action-dialog/add-action-dialog';

@Component({
  selector: 'app-resource-detail',
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
    MatSnackBarModule,
    MatTableModule,
    MatTooltipModule,
  ],
  templateUrl: './resource-detail.html',
  styleUrl: './resource-detail.css',
})
export class ResourceDetailComponent implements OnInit {
  private route           = inject(ActivatedRoute);
  private router          = inject(Router);
  private resourceService = inject(ResourceService);
  private dialog          = inject(MatDialog);
  private snackBar        = inject(MatSnackBar);

  resource    = signal<ResourceView | null>(null);
  isLoading   = signal(false);
  isSaving    = signal(false);
  saveError   = signal<string | null>(null);

  readonly actionColumns = ['name', 'description', 'isStandard', 'actions'];

  form = new FormGroup({
    description: new FormControl(''),
    serviceName: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadResource(id);
  }

  private loadResource(id: number): void {
    this.isLoading.set(true);
    this.resourceService.getResourceById(id).subscribe({
      next: r => {
        this.resource.set(r);
        this.form.patchValue({ description: r.description ?? '', serviceName: r.serviceName });
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.router.navigate(['/admin/abac/resources']);
      },
    });
  }

  saveResource(): void {
    if (this.form.invalid || !this.resource()) return;
    this.isSaving.set(true);
    this.saveError.set(null);

    const { description, serviceName } = this.form.getRawValue();
    this.resourceService.updateResource(this.resource()!.id, { description: description ?? undefined, serviceName }).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.snackBar.open('Resource updated.', 'Dismiss', { duration: 3000 });
        this.loadResource(this.resource()!.id);
      },
      error: () => {
        this.isSaving.set(false);
        this.saveError.set('Failed to save changes. Please try again.');
      },
    });
  }

  openAddActionDialog(): void {
    if (!this.resource()) return;
    this.dialog
      .open(AddActionDialogComponent, { width: '480px', data: { resourceId: this.resource()!.id } })
      .afterClosed()
      .subscribe(added => {
        if (added) this.loadResource(this.resource()!.id);
      });
  }

  deleteAction(action: ActionView): void {
    if (!this.resource()) return;
    if (!confirm(`Delete action "${action.name}"?`)) return;

    this.resourceService.removeAction(this.resource()!.id, action.id).subscribe({
      next: () => {
        this.snackBar.open(`Action "${action.name}" deleted.`, 'Dismiss', { duration: 3000 });
        this.loadResource(this.resource()!.id);
      },
      error: (err: HttpErrorResponse) => {
        const code = err.error?.error?.code;
        if (err.status === 409 && code === 'ACTION_IN_USE') {
          this.snackBar.open(
            `Cannot delete "${action.name}": action is used by UI elements.`,
            'Dismiss',
            { duration: 5000 },
          );
        } else {
          this.snackBar.open('Failed to delete action. Please try again.', 'Dismiss', { duration: 3000 });
        }
      },
    });
  }

  goBack(): void {
    this.router.navigate(['/admin/abac/resources']);
  }
}
