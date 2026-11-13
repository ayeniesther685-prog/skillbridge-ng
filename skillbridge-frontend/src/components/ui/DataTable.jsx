// Table on md+, stacked cards on mobile. columns: [{ key, header, render?(row) }]
export default function DataTable({ columns, rows, onRowClick }) {
  const cell = (c, r) => (c.render ? c.render(r) : r[c.key] ?? '—')
  return (
    <>
      <div className="hidden overflow-x-auto rounded-lg border md:block">
        <table className="w-full text-left text-sm">
          <thead className="border-b bg-surface text-muted"><tr>{columns.map((c) => <th key={c.key} className="px-4 py-3 font-medium">{c.header}</th>)}</tr></thead>
          <tbody>
            {rows.map((r, i) => (
              <tr key={r.id ?? i} onClick={onRowClick ? () => onRowClick(r) : undefined} className={`border-b last:border-0 ${onRowClick ? 'cursor-pointer hover:bg-fg/5' : ''}`}>
                {columns.map((c) => <td key={c.key} className="px-4 py-3 align-top">{cell(c, r)}</td>)}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div className="space-y-3 md:hidden">
        {rows.map((r, i) => (
          <div key={r.id ?? i} onClick={onRowClick ? () => onRowClick(r) : undefined} className="space-y-2 rounded-lg border bg-card p-4 text-sm">
            {columns.map((c) => <div key={c.key} className="flex justify-between gap-4"><span className="text-muted">{c.header}</span><span className="text-right">{cell(c, r)}</span></div>)}
          </div>
        ))}
      </div>
    </>
  )
}
