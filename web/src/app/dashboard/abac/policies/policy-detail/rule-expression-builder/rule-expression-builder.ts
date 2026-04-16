import {
  Component, Input, Output, EventEmitter, OnChanges, SimpleChanges,
  computed, effect, signal, inject,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule, ReactiveFormsModule, FormControl } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { CommonModule } from '@angular/common';
import { NgxMatSelectSearchModule } from 'ngx-mat-select-search';
import { ExpressionNodeRequest, ExpressionNodeView, NamedExpressionView } from '../../../../../core/services/policy.service';
import { ExpressionService } from '../../../../../core/services/expression.service';
import { detectBuilderMode, parseToConditionRows } from './spel-parser';

export type ConditionType =
  | 'subject_has_role'
  | 'subject_attr_contains'
  | 'subject_attr_equals'
  | 'action_is'
  | 'action_is_one_of'
  | 'navigation_only'
  | 'navigation_or_instance'
  | 'instance_field_equals'
  | 'instance_field_contains'
  | 'subject_attr_contains_field'
  | 'subject_attr_equals_field';

export interface ConditionRow {
  id: string;
  type: ConditionType;
  params: Record<string, string>;
}

interface ConditionOption {
  value: ConditionType;
  label: string;
}

const TARGET_OPTIONS: ConditionOption[] = [
  { value: 'subject_has_role',        label: 'Subject: has role' },
  { value: 'subject_attr_contains',   label: 'Subject: attribute contains' },
  { value: 'subject_attr_equals',     label: 'Subject: attribute equals' },
  { value: 'action_is',               label: 'Action: is' },
  { value: 'action_is_one_of',        label: 'Action: is one of' },
];

const CONDITION_OPTIONS: ConditionOption[] = [
  ...TARGET_OPTIONS,
  { value: 'navigation_only',              label: 'Navigation (no instance data)' },
  { value: 'navigation_or_instance',       label: 'Navigation OR instance condition' },
  { value: 'instance_field_equals',        label: 'Instance field equals' },
  { value: 'instance_field_contains',      label: 'Instance field contains' },
  { value: 'subject_attr_contains_field',  label: 'Subject attribute contains field' },
  { value: 'subject_attr_equals_field',    label: 'Subject attribute equals field' },
];

export type ActiveTab = 'builder' | 'library' | 'raw';

@Component({
  selector: 'app-rule-expression-builder',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    NgxMatSelectSearchModule,
  ],
  templateUrl: './rule-expression-builder.html',
  styleUrl: './rule-expression-builder.css',
})
export class RuleExpressionBuilderComponent implements OnChanges {
  @Input() mode: 'target' | 'condition' = 'target';
  @Input() resourceActions: { id: number; name: string }[] = [];
  @Input() initialValue: ExpressionNodeView | null = null;
  @Input() depth: number = 0;
  @Output() nodeChange = new EventEmitter<ExpressionNodeRequest | null>();

  private expressionService = inject(ExpressionService);

  // ── tab state ──────────────────────────────────────────────────────────────
  activeTab = signal<ActiveTab>('builder');

  // ── builder tab ───────────────────────────────────────────────────────────
  conditions  = signal<ConditionRow[]>([{ id: '0', type: 'subject_has_role', params: {} }]);
  private nextId = 1;

  // ── library tab ───────────────────────────────────────────────────────────
  namedExpressions   = signal<NamedExpressionView[]>([]);
  libraryLoaded      = signal(false);
  librarySearchControl = new FormControl<string>('', { nonNullable: true });
  searchTerm = toSignal(this.librarySearchControl.valueChanges, { initialValue: '' });
  selectedRefId      = signal<number | null>(null);

  filteredExpressions = computed(() => {
    const term = this.searchTerm().toLowerCase();
    return this.namedExpressions().filter(e =>
      e.name.toLowerCase().includes(term) || e.spel.toLowerCase().includes(term)
    );
  });

  getSelectedExpressionName = computed(() => {
    const id = this.selectedRefId();
    if (id === null) return '';
    const found = this.namedExpressions().find(e => e.id === id);
    return found ? found.name : '';
  });

  // ── raw tab ───────────────────────────────────────────────────────────────
  rawName = signal('');
  rawSpel = signal('');

  // ── computed SpEL preview ─────────────────────────────────────────────────
  get conditionOptions(): ConditionOption[] {
    return this.mode === 'condition' ? CONDITION_OPTIONS : TARGET_OPTIONS;
  }

  builderSpel = computed(() => {
    const row = this.conditions()[0];
    if (!row) return null;
    return this.conditionToSpel(row);
  });

  constructor() {
    effect(() => {
      this.emitCurrentNode();
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!changes['initialValue']) return;
    const v = this.initialValue;
    
    // Prevent feedback loop: If incoming value matches our current state, ignore it.
    // Use ?? '' to normalize null builderSpel (nothing filled yet) so it matches resolvedSpel ''
    const currentSpel = this.activeTab() === 'builder' ? (this.builderSpel() ?? '') : this.rawSpel();
    if (v?.resolvedSpel === currentSpel && (v?.name || '') === this.rawName()) {
      return; 
    }

    if (!v) {
      this.conditions.set([{ id: String(this.nextId++), type: this.mode === 'condition' ? 'navigation_only' : 'subject_has_role', params: {} }]);
      this.activeTab.set('builder');
      this.rawName.set('');
      return;
    }

    if (v.type === 'LIBRARY_REF') {
      this.activeTab.set('library');
      this.selectedRefId.set(v.refId ?? null);
      this.loadLibraryIfNeeded();
      return;
    }

    if (v.type === 'COMPOSITION') return; // handled by parent ExpressionNodeEditor

    // INLINE
    const spel = v.resolvedSpel ?? '';
    const detectedMode = detectBuilderMode(spel);
    
    if (detectedMode === 'builder') {
      const parsed = parseToConditionRows(spel);
      if (parsed) {
        const rows = parsed.rows.map(r => ({ ...r, id: String(this.nextId++) }));
        this.conditions.set(rows.length > 0 ? [rows[0]] : [{ id: String(this.nextId++), type: 'subject_has_role', params: {} }]);
        // Only switch to builder if we aren't already in builder/library
        if (this.activeTab() !== 'builder' && this.activeTab() !== 'library') {
          this.activeTab.set('builder');
        }
        if (v.name) this.rawName.set(v.name);
        return;
      }
    }
    
    // fallback to raw
    this.rawSpel.set(spel);
    if (v.name) this.rawName.set(v.name);
    // Only switch to raw if we aren't already there or in library
    if (this.activeTab() !== 'raw' && this.activeTab() !== 'library') {
       this.activeTab.set('raw');
    }
  }

  // ── tab switching ─────────────────────────────────────────────────────────
  switchTab(tab: ActiveTab): void {
    if (tab === this.activeTab()) return;
    if (tab === 'library') this.loadLibraryIfNeeded();
    this.activeTab.set(tab);
  }

  loadLibraryIfNeeded(): void {
    if (this.libraryLoaded()) return;
    this.expressionService.getNamedExpressions().subscribe(list => {
      this.namedExpressions.set(list);
      this.libraryLoaded.set(true);
    });
  }

  // ── library actions ───────────────────────────────────────────────────────
  selectLibraryItem(id: number): void {
    this.selectedRefId.set(id);
    this.emitCurrentNode();
  }

  // ── builder actions ───────────────────────────────────────────────────────
  updateConditionType(id: string, type: ConditionType): void {
    this.conditions.update(rows =>
      rows.map(r => r.id === id ? { ...r, type, params: {} } : r)
    );
  }

  updateParam(id: string, key: string, value: string): void {
    this.conditions.update(rows =>
      rows.map(r => r.id === id ? { ...r, params: { ...r.params, [key]: value } } : r)
    );
  }

  // ── raw actions ───────────────────────────────────────────────────────────
  onRawSpelInput(value: string): void {
    this.rawSpel.set(value);
  }

  onRawNameInput(value: string): void {
    this.rawName.set(value);
  }

  // ── wrap actions (depth < 3) ──────────────────────────────────────────────
  wrapInBlock(operator: 'AND' | 'OR'): void {
    const current = this.buildCurrentNode();
    this.nodeChange.emit({
      type: 'COMPOSITION',
      operator,
      children: current ? [current] : [],
    });
  }

  // ── emit ──────────────────────────────────────────────────────────────────
  private emitCurrentNode(): void {
    this.nodeChange.emit(this.buildCurrentNode());
  }

  private buildCurrentNode(): ExpressionNodeRequest | null {
    switch (this.activeTab()) {
      case 'builder': {
        const spel = this.builderSpel();
        const name = this.rawName().trim();
        if (!spel || !name) {
          // Return an incomplete INLINE instead of null.
          // null is interpreted by the parent as "delete this child".
          return { type: 'INLINE', spel: spel ?? '', name };
        }
        return { type: 'INLINE', spel, name };
      }
      case 'library': {
        const refId = this.selectedRefId();
        if (refId === null) return { type: 'INLINE', spel: '', name: '' };
        return { type: 'LIBRARY_REF', refId };
      }
      case 'raw': {
        const spel = this.rawSpel().trim();
        const name = this.rawName().trim();
        if (!spel || !name) {
          return { type: 'INLINE', spel, name };
        }
        return { type: 'INLINE', spel, name };
      }
    }
  }

  // ── SpEL generation (forward) ─────────────────────────────────────────────
  conditionToSpel(row: ConditionRow): string | null {
    const p = row.params;
    switch (row.type) {
      case 'subject_has_role':
        return p['role']?.trim() ? `subject.roles.contains('${p['role'].trim()}')` : null;
      case 'subject_attr_contains':
        return p['attribute']?.trim() && p['value']?.trim()
          ? `subject.getAttribute('${p['attribute'].trim()}').contains('${p['value'].trim()}')`
          : null;
      case 'subject_attr_equals':
        return p['attribute']?.trim() && p['value']?.trim()
          ? `subject.getAttribute('${p['attribute'].trim()}') == '${p['value'].trim()}'`
          : null;
      case 'action_is':
        return p['action']?.trim()
          ? `action.getAttribute('name') == '${p['action'].trim()}'`
          : null;
      case 'action_is_one_of': {
        const actions = (p['actions'] ?? '').split(',').map(s => s.trim()).filter(s => s);
        if (actions.length === 0) return null;
        if (actions.length === 1) return `action.getAttribute('name') == '${actions[0]}'`;
        return `#{'${actions.join("','")}'}.contains(action.getAttribute('name'))`;
      }
      case 'navigation_only':
        return 'object.data == null';
      case 'navigation_or_instance':
        return p['instanceCondition']?.trim()
          ? `object.data == null || ${p['instanceCondition'].trim()}`
          : 'object.data == null';
      case 'instance_field_equals':
        return p['field']?.trim() && p['value']?.trim()
          ? `object.data.${p['field'].trim()} == '${p['value'].trim()}'`
          : null;
      case 'instance_field_contains':
        return p['field']?.trim() && p['value']?.trim()
          ? `object.data.${p['field'].trim()}.contains('${p['value'].trim()}')`
          : null;
      case 'subject_attr_contains_field':
        return p['attribute']?.trim() && p['field']?.trim()
          ? `subject.getAttribute('${p['attribute'].trim()}').contains(object.data.${p['field'].trim()})`
          : null;
      case 'subject_attr_equals_field':
        return p['attribute']?.trim() && p['field']?.trim()
          ? `subject.getAttribute('${p['attribute'].trim()}') == object.data.${p['field'].trim()}`
          : null;
      default:
        return null;
    }
  }
}
