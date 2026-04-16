import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { UserAvatarComponent } from '../core/components/user-avatar/user-avatar';
import { AbacService, ADMIN_ROUTE_ELEMENT_IDS } from '../core/services/abac.service';
import { AuthService } from '../core/services/auth.service';
import { UserSelfService } from '../core/services/user-self.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    RouterLink,
    RouterLinkActive,
    RouterOutlet,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatSidenavModule,
    MatToolbarModule,
    MatTooltipModule,
    UserAvatarComponent,
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class DashboardComponent implements OnInit {
  private authService     = inject(AuthService);
  private userSelfService = inject(UserSelfService);
  readonly abacService    = inject(AbacService);

  username = signal<string | null>(null);
  fullName = signal<string | null>(null);

  ngOnInit(): void {
    this.userSelfService.getMe().subscribe(profile => {
      if (profile) {
        this.username.set(profile.username);
        this.fullName.set(profile.fullName);
      }
    });
    this.abacService.loadVisibility([...ADMIN_ROUTE_ELEMENT_IDS]).subscribe();
  }

  logout(): void {
    this.abacService.clearVisibility();
    this.authService.logout();
  }
}
