<script setup>
import { ref, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import { api, msgErro } from '../services/api'
import { useAuth } from '../stores/auth'
import { toIso, fmtData, fmtDataHora } from '../utils/format'

const toast = useToast()
const confirm = useConfirm()
const auth = useAuth()

const hoje = new Date()
const fimPadrao = new Date()
fimPadrao.setDate(hoje.getDate() + 7)

const inicio = ref(hoje)
const fim = ref(fimPadrao)
const status = ref(null)
const itens = ref([])
const carregando = ref(false)

const opcoesStatus = [
  { label: 'Todos', value: null },
  { label: 'Agendado', value: 'AGENDADO' },
  { label: 'Cancelado', value: 'CANCELADO' }
]

async function buscar() {
  if (!inicio.value || !fim.value) {
    toast.add({ severity: 'warn', summary: 'Informe o período', detail: 'Escolha a data inicial e a final.', life: 4000 })
    return
  }
  carregando.value = true
  try {
    const params = { inicio: toIso(inicio.value), fim: toIso(fim.value) }
    if (status.value) params.status = status.value
    itens.value = (await api.get('/agendamentos', { params })).data
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Erro na busca', detail: msgErro(e), life: 5000 })
  } finally {
    carregando.value = false
  }
}

function cancelar(item) {
  confirm.require({
    header: 'Cancelar agendamento',
    message: `Cancelar o agendamento do pedido ${item.idPedido} (${item.fornecedor})? Os pallets voltam a ficar disponíveis na faixa.`,
    acceptLabel: 'Cancelar agendamento',
    rejectLabel: 'Manter',
    acceptProps: { severity: 'danger' },
    rejectProps: { severity: 'secondary', outlined: true },
    accept: async () => {
      try {
        await api.patch(`/agendamentos/${item.id}/cancelar`)
        toast.add({ severity: 'success', summary: 'Cancelado', detail: 'Agendamento cancelado.', life: 3000 })
        await buscar()
      } catch (e) {
        toast.add({ severity: 'error', summary: 'Não foi possível cancelar', detail: msgErro(e), life: 6000 })
      }
    }
  })
}

onMounted(buscar)
</script>

<template>
  <div>
    <div class="flex flex-wrap items-center justify-between gap-3">
      <h1 class="text-2xl font-semibold">Agendamentos</h1>
      <router-link v-if="auth.isAdmin" to="/agendamentos/novo">
        <Button label="Novo agendamento" icon="pi pi-plus" />
      </router-link>
    </div>

    <form class="mt-5 flex flex-wrap items-end gap-3 rounded-lg border border-slate-200 bg-white p-4" @submit.prevent="buscar">
      <div>
        <label for="f-inicio" class="mb-1 block text-sm font-medium">De</label>
        <DatePicker v-model="inicio" inputId="f-inicio" dateFormat="dd/mm/yy" showIcon />
      </div>
      <div>
        <label for="f-fim" class="mb-1 block text-sm font-medium">Até</label>
        <DatePicker v-model="fim" inputId="f-fim" dateFormat="dd/mm/yy" showIcon />
      </div>
      <div>
        <label for="f-status" class="mb-1 block text-sm font-medium">Situação</label>
        <Select v-model="status" inputId="f-status" :options="opcoesStatus" optionLabel="label" optionValue="value" class="w-44" />
      </div>
      <Button type="submit" label="Buscar" icon="pi pi-search" :loading="carregando" />
    </form>

    <DataTable :value="itens" :loading="carregando" class="mt-5" size="small" stripedRows paginator :rows="15" scrollable>
      <template #empty>Nenhum agendamento neste período.</template>
      <Column header="Data" sortable sortField="data">
        <template #body="{ data }"><span class="num">{{ fmtData(data.data) }}</span></template>
      </Column>
      <Column field="faixaHoraria" header="Faixa" />
      <Column field="idPedido" header="Pedido" />
      <Column field="fornecedor" header="Fornecedor" />
      <Column field="emailFornecedor" header="E-mail" />
      <Column field="tipoCaminhao" header="Caminhão" />
      <Column field="tipoPaletizacao" header="Paletização" />
      <Column header="Pallets">
        <template #body="{ data }">
          <span class="num">{{ data.quantidadePallets }}</span>
          <span v-if="data.palletsConsumidos !== data.quantidadePallets" class="num text-xs text-slate-500">
            (ocupa {{ data.palletsConsumidos }})
          </span>
        </template>
      </Column>
      <Column header="Situação">
        <template #body="{ data }">
          <Tag :value="data.status === 'AGENDADO' ? 'Agendado' : 'Cancelado'" :severity="data.status === 'AGENDADO' ? 'success' : 'danger'" />
        </template>
      </Column>
      <Column header="Criado em">
        <template #body="{ data }"><span class="text-xs text-slate-500">{{ fmtDataHora(data.criadoEm) }} · {{ data.criadoPor }}</span></template>
      </Column>
      <Column v-if="auth.isAdmin" header="Ações">
        <template #body="{ data }">
          <Button v-if="data.status === 'AGENDADO'" label="Cancelar" icon="pi pi-times" severity="danger" text size="small" @click="cancelar(data)" />
        </template>
      </Column>
    </DataTable>
  </div>
</template>
