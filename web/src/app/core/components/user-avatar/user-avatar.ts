import { Component, Input, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-user-avatar',
  standalone: true,
  imports: [MatButtonModule, MatDividerModule, MatIconModule, MatMenuModule],
  templateUrl: './user-avatar.html',
  styleUrl: './user-avatar.css',
})
export class UserAvatarComponent {
  @Input() username: string | null = null;
  @Input() fullName: string | null = null;
  @Input() showProfileOptions: boolean = true; // Thêm Input để điều khiển việc hiển thị các option profile

  private authService = inject(AuthService);
  private router = inject(Router);

  get initials(): string {
    if (this.fullName) {
      const parts = this.fullName.trim().split(/\s+/);
      const first = parts[0]?.[0] ?? '';
      const last  = parts.length > 1 ? (parts[parts.length - 1][0] ?? '') : '';
      return (first + last).toUpperCase() || '?';
    }
    if (this.username) {
      return this.username[0].toUpperCase();
    }
    return '?';
  }

  goToProfile(): void {
    this.router.navigate(['/app/profile']);
  }

  goToChangePassword(): void {
    this.router.navigate(['/app/profile/password']);
  }

  logout(): void {
    this.authService.logout();
  }
}
