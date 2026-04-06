import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActiveSessionView, AdminSessionService } from '../../core/services/admin-session.service';
import { ForceTerminateDialogComponent } from './force-terminate-dialog/force-terminate-dialog';

@Component({
  selector: 'app-active-sessions',
  standalone: true,
  imports: [
    DatePipe,
    MatButtonModule,
    MatDialogModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTableModule,
    MatTooltipModule,
  ],
  templateUrl: './active-sessions.html',
  styleUrl: './active-sessions.css',
})
export class ActiveSessionsComponent implements OnInit {
  private adminSessionService = inject(AdminSessionService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  sessions = signal<ActiveSessionView[]>([]);
  isLoading = signal(false);
  terminatingIds = signal<Set<string>>(new Set());

  readonly displayedColumns = ['username', 'deviceName', 'ipAddress', 'createdAt', 'actions'];

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.isLoading.set(true);
    this.adminSessionService.getActiveSessions().subscribe({
      next: data => {
        this.sessions.set(data);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  openTerminateDialog(session: ActiveSessionView): void {
    this.dialog
      .open(ForceTerminateDialogComponent, { data: { session }, width: '440px' })
      .afterClosed()
      .subscribe(confirmed => {
        if (confirmed) this.doTerminate(session);
      });
  }

  private doTerminate(session: ActiveSessionView): void {
    const ids = new Set(this.terminatingIds());
    ids.add(session.sessionId);
    this.terminatingIds.set(ids);

    this.adminSessionService.forceTerminate(session.sessionId).subscribe({
      next: () => {
        this.sessions.update(list => list.filter(s => s.sessionId !== session.sessionId));
        const done = new Set(this.terminatingIds());
        done.delete(session.sessionId);
        this.terminatingIds.set(done);
        this.snackBar.open(`Session of "${session.username ?? session.userId}" terminated.`, 'Dismiss', { duration: 3000 });
      },
      error: (err: HttpErrorResponse) => {
        const done = new Set(this.terminatingIds());
        done.delete(session.sessionId);
        this.terminatingIds.set(done);

        const code = err.error?.code;
        if (code === '02001') {
          this.snackBar.open('Session not found — it may have already ended.', 'Dismiss', { duration: 4000 });
        } else if (code === '02002') {
          this.snackBar.open('Session is no longer active.', 'Dismiss', { duration: 4000 });
        } else {
          this.snackBar.open('Failed to terminate session. Please try again.', 'Dismiss', { duration: 4000 });
        }
        this.load();
      },
    });
  }

  isTerminating(sessionId: string): boolean {
    return this.terminatingIds().has(sessionId);
  }
}
