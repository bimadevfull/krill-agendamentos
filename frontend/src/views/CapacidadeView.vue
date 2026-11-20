<script setup>
import { ref, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import { api, msgErro } from '../services/api'

const toast = useToast()
const form = ref({ palletsPorDia: null, palletsPorFaixa: null })
const salvando = ref(false)

onMounted(async () => {
  try {
    form.value = (await api.get('/configuracao-capacidade')).data
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Erro ao carregar', detail: msgErro(e), life: 5000 })
  }
})

async function salvar() {
  salvando.value = true
  try {
    await api.put('/configuracao-capacidade', {
      palletsPorDia: form.value.palletsPorDia,
      palletsPorFaixa: form.value.palletsPorFaixa
    })
    toast.add({ severity: 'success', summary: 'Capacidade atualizada', detail: 'Os novos limites já valem para as próximas consultas.', life: 4000 })
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Não foi possível salvar', detail: msgErro(e), life: 6000 })
  } finally {
    salvando.value = false
  }
}
</script>

<template>
  <div>
    <h1 class="text-2xl font-semibold">Capacidade</h1>
    <p class="mt-1 max-w-xl text-sm text-slate-600">
      Os limites avisam quando uma data ou faixa passa da capacidade, mas nunca impedem o cadastro do agendamento.
    </p>
    <form class="mt-5 max-w-md space-y-4 rounded-lg border border-slate-200 bg-white p-5" @submit.prevent="salvar">
      <div>
        <label for="c-dia" class="mb-1 block text-sm font-medium">Pallets por dia</label>
        <InputNumber v-model="form.palletsPorDia" inputId="c-dia" :min="1" :useGrouping="false" fluid />
      </div>
      <div>
        <label for="c-faixa" class="mb-1 block text-sm font-medium">Pallets por faixa horária</label>
        <InputNumber v-model="form.palletsPorFaixa" inputId="c-faixa" :min="1" :useGrouping="false" fluid />
      </div>
      <Button type="submit" label="Salvar limites" icon="pi pi-check" :loading="salvando" />
    </form>
  </div>
</template>
