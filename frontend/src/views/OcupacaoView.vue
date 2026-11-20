<script setup>
import { ref, watch, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import { api, msgErro } from '../services/api'
import { toIso } from '../utils/format'
import OcupacaoPanel from '../components/OcupacaoPanel.vue'

const toast = useToast()
const data = ref(new Date())
const ocupacao = ref(null)

async function carregar() {
  if (!data.value) return
  try {
    ocupacao.value = (await api.get('/ocupacao', { params: { data: toIso(data.value) } })).data
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Erro ao consultar ocupação', detail: msgErro(e), life: 5000 })
  }
}

watch(data, carregar)
onMounted(carregar)
</script>

<template>
  <div>
    <h1 class="text-2xl font-semibold">Ocupação</h1>
    <div class="mt-5 max-w-xs">
      <label for="o-data" class="mb-1 block text-sm font-medium">Data</label>
      <DatePicker v-model="data" inputId="o-data" dateFormat="dd/mm/yy" showIcon fluid />
    </div>
    <div class="mt-5 max-w-xl">
      <OcupacaoPanel v-if="ocupacao" :ocupacao="ocupacao" />
    </div>
  </div>
</template>
