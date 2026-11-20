<script setup>
import { computed } from 'vue'
import { percentual, corOcupacao, fmtData } from '../utils/format'

const props = defineProps({
  ocupacao: { type: Object, required: true },
  extra: { type: Number, default: 0 }, // pallets do agendamento em edicao (projecao)
  faixaId: { type: Number, default: null }
})

const dia = computed(() => {
  const usado = props.ocupacao.usado + props.extra
  return { usado, limite: props.ocupacao.limite, pct: percentual(usado, props.ocupacao.limite) }
})

const faixas = computed(() =>
  props.ocupacao.faixas.map((f) => {
    const usado = f.usado + (f.faixaId === props.faixaId ? props.extra : 0)
    return { ...f, usado, pct: percentual(usado, f.limite) }
  })
)

const largura = (pct) => `${Math.min(Math.max(pct, 0), 100)}%`
</script>

<template>
  <section class="rounded-lg border border-slate-200 bg-white p-4" aria-label="Ocupação">
    <h3 class="text-sm font-semibold text-slate-600">Ocupação em {{ fmtData(ocupacao.data) }}</h3>

    <div class="mt-3">
      <div class="flex items-baseline justify-between">
        <span class="font-medium">Dia</span>
        <span class="num text-sm">
          {{ dia.usado }} / {{ dia.limite }} pallets
          <strong :style="{ color: corOcupacao(dia.pct) }">{{ dia.pct }}%</strong>
        </span>
      </div>
      <div class="mt-1 h-2.5 overflow-hidden rounded-full bg-slate-200">
        <div class="h-full rounded-full transition-all" :style="{ width: largura(dia.pct), background: corOcupacao(dia.pct) }" />
      </div>
      <p v-if="dia.pct > 100" class="mt-1 text-xs" style="color: var(--krill-over)">Acima da capacidade diária</p>
    </div>

    <ul class="mt-5 space-y-3">
      <li v-for="f in faixas" :key="f.faixaId" :class="f.faixaId === faixaId ? 'rounded-md bg-blue-50 p-2 -m-2' : ''">
        <div class="flex items-baseline justify-between">
          <span class="text-sm">{{ f.descricao }}</span>
          <span class="num text-sm">
            {{ f.usado }} / {{ f.limite }}
            <strong :style="{ color: corOcupacao(f.pct) }">{{ f.pct }}%</strong>
          </span>
        </div>
        <div class="mt-1 h-2 overflow-hidden rounded-full bg-slate-200">
          <div class="h-full rounded-full transition-all" :style="{ width: largura(f.pct), background: corOcupacao(f.pct) }" />
        </div>
        <p v-if="f.pct > 100" class="mt-1 text-xs" style="color: var(--krill-over)">Acima da capacidade da faixa</p>
      </li>
    </ul>
  </section>
</template>
