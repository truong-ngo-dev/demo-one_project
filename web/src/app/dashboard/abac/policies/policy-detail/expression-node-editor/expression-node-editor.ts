import {
  Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, forwardRef, signal, computed,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ExpressionNodeRequest, ExpressionNodeView } from '../../../../../core/services/policy.service';
import { RuleExpressionBuilderComponent } from '../rule-expression-builder/rule-expression-builder';
import { MatTooltipModule } from '@angular/material/tooltip';

/** Converts an ExpressionNodeRequest back to a view shape so sub-editors can be pre-filled. */
function requestToView(req: ExpressionNodeRequest): ExpressionNodeView {
  return {
    type: req.type,
    name: req.name ?? null,
    resolvedSpel: req.spel ?? null,
    refId: req.refId ?? null,
    operator: req.operator ?? null,
    children: req.children?.map(requestToView) ?? null,
  };
}

@Component({
  selector: 'app-expression-node-editor',
  standalone: true,
  imports: [
    MatButtonModule,
    MatIconModule,
    RuleExpressionBuilderComponent,
    MatTooltipModule,
    // eslint-disable-next-line @typescript-eslint/no-use-before-define
    forwardRef(() => ExpressionNodeEditorComponent),
  ],
  templateUrl: './expression-node-editor.html',
  styleUrl: './expression-node-editor.css',
})
export class ExpressionNodeEditorComponent implements OnChanges {
  @Input() mode: 'target' | 'condition' = 'target';
  @Input() resourceActions: { id: number; name: string }[] = [];
  @Input() initialValue: ExpressionNodeView | null = null;
  @Input() depth: number = 0;
  @Output() nodeChange = new EventEmitter<ExpressionNodeRequest | null>();

  // ── composition state ─────────────────────────────────────────────────────
  isComposition = signal(false);
  compositionOperator = signal<'AND' | 'OR'>('AND');
  children = signal<{ id: string; view: ExpressionNodeView | null }[]>([]);

  /** Tracks the current ExpressionNodeRequest for each child by its ID. */
  private childNodes = new Map<string, ExpressionNodeRequest | null>();
  private nextId = 0;

  // ── computed SpEL preview (recursive) ─────────────────────────────────────
  fullSpel = computed(() => {
    if (this.depth !== 0) return null;
    const node = this.isComposition() ? this.buildCompositionNode() : this.leafNode();
    return this.computeNodeSpel(node) || '(no expression)';
  });

  leafNode = signal<ExpressionNodeRequest | null>(null);

  private computeNodeSpel(node: ExpressionNodeRequest | null): string | null {
    if (!node) return null;
    if (node.type === 'INLINE') return node.spel || null;
    if (node.type === 'LIBRARY_REF') return `ref:${node.refId}`; // Placeholder if spel unknown
    
    if (node.type === 'COMPOSITION') {
      const childrenSpel = (node.children ?? [])
        .map(c => this.computeNodeSpel(c))
        .filter(s => s !== null);
      
      if (childrenSpel.length === 0) return null;
      if (childrenSpel.length === 1) return childrenSpel[0];
      return `(${childrenSpel.join(` ${node.operator} `)})`;
    }
    return null;
  }

  private buildCompositionNode(): ExpressionNodeRequest {
    const validChildren = this.children()
      .map(c => this.childNodes.get(c.id) ?? null)
      .filter((n): n is ExpressionNodeRequest => n !== null);

    return {
      type: 'COMPOSITION',
      operator: this.compositionOperator(),
      children: validChildren,
    };
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!changes['initialValue']) return;
    const v = this.initialValue;
    
    if (!v || v.type !== 'COMPOSITION') {
      this.isComposition.set(false);
      this.childNodes.clear();
      this.children.set([]);
      
      // Update leafNode to keep preview in sync
      if (v) {
        this.leafNode.set({
          type: v.type as any,
          spel: v.resolvedSpel ?? undefined,
          name: v.name ?? undefined,
          refId: v.refId ?? undefined,
        });
      } else {
        this.leafNode.set(null);
      }
      return;
    }

    this.isComposition.set(true);
    this.compositionOperator.set(v.operator ?? 'AND');
    this.childNodes.clear();
    this.children.set(
      (v.children ?? []).map(c => {
        const id = String(this.nextId++);
        this.childNodes.set(id, null);
        return { id, view: c };
      }),
    );
  }

  // ── leaf builder callbacks ────────────────────────────────────────────────

  /** Receives nodeChange from the leaf RuleExpressionBuilder. */
  onLeafChange(node: ExpressionNodeRequest | null): void {
    if (node?.type === 'COMPOSITION') {
      // Wrap action — promote this editor to composition mode
      this.isComposition.set(true);
      this.compositionOperator.set(node.operator ?? 'AND');
      this.childNodes.clear();
      this.children.set(
        (node.children ?? []).map(child => {
          const id = String(this.nextId++);
          this.childNodes.set(id, child);
          return { id, view: requestToView(child) };
        }),
      );
      this.emitComposition();
    } else {
      this.leafNode.set(node);
      this.nodeChange.emit(node);
    }
  }

  // ── composition callbacks ─────────────────────────────────────────────────

  onChildChange(id: string, node: ExpressionNodeRequest | null): void {
    if (node === null) {
      this.removeChild(id);
      return;
    }

    this.childNodes.set(id, node);
    this.emitComposition();
  }

  setOperator(op: 'AND' | 'OR'): void {
    this.compositionOperator.set(op);
    this.emitComposition();
  }

  addChild(): void {
    const id = String(this.nextId++);
    this.childNodes.set(id, null);
    this.children.update(list => [...list, { id, view: null }]);
    this.emitComposition();
  }

  removeChild(id: string): void {
    this.childNodes.delete(id);
    this.children.update(list => {
      const newList = list.filter(c => c.id !== id);

      if (newList.length === 0) {
        // Keep composition alive — add a fresh empty leaf instead of collapsing
        const freshId = String(this.nextId++);
        this.childNodes.set(freshId, null);
        this.nodeChange.emit({ type: 'COMPOSITION', operator: this.compositionOperator(), children: [] });
        return [{ id: freshId, view: null }];
      } else {
        // Just emit the new composition without auto-unwrapping
        const validChildren = newList
          .map(c => this.childNodes.get(c.id) ?? null)
          .filter((n): n is ExpressionNodeRequest => n !== null);

        this.nodeChange.emit({
          type: 'COMPOSITION',
          operator: this.compositionOperator(),
          children: validChildren,
        });
      }
      return newList;
    });
  }

  removeComposition(): void {
    this.isComposition.set(false);
    this.childNodes.clear();
    this.children.set([]);
    this.leafNode.set(null);
    this.nodeChange.emit(null);
  }

  private emitComposition(): void {
    const validChildren = this.children()
      .map(c => this.childNodes.get(c.id) ?? null)
      .filter((n): n is ExpressionNodeRequest => n !== null);

    // If depth 0, we force a refresh of the children signal reference 
    // to ensure computeNodeSpel (which might depend on deep child changes) triggers.
    if (this.depth === 0) {
      this.children.update(list => [...list]);
    }

    // Don't auto-unwrap here during initial emit or addChild
    this.nodeChange.emit({
      type: 'COMPOSITION',
      operator: this.compositionOperator(),
      children: validChildren,
    });
  }

}
