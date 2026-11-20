<script setup>
import { ref, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import { api, msgErro } from '../services/api'
import { useAuth } from '../stores/auth'
import { fmtHora } from '../utils/format'

const props = defineProps({
  title: { type: String, required: true },
  endpoint: { type: String, required: true },
  fields: { type: Array, required: true },
  hint: { type: String, default: '' }
})

const toast = useToast()
const confirm = useConfirm()
const auth = useAuth()

const itens = ref([])
const carregando = ref(false)
const dialog = ref(false)
const editandoId = ref(null)
const form = ref({})
const salvando = ref(false)

const exibir = (campo, valor) => (campo.type === 'time' ? fmtHora(valor) : valor)

async function carregar() {
  carregando.value = true
  try {
    itens.value = (await api.get(props.endpoint)).data
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Erro ao carregar', detail: msgErro(e), life: 5000 })
  } finally {
    carregando.value = false
  }
}

function abrirNovo() {
  editandoId.value = null
  form.value = Object.fromEntries(props.fields.map((f) => [f.key, f.type === 'number' ? null : '']))
  dialog.value = true
}

function abrirEdicao(item) {
  editandoId.value = item.id
  form.value = Object.fromEntries(
    props.fields.map((f) => [f.key, f.type === 'time' ? fmtHora(item[f.key]) : item[f.key]])
  )
  dialog.value = true
}

async function salvar() {
  salvando.value = true
  try {
    if (editandoId.value) await api.put(`${props.endpoint}/${editandoId.value}`, form.value)
    else await api.post(props.endpoint, form.value)
    dialog.value = false
    toast.add({ severity: 'success', summary: 'Salvo', detail: 'Cadastro salvo com sucesso.', life: 3000 })
    await carregar()
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Não foi possível salvar', detail: msgErro(e), life: 6000 })
  } finally {
    salvando.value = false
  }
}

function excluir(item) {
  confirm.require({
    header: 'Excluir cadastro',
    message: 'Esta ação não pode ser desfeita. Deseja excluir este registro?',
    acceptLabel: 'Excluir',
    rejectLabel: 'Manter',
    acceptProps: { severity: 'danger' },
    rejectProps: { severity: 'secondary', outlined: true },
    accept: async () => {
      try {
        await api.delete(`${props.endpoint}/${item.id}`)
        toast.add({ severity: 'success', summary: 'Excluído', detail: 'Registro excluído.', life: 3000 })
        await carregar()
      } catch (e) {
        toast.add({ severity: 'error', summary: 'Não foi possível excluir', detail: msgErro(e), life: 6000 })
      }
    }
  })
}

onMounted(carregar)
</script>

<template>
  <div>
    <div class="flex flex-wrap items-center justify-between gap-3">
      <div>
        <h1 class="text-2xl font-semibold">{{ title }}</h1>
        <p v-if="hint" class="mt-1 text-sm text-slate-600">{{ hint }}</p>
      </div>
      <Button v-if="auth.isAdmin" label="Novo cadastro" icon="pi pi-plus" @click="abrirNovo" />
    </div>

    <DataTable :value="itens" :loading="carregando" class="mt-5" size="small" stripedRows>
      <template #empty>Nenhum registro cadastrado. Use "Novo cadastro" para começar.</template>
      <Column v-for="f in fields" :key="f.key" :field="f.key" :header="f.label">
        <template #body="{ data }">{{ exibir(f, data[f.key]) }}</template>
      </Column>
      <Column v-if="auth.isAdmin" header="Ações" style="width: 8rem">
        <template #body="{ data }">
          <Button icon="pi pi-pencil" text rounded aria-label="Editar" @click="abrirEdicao(data)" />
          <Button icon="pi pi-trash" text rounded severity="danger" aria-label="Excluir" @click="excluir(data)" />
        </template>
      </Column>
    </DataTable>

    <Dialog v-model:visible="dialog" modal :header="editandoId ? 'Editar cadastro' : 'Novo cadastro'" :style="{ width: '26rem' }">
      <div class="space-y-4">
        <div v-for="f in fields" :key="f.key">
          <label :for="`campo-${f.key}`" class="mb-1 block text-sm font-medium">{{ f.label }}</label>
          <InputNumber v-if="f.type === 'number'" v-model="form[f.key]" :inputId="`campo-${f.key}`" :min="f.min" :useGrouping="false" fluid />
          <InputText v-else-if="f.type === 'time'" v-model="form[f.key]" :id="`campo-${f.key}`" type="time" fluid />
          <InputText v-else v-model="form[f.key]" :id="`campo-${f.key}`" :maxlength="f.max" fluid />
        </div>
      </div>
      <template #footer>
        <Button label="Cancelar" severity="secondary" text @click="dialog = false" />
        <Button label="Salvar" icon="pi pi-check" :loading="salvando" @click="salvar" />
      </template>
    </Dialog>
  </div>
</template>
