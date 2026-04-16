import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { DatePipe, JsonPipe } from '@angular/common';
import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { MatButtonModule } from '@angular/material/button';
import { MatChipInputEvent, MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
  SimulateService, SimulateResponse,
  NavigationSimulateResult, NavigationActionDecision,
  ReverseLookupResult,
} from '../../../core/services/simulate.service';
import { ResourceService, ResourceSummaryView, ActionView } from '../../../core/services/resource.service';

@Component({
  selector: 'app-simulator',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    DatePipe,
    JsonPipe,
    MatButtonModule,
    MatChipsModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatTableModule,
    MatTooltipModule,
  ],
  templateUrl: './simulator.html',
  styleUrl: './simulator.css',
})
export class SimulatorComponent implements OnInit {
  private simulateService = inject(SimulateService);
  private resourceService = inject(ResourceService);

  readonly separatorKeysCodes = [ENTER, COMMA];

  // ── Shared subject fields ──────────────────────────────────────────────────
  roles      = signal<string[]>([]);
  resources  = signal<ResourceSummaryView[]>([]);

  subjectForm = new FormGroup({
    userId:     new FormControl(''),
    attributes: new FormControl(''),
  });

  // ── Mode toggle ───────────────────────────────────────────────────────────
  mode = signal<'navigation' | 'instance' | 'reverse'>('navigation');

  // ── Navigation mode ───────────────────────────────────────────────────────
  navForm = new FormGroup({
    resourceId: new FormControl<number | null>(null, Validators.required),
  });

  isNavigating  = signal(false);
  navResult     = signal<NavigationSimulateResult | null>(null);
  navError      = signal<string | null>(null);
  navDecisions  = computed<NavigationActionDecision[]>(() => this.navResult()?.decisions ?? []);
  readonly navColumns = ['action', 'decision', 'matchedRule'];

  // ── Instance mode ─────────────────────────────────────────────────────────
  instanceForm = new FormGroup({
    resourceName:  new FormControl<number | null>(null, Validators.required),
    action:        new FormControl<number | null>(null, Validators.required),
    resourceData:  new FormControl(''),
  });

  availableActions = signal<ActionView[]>([]);
  isSimulating     = signal(false);
  result           = signal<SimulateResponse | null>(null);
  error            = signal<string | null>(null);

  // ── Reverse Lookup mode ───────────────────────────────────────────────────
  reverseForm = new FormGroup({
    resourceId: new FormControl<number | null>(null, Validators.required),
    actionId:   new FormControl<number | null>(null, Validators.required),
  });

  reverseActions  = signal<ActionView[]>([]);
  isLookingUp     = signal(false);
  reverseResult   = signal<ReverseLookupResult | null>(null);
  lookupError     = signal<string | null>(null);

  ngOnInit(): void {
    this.resourceService.getResources({ size: 200 }).subscribe({
      next: result => this.resources.set(result.data),
    });
  }

  // ── Navigation helpers ────────────────────────────────────────────────────

  runNavigationSimulation(): void {
    if (this.navForm.invalid) return;
    const resourceId = this.navForm.controls.resourceId.value!;
    const resourceName = this.resources().find(r => r.id === resourceId)?.name;
    if (!resourceName) return;

    const attrsStr = this.subjectForm.controls.attributes.value;
    let attributes: Record<string, unknown> = {};
    if (attrsStr?.trim()) {
      try { attributes = JSON.parse(attrsStr); }
      catch {
        this.navError.set('Attributes must be valid JSON.');
        return;
      }
    }

    this.isNavigating.set(true);
    this.navResult.set(null);
    this.navError.set(null);

    this.simulateService.simulateNavigation({
      subject: {
        userId: this.subjectForm.controls.userId.value || null,
        roles: this.roles(),
        attributes,
      },
      resourceName,
      policySetId: null,
    }).subscribe({
      next: res => { this.navResult.set(res); this.isNavigating.set(false); },
      error: (err: HttpErrorResponse) => {
        this.isNavigating.set(false);
        this.navError.set(err.error?.error?.message ?? 'Navigation simulation failed.');
      },
    });
  }

  // ── Instance helpers ──────────────────────────────────────────────────────

  onResourceChange(resourceId: number | null): void {
    this.instanceForm.controls.action.reset(null);
    this.availableActions.set([]);
    if (resourceId == null) return;
    this.resourceService.getResourceById(resourceId).subscribe({
      next: r => this.availableActions.set(r.actions),
    });
  }

  getSelectedResourceName(): string {
    const rid = this.instanceForm.controls.resourceName.value;
    return this.resources().find(r => r.id === rid)?.name ?? '';
  }

  getSelectedActionName(): string {
    const aid = this.instanceForm.controls.action.value;
    return this.availableActions().find(a => a.id === aid)?.name ?? '';
  }

  runSimulation(): void {
    if (this.instanceForm.invalid) return;
    this.isSimulating.set(true);
    this.result.set(null);
    this.error.set(null);

    const v = this.instanceForm.getRawValue();
    const attrsStr = this.subjectForm.controls.attributes.value;

    let resourceData: unknown = null;
    if (v.resourceData?.trim()) {
      try { resourceData = JSON.parse(v.resourceData); }
      catch {
        this.error.set('Resource data must be valid JSON.');
        this.isSimulating.set(false);
        return;
      }
    }

    let attributes: Record<string, unknown> = {};
    if (attrsStr?.trim()) {
      try { attributes = JSON.parse(attrsStr); }
      catch {
        this.error.set('Attributes must be valid JSON.');
        this.isSimulating.set(false);
        return;
      }
    }

    this.simulateService.simulate({
      subject: {
        userId: this.subjectForm.controls.userId.value || null,
        roles: this.roles(),
        attributes,
      },
      resource: {
        name: this.getSelectedResourceName(),
        data: resourceData,
      },
      action: this.getSelectedActionName(),
      policySetId: null,
    }).subscribe({
      next: res => { this.result.set(res); this.isSimulating.set(false); },
      error: (err: HttpErrorResponse) => {
        this.isSimulating.set(false);
        this.error.set(err.error?.error?.message ?? 'Simulation failed. Please try again.');
      },
    });
  }

  // ── Reverse Lookup helpers ────────────────────────────────────────────────

  onReverseResourceChange(resourceId: number | null): void {
    this.reverseForm.controls.actionId.reset(null);
    this.reverseActions.set([]);
    if (resourceId == null) return;
    this.resourceService.getResourceById(resourceId).subscribe({
      next: r => this.reverseActions.set(r.actions),
    });
  }

  runReverseLookup(): void {
    if (this.reverseForm.invalid) return;
    const resourceId = this.reverseForm.controls.resourceId.value!;
    const actionId   = this.reverseForm.controls.actionId.value!;
    const resourceName = this.resources().find(r => r.id === resourceId)?.name;
    const actionName   = this.reverseActions().find(a => a.id === actionId)?.name;
    if (!resourceName || !actionName) return;

    this.isLookingUp.set(true);
    this.reverseResult.set(null);
    this.lookupError.set(null);

    this.simulateService.getReverseLookup(resourceName, actionName).subscribe({
      next: res => { this.reverseResult.set(res); this.isLookingUp.set(false); },
      error: (err: HttpErrorResponse) => {
        this.isLookingUp.set(false);
        this.lookupError.set(err.error?.error?.message ?? 'Reverse lookup failed.');
      },
    });
  }

  getReverseActionName(): string {
    const aid = this.reverseForm.controls.actionId.value;
    return this.reverseActions().find(a => a.id === aid)?.name ?? '';
  }

  getReverseResourceName(): string {
    const rid = this.reverseForm.controls.resourceId.value;
    return this.resources().find(r => r.id === rid)?.name ?? '';
  }

  // ── Shared ────────────────────────────────────────────────────────────────

  addRole(event: MatChipInputEvent): void {
    const value = (event.value || '').trim();
    if (value) this.roles.update(roles => [...roles, value]);
    event.chipInput.clear();
  }

  removeRole(role: string): void {
    this.roles.update(roles => roles.filter(r => r !== role));
  }

  get resultJson(): string {
    return JSON.stringify(this.result(), null, 2);
  }
}
