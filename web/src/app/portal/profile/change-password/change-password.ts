import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { UserSelfService, ChangePasswordData } from '../../../core/services/user-self.service';
import { MatDivider } from '@angular/material/divider';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatDivider,
  ],
  templateUrl: './change-password.html',
  styleUrl: './change-password.css',
})
export class ChangePasswordComponent implements OnInit {
  private userSelfService = inject(UserSelfService);

  // true = user có password → phải nhập currentPassword
  hasPassword = computed(() => this.userSelfService.profile()?.hasPassword ?? true);

  isSaving     = signal(false);
  errorMessage = signal<string | null>(null);
  infoMessage  = signal<string | null>(null);

  form = new FormGroup({
    currentPassword: new FormControl(''),
    newPassword:     new FormControl('', [Validators.required]),
  });

  ngOnInit(): void {
    // Đảm bảo profile đã được load để hasPassword signal hoạt động đúng
    this.userSelfService.getMe().subscribe();
  }

  submit(): void {
    if (this.isSaving() || this.form.invalid) return;

    this.errorMessage.set(null);
    this.infoMessage.set(null);
    this.isSaving.set(true);

    const formValue = this.form.value;
    const data: ChangePasswordData = { newPassword: formValue.newPassword! };
    if (this.hasPassword() && formValue.currentPassword) {
      data.currentPassword = formValue.currentPassword;
    }

    this.userSelfService.changePassword(data).subscribe({
      next: (result) => {
        this.isSaving.set(false);
        if (!result.changed) {
          this.infoMessage.set(result.message ?? 'Password không thay đổi.');
        } else {
          this.infoMessage.set('Đổi mật khẩu thành công.');
          this.form.reset();
        }
      },
      error: (err) => {
        this.isSaving.set(false);
        const code = err?.error?.code;
        if (code === '10014')      this.errorMessage.set('Vui lòng nhập mật khẩu hiện tại.');
        else if (code === '10005') this.errorMessage.set('Mật khẩu hiện tại không đúng.');
        else                       this.errorMessage.set('Đã có lỗi xảy ra, vui lòng thử lại.');
      },
    });
  }
}
