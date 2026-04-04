import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltip } from '@angular/material/tooltip';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [RouterLink, MatButtonModule, MatIconModule],
  templateUrl: './landing.html',
  styleUrl: './landing.css',
})
export class LandingComponent {
  readonly loginUrl = `${environment.webGatewayUrl}/webgw/auth/login`;

  loginWithGoogle(): void {
    window.location.href = `${environment.webGatewayUrl}/oauth2/authorization/web-gateway`;
  }
}
