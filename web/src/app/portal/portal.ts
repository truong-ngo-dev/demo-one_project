import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { UserAvatarComponent } from '../core/components/user-avatar/user-avatar';
import { UserSelfService } from '../core/services/user-self.service';
import { AuthService } from '../core/services/auth.service';
import { MatTooltip } from '@angular/material/tooltip';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';

@Component({
  selector: 'app-portal',
  standalone: true,
  imports: [
    RouterLink,
    RouterLinkActive,
    RouterOutlet,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatSidenavModule,
    UserAvatarComponent,
    MatTooltip,
  ],
  templateUrl: './portal.html',
  styleUrl: './portal.css',
})
export class PortalComponent implements OnInit {
  private userSelfService = inject(UserSelfService);
  private authService    = inject(AuthService);
  private router         = inject(Router);

  username = signal<string | null>(null);
  fullName = signal<string | null>(null);

  ngOnInit(): void {
    // authGuard đã chạy trước — _currentUser đã được cache, dùng lại luôn
    this.authService.loadCurrentUser().subscribe(user => {
      if (user?.roles.some(r => r.name === 'ADMIN')) {
        this.router.navigate(['/admin/dashboard']);
        return;
      }
    });

    this.userSelfService.getMe().subscribe(profile => {
      if (profile) {
        this.username.set(profile.username);
        this.fullName.set(profile.fullName);
      }
    });
  }
}
