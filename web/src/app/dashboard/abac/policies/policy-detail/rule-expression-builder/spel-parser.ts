import { ConditionRow, ConditionType } from './rule-expression-builder';

/**
 * Splits a SpEL string by top-level && or || operators,
 * ignoring operators inside parentheses, brackets, or single-quoted strings.
 * Returns { parts, operator } where operator is the detected top-level operator.
 */
function splitByTopLevelOperator(spel: string): { parts: string[]; op: 'AND' | 'OR' } | null {
  const s = spel.trim();
  let depth = 0;
  let inString = false;
  let i = 0;
  const parts: string[] = [];
  let current = '';
  let detectedOp: 'AND' | 'OR' | null = null;

  while (i < s.length) {
    const ch = s[i];

    if (ch === "'" && !inString) { inString = true; current += ch; i++; continue; }
    if (ch === "'" && inString)  { inString = false; current += ch; i++; continue; }
    if (inString) { current += ch; i++; continue; }

    if (ch === '(' || ch === '[' || ch === '{') { depth++; current += ch; i++; continue; }
    if (ch === ')' || ch === ']' || ch === '}') { depth--; current += ch; i++; continue; }

    if (depth === 0) {
      // check &&
      if (s.slice(i, i + 2) === '&&') {
        if (detectedOp && detectedOp !== 'AND') return null; // mixed operators
        detectedOp = 'AND';
        parts.push(current.trim());
        current = '';
        i += 2;
        continue;
      }
      // check ||
      if (s.slice(i, i + 2) === '||') {
        if (detectedOp && detectedOp !== 'OR') return null; // mixed operators
        detectedOp = 'OR';
        parts.push(current.trim());
        current = '';
        i += 2;
        continue;
      }
    }
    current += ch;
    i++;
  }

  if (current.trim()) parts.push(current.trim());
  return { parts, op: detectedOp ?? 'AND' };
}

/** Extracts a single-quoted string value from a SpEL expression, e.g. 'MANAGER' → MANAGER */
function extractQuoted(s: string): string | null {
  const m = s.match(/^'(.*)'$/);
  return m ? m[1] : null;
}

/**
 * Attempts to parse a single SpEL condition part into a ConditionRow.
 * Returns null if the pattern is not recognized.
 */
export function tryParseCondition(part: string): ConditionRow | null {
  const s = part.trim();

  // subject.roles.contains('X')
  const roleM = s.match(/^subject\.roles\.contains\('(.+)'\)$/);
  if (roleM) return row('subject_has_role', { role: roleM[1] });

  // subject.getAttribute('A') == 'V'
  const saEqM = s.match(/^subject\.getAttribute\('(.+)'\)\s*==\s*'(.+)'$/);
  if (saEqM) return row('subject_attr_equals', { attribute: saEqM[1], value: saEqM[2] });

  // subject.getAttribute('A').contains('V')
  const saContM = s.match(/^subject\.getAttribute\('(.+)'\)\.contains\('(.+)'\)$/);
  if (saContM) return row('subject_attr_contains', { attribute: saContM[1], value: saContM[2] });

  // action.getAttribute('name') == 'X'
  const actionEqM = s.match(/^action\.getAttribute\('name'\)\s*==\s*'(.+)'$/);
  if (actionEqM) return row('action_is', { action: actionEqM[1] });

  // #{'X','Y',...}.contains(action.getAttribute('name'))
  const actionOneOfM = s.match(/^#\{((?:'[^']*'(?:,'[^']*')*)?)\}\.contains\(action\.getAttribute\('name'\)\)$/);
  if (actionOneOfM) {
    const actions = actionOneOfM[1].split(',').map(a => a.replace(/'/g, '').trim()).join(',');
    return row('action_is_one_of', { actions });
  }

  // object.data == null || <condition>  (navigation_or_instance)
  const navOrM = s.match(/^object\.data\s*==\s*null\s*\|\|\s*(.+)$/);
  if (navOrM) return row('navigation_or_instance', { instanceCondition: navOrM[1].trim() });

  // object.data == null  (navigation_only)
  if (s === 'object.data == null') return row('navigation_only', {});

  // object.data.F == 'V'
  const ifEqM = s.match(/^object\.data\.(\w+)\s*==\s*'(.+)'$/);
  if (ifEqM) return row('instance_field_equals', { field: ifEqM[1], value: ifEqM[2] });

  // object.data.F.contains('V')
  const ifContM = s.match(/^object\.data\.(\w+)\.contains\('(.+)'\)$/);
  if (ifContM) return row('instance_field_contains', { field: ifContM[1], value: ifContM[2] });

  // subject.getAttribute('A').contains(object.data.F)
  const sacfM = s.match(/^subject\.getAttribute\('(.+)'\)\.contains\(object\.data\.(\w+)\)$/);
  if (sacfM) return row('subject_attr_contains_field', { attribute: sacfM[1], field: sacfM[2] });

  // subject.getAttribute('A') == object.data.F
  const saefM = s.match(/^subject\.getAttribute\('(.+)'\)\s*==\s*object\.data\.(\w+)$/);
  if (saefM) return row('subject_attr_equals_field', { attribute: saefM[1], field: saefM[2] });

  return null;
}

function row(type: ConditionType, params: Record<string, string>): ConditionRow {
  return { id: String(Math.random()), type, params };
}

/**
 * Attempts to parse a SpEL string into ConditionRows.
 * Returns null if any part fails to parse (falls back to raw mode).
 */
export function parseToConditionRows(spel: string): { rows: ConditionRow[]; op: 'AND' | 'OR' } | null {
  if (!spel || !spel.trim()) return null;

  const split = splitByTopLevelOperator(spel.trim());
  if (!split) return null;

  const rows: ConditionRow[] = [];
  for (const part of split.parts) {
    const r = tryParseCondition(part);
    if (r === null) return null;
    rows.push(r);
  }

  return rows.length > 0 ? { rows, op: split.op } : null;
}

/**
 * Detects whether a SpEL string is representable as visual builder mode.
 */
export function detectBuilderMode(spel: string | null | undefined): 'builder' | 'raw' {
  if (!spel || !spel.trim()) return 'builder';
  return parseToConditionRows(spel) !== null ? 'builder' : 'raw';
}
