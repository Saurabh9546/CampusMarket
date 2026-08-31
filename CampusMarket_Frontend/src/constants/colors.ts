/**
 * Mirrors src/styles/variables.css. Use this when a color value is needed in
 * JS/TS (e.g. passed as a prop to an SVG icon) rather than in a stylesheet —
 * keep the two in sync if the palette changes.
 */
export const COLORS = {
  ink: '#16243D',
  inkSoft: '#3E4C63',
  paper: '#F6F5F1',
  card: '#FFFFFF',
  accent: '#2F6F4E',
  accentDark: '#1F4D36',
  accentTint: '#E6EFE9',
  stamp: '#B23A2E',
  stampTint: '#F7E7E4',
  amber: '#B87A1E',
  amberTint: '#FBF0DD',
  grayTint: '#EDECE7',
  line: '#DEDBD1',
  lineStrong: '#C7C3B6',
  muted: '#6E6D65',
} as const;
