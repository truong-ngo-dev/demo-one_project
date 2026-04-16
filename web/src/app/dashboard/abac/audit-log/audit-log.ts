import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuditLogService, AuditLogEntry } from '../../../core/services/audit-log.service';
import { SnapshotDialogComponent } from './snapshot-dialog/snapshot-dialog';

@Component({
  selector: 'app-audit-log',
  standalone: true,
  imports: [
    DatePipe,
    FormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatTableModule,
    MatTooltipModule,
  ],
  templateUrl: './audit-log.html',
  styleUrl: './audit-log.css',
})
export class AuditLogComponent implements OnInit {
  private auditLogService = inject(AuditLogService);
  private dialog = inject(MatDialog);

  entries = signal<AuditLogEntry[]>([]);
  isLoading = signal(false);
  totalElements = signal(0);
  page = signal(0);
  pageSize = signal(20);

  filterEntityType = signal<string>('');
  filterPerformedBy = signal<string>('');

  readonly displayedColumns = ['time', 'entityType', 'entityName', 'action', 'performedBy', 'snapshot'];

  readonly entityTypeOptions = [
    { value: '', label: 'All' },
    { value: 'POLICY_SET', label: 'Policy Set' },
    { value: 'POLICY', label: 'Policy' },
    { value: 'RULE', label: 'Rule' },
    { value: 'UI_ELEMENT', label: 'UI Element' },
  ];

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.isLoading.set(true);
    this.auditLogService.getAuditLog({
      entityType: this.filterEntityType() || undefined,
      performedBy: this.filterPerformedBy() || undefined,
      page: this.page(),
      size: this.pageSize(),
    }).subscribe({
      next: result => {
        this.entries.set(result.data);
        this.totalElements.set(result.meta.total);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  onFilterChange(): void {
    this.page.set(0);
    this.load();
  }

  onPageChange(event: PageEvent): void {
    this.page.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.load();
  }

  openSnapshot(entry: AuditLogEntry): void {
    this.dialog.open(SnapshotDialogComponent, {
      width: '480px',
      data: { snapshotJson: entry.snapshotJson },
    });
  }
}
