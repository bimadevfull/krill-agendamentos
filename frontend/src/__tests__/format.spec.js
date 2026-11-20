import { describe, it, expect } from 'vitest'
import { toIso, fromIso, fmtData, fmtHora, consumo, percentual, corOcupacao } from '../utils/format'

describe('format', () => {
  it('converte Date para yyyy-mm-dd no fuso local', () => {
    expect(toIso(new Date(2026, 10, 5))).toBe('2026-11-05')
  })

  it('converte yyyy-mm-dd para Date local', () => {
    const d = fromIso('2026-11-20')
    expect([d.getFullYear(), d.getMonth(), d.getDate()]).toEqual([2026, 10, 20])
  })

  it('formata data e hora', () => {
    expect(fmtData('2026-11-20')).toBe('20/11/2026')
    expect(fmtData('')).toBe('')
    expect(fmtHora('08:00:00')).toBe('08:00')
  })

  it('nao paletizado dobra o consumo', () => {
    expect(consumo(10, 1)).toBe(10)
    expect(consumo(10, 2)).toBe(20)
    expect(consumo(null, 2)).toBe(0)
  })

  it('calcula percentual com uma casa decimal', () => {
    expect(percentual(60, 120)).toBe(50)
    expect(percentual(130, 120)).toBe(108.3)
    expect(percentual(10, 0)).toBe(0)
  })

  it('escolhe a cor conforme a ocupacao', () => {
    expect(corOcupacao(50)).toContain('ok')
    expect(corOcupacao(85)).toContain('warn')
    expect(corOcupacao(101)).toContain('over')
  })
})
