<script setup>
import { ref, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import { api, msgErro } from '../services/api'
import { toIso, fmtDataHora } from '../utils/format'

const toast = useToast()
const hoje = new Date()
const inicio = ref(new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate() - 7))
const fim = ref(hoje)
const usuario = ref('')
const logs = ref([])
const carregando = ref(false)

async function buscar() {
  if (!inicio.value || !fim.value) return
  carregando.value = true
  try {
    const params = { inicio: toIso(inicio.value), fim: toIso(fim.value) }
    if (usuario.value.trim()) params.usuario = usuario.value.trim()
    logs.value = (await api.get('/auditoria', { params })).data
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Erro na busca', detail: msgErro(e), life: 5000 })
  } finally {
    carregando.value = false
  }
}

const severidade = (acao) =>
  ({ CRIAR: 'success', ATUALIZAR: 'info', EXCLUIR: 'danger', CANCELAR: 'danger', LOGIN_FALHA: 'warn' })[acao] || 'secondary'

onMounted(buscar)
</script>

<template>
  <div>
    <h1 class="text-2xl font-semibold">Auditoria</h1>
    <p class="mt-1 text-sm text-slate-600">Ações executadas no sistema. Mostra até 500 registros mais recentes do período.</p>

    <form class="mt-5 flex flex-wrap items-end gap-3 rounded-lg border border-slate-200 bg-white p-4" @submit.prevent="buscar">
      <div>
        <label for="l-inicio" class="mb-1 block text-sm font-medium">De</label>
        <DatePicker v-model="inicio" inputId="l-inicio" dateFormat="dd/mm/yy" showIcon />
      </div>
      <div>
        <label for="l-fim" class="mb-1 block text-sm font-medium">Até</label>
        <DatePicker v-model="fim" inputId="l-fim" dateFormat="dd/mm/yy" showIcon />
      </div>
      <div>
        <label for="l-usuario" class="mb-1 block text-sm font-medium">Usuário</label>
        <InputText id="l-usuario" v-model="usuario" placeholder="e-mail" />
      </div>
      <Button type="submit" label="Buscar" icon="pi pi-search" :loading="carregando" />
    </form>

    <DataTable :value="logs" :loading="carregando" class="mt-5" size="small" stripedRows paginator :rows="20" scrollable>
      <template #empty>Nenhum registro neste período.</template>
      <Column header="Data e hora">
        <template #body="{ data }"><span class="num">{{ fmtDataHora(data.dataHora) }}</span></template>
      </Column>
      <Column field="usuario" header="Usuário" />
      <Column header="Ação">
        <template #body="{ data }"><Tag :value="data.acao" :severity="severidade(data.acao)" /></template>
      </Column>
      <Column field="recurso" header="Recurso" />
      <Column field="detalhe" header="Detalhe" />
      <Column field="status" header="HTTP" />
      <Column field="ip" header="IP" />
    </DataTable>
  </div>
</template>
