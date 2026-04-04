import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCard, MatCardContent, MatCardHeader, MatCardTitle, MatCardSubtitle } from '@angular/material/card';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatCard,
    MatCardHeader,
    MatCardContent,
    MatCardTitle,
    MatCardSubtitle
  ],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class RegisterComponent {
  private http   = inject(HttpClient);
  private router = inject(Router);

  isLoading    = signal(false);
  errorMessage = signal<string | null>(null);

  form = new FormGroup({
    email:    new FormControl('', [Validators.required, Validators.email]),
    username: new FormControl('', [Validators.required]),
    password: new FormControl('', [Validators.required]),
    fullName: new FormControl(''),
  });

  register(): void {
    if (this.isLoading() || this.form.invalid) return;

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const { email, username, password, fullName } = this.form.getRawValue();

    this.http.post('/api/admin/v1/users/register', { email, username, password, fullName }).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.router.navigate(['/'], { state: { registerSuccess: true } });
      },
      error: (err) => {
        this.isLoading.set(false);
        const code = err?.error?.code;
        if (code === '10009')      this.errorMessage.set('Email này đã được sử dụng.');
        else if (code === '10010') this.errorMessage.set('Username này đã được sử dụng.');
        else                       this.errorMessage.set('Đã có lỗi xảy ra, vui lòng thử lại.');
      },
    });
  }

  loginWithGoogle(): void {
    window.location.href = `${environment.webGatewayUrl}/oauth2/authorization/web-gateway`;
  }
}
