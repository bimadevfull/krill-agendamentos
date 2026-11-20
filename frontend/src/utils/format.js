const pad = (n) => String(n).padStart(2, '0')

/** Date -> 'yyyy-mm-dd' usando o fuso local (evita o deslocamento do toISOString). */
export const toIso = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`

/** 'yyyy-mm-dd' -> Date local. */
export const fromIso = (s) => {
  const [y, m, d] = s.split('-').map(Number)
  return new Date(y, m - 1, d)
}

export const fmtData = (s) => (s ? s.split('-').reverse().join('/') : '')
export const fmtDataHora = (s) => (s ? new Date(s).toLocaleString('pt-BR') : '')
export const fmtHora = (s) => (s ? String(s).slice(0, 5) : '')

/** Pallets ocupados = quantidade x fator da paletizacao. */
export const consumo = (quantidade, fator) => (Number(quantidade) || 0) * (Number(fator) || 0)

export const percentual = (usado, limite) =>
  limite > 0 ? Math.round((usado * 1000) / limite) / 10 : 0

/** Cor da barra de ocupacao: verde, ambar (>= 80%) ou vermelho (acima de 100%). */
export const corOcupacao = (pct) => {
  if (pct > 100) return 'var(--krill-over)'
  if (pct >= 80) return 'var(--krill-warn)'
  return 'var(--krill-ok)'
}
