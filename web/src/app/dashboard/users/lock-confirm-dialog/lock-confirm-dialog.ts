import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { UserSummary } from '../../../core/services/user.service';

@Component({
  selector: 'app-lock-confirm-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatIconModule],
  templateUrl: './lock-confirm-dialog.html',
  styleUrl: './lock-confirm-dialog.css',
})
export class LockConfirmDialogComponent {
  dialogRef = inject(MatDialogRef<LockConfirmDialogComponent>);
  data: { user: UserSummary; action: 'lock' | 'unlock' } = inject(MAT_DIALOG_DATA);
}
