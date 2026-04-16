import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-snapshot-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule],
  templateUrl: './snapshot-dialog.html',
  styleUrl: './snapshot-dialog.css',
})
export class SnapshotDialogComponent {
  data = inject<{ snapshotJson: string | null }>(MAT_DIALOG_DATA);

  get formatted(): string {
    if (!this.data.snapshotJson) return '(no snapshot)';
    try {
      return JSON.stringify(JSON.parse(this.data.snapshotJson), null, 2);
    } catch {
      return this.data.snapshotJson;
    }
  }
}
