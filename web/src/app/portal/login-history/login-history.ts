import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { LoginActivityItem, SessionService } from '../../core/services/session.service';

@Component({
  selector: 'app-login-history',
  standalone: true,
  imports: [CommonModule, MatProgressSpinnerModule, MatButtonModule, MatIconModule],
  templateUrl: './login-history.html',
  styleUrl: './login-history.css',
})
export class LoginHistoryComponent implements OnInit {
  private sessionService = inject(SessionService);

  activities = signal<LoginActivityItem[]>([]);
  isLoading = signal(true);
  page = signal(0);
  totalPages = signal(0);

  ngOnInit(): void {
    this.loadPage(0);
  }

  loadPage(page: number): void {
    this.isLoading.set(true);
    this.sessionService.getMyLoginActivities(page, 20).subscribe({
      next: result => {
        this.activities.set(result.content);
        this.page.set(result.page);
        this.totalPages.set(result.totalPages);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  previous(): void {
    if (this.page() > 0) this.loadPage(this.page() - 1);
  }

  next(): void {
    if (this.page() < this.totalPages() - 1) this.loadPage(this.page() + 1);
  }

  isFailed(item: LoginActivityItem): boolean {
    return item.result !== 'SUCCESS';
  }
}
