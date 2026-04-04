import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { DeviceSession, SessionService } from '../../core/services/session.service';
import { MatDivider } from '@angular/material/divider';

@Component({
  selector: 'app-devices',
  standalone: true,
  imports: [
    CommonModule,
    MatProgressSpinnerModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatSnackBarModule,
    MatDivider
  ],
  templateUrl: './devices.html',
  styleUrl: './devices.css',
})
export class DevicesComponent implements OnInit {
  private sessionService = inject(SessionService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  devices = signal<DeviceSession[]>([]);
  isLoading = signal(true);

  ngOnInit(): void {
    this.loadDevices();
  }

  loadDevices(): void {
    this.isLoading.set(true);
    this.sessionService.getMyDevices().subscribe({
      next: list => {
        this.devices.set(list);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  revokeDevice(device: DeviceSession): void {
    if (!device.sessionId) return;

    const ref = this.dialog.open(ConfirmRevokeDialogComponent, {
      width: '400px',
      data: { deviceName: device.deviceName || 'Thiết bị không xác định' },
    });

    ref.afterClosed().subscribe(confirmed => {
      if (!confirmed || !device.sessionId) return;

      this.sessionService.revokeSession(device.sessionId).subscribe({
        next: () => {
          this.snackBar.open('Đã đăng xuất thiết bị thành công.', 'Đóng', { duration: 3000 });
          this.loadDevices();
        },
        error: () => {
          this.snackBar.open('Không thể đăng xuất thiết bị. Vui lòng thử lại.', 'Đóng', {
            duration: 4000,
          });
        },
      });
    });
  }
}

// ---------------------------------------------------------------------------
// Confirm revoke dialog — co-located with usage
// ---------------------------------------------------------------------------

import { Component as NgComponent, inject as ngInject } from '@angular/core';

@NgComponent({
  selector: 'app-confirm-revoke-dialog',
  standalone: true,
  imports: [MatButtonModule, MatDialogModule],
  template: `
    <h2 mat-dialog-title>Đăng xuất thiết bị</h2>
    <mat-dialog-content>
      <p>
        Bạn có chắc muốn đăng xuất khỏi
        <strong>{{ data.deviceName }}</strong>?
      </p>
      <p class="warning-text">Thiết bị đó sẽ bị đăng xuất ngay lập tức.</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="dialogRef.close(false)">Hủy</button>
      <button mat-flat-button color="warn" (click)="dialogRef.close(true)">Đăng xuất</button>
    </mat-dialog-actions>
  `,
})
export class ConfirmRevokeDialogComponent {
  dialogRef = ngInject(MatDialogRef<ConfirmRevokeDialogComponent>);
  data: { deviceName: string } = ngInject(MAT_DIALOG_DATA);
}
