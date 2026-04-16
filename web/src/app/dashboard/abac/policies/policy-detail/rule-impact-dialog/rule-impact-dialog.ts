import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { ImpactPreviewResult } from '../../../../../core/services/simulate.service';

@Component({
  selector: 'app-rule-impact-dialog',
  standalone: true,
  imports: [
    MatButtonModule,
    MatChipsModule,
    MatDialogModule,
    MatIconModule,
  ],
  templateUrl: './rule-impact-dialog.html',
  styleUrl: './rule-impact-dialog.css',
})
export class RuleImpactDialogComponent {
  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { impact: ImpactPreviewResult },
    private dialogRef: MatDialogRef<RuleImpactDialogComponent>,
  ) {}

  get impact(): ImpactPreviewResult {
    return this.data.impact;
  }

  cancel(): void { this.dialogRef.close(false); }
  confirm(): void { this.dialogRef.close(true); }
}
