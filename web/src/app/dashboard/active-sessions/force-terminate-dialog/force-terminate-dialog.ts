import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ActiveSessionView } from '../../../core/services/admin-session.service';

@Component({
  selector: 'app-force-terminate-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatIconModule],
  templateUrl: './force-terminate-dialog.html',
  styleUrl: './force-terminate-dialog.css',
})
export class ForceTerminateDialogComponent {
  dialogRef = inject(MatDialogRef<ForceTerminateDialogComponent>);
  data: { session: ActiveSessionView } = inject(MAT_DIALOG_DATA);
}
