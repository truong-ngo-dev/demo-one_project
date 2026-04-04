import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { UserSelfService, UpdateProfileData, UserProfile } from '../../core/services/user-self.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatDividerModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
  ],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class ProfileComponent implements OnInit {
  private userSelfService = inject(UserSelfService);
  private authService     = inject(AuthService);

  user     = signal<UserProfile | null>(null);
  isLoading = signal(true);
  isSaving  = signal(false);
  errorMessage   = signal<string | null>(null);
  successMessage = signal<string | null>(null);
  showProfileNudge = signal(false);

  form = new FormGroup({
    username:    new FormControl(''),
    fullName:    new FormControl(''),
    phoneNumber: new FormControl(''),
  });

  ngOnInit(): void {
    const currentUser = this.authService.currentUser;
    if (currentUser?.requiresProfileCompletion) {
      this.showProfileNudge.set(true);
    }

    this.userSelfService.getMe().subscribe(profile => {
      this.isLoading.set(false);
      if (!profile) return;

      this.user.set(profile);
      this.form.patchValue({
        username:    profile.username,
        fullName:    profile.fullName ?? '',
        phoneNumber: profile.phoneNumber ?? '',
      });

      if (profile.usernameChanged) {
        this.form.get('username')?.disable();
      }
    });
  }

  submit(): void {
    if (this.isSaving()) return;

    const profile = this.user();
    const formValue = this.form.getRawValue();
    const data: UpdateProfileData = {};

    // Username chỉ gửi nếu chưa đổi lần nào VÀ giá trị thực sự thay đổi
    if (!profile?.usernameChanged && formValue.username && formValue.username !== profile?.username) {
      data.username = formValue.username;
    }

    if (formValue.fullName?.trim()) {
      data.fullName = formValue.fullName.trim();
    }

    if (formValue.phoneNumber?.trim()) {
      data.phoneNumber = formValue.phoneNumber.trim();
    }

    if (Object.keys(data).length === 0) return;

    this.isSaving.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    this.userSelfService.updateProfile(data).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.successMessage.set('Cập nhật thông tin thành công.');
        // Nếu username vừa được đổi, disable field
        if (data.username) {
          this.form.get('username')?.disable();
          this.user.update(u => u ? { ...u, usernameChanged: true } : u);
        }
      },
      error: (err) => {
        this.isSaving.set(false);
        const code = err?.error?.code;
        if (code === '10010')      this.errorMessage.set('Username này đã được sử dụng.');
        else if (code === '10012') this.errorMessage.set('Username chỉ có thể đổi một lần.');
        else if (code === '10013') this.errorMessage.set('Số điện thoại này đã được sử dụng.');
        else                       this.errorMessage.set('Đã có lỗi xảy ra, vui lòng thử lại.');
      },
    });
  }
}
