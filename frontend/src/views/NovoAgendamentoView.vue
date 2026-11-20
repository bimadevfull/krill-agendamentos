<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { api, msgErro } from '../services/api'
import { toIso, consumo } from '../utils/format'
import OcupacaoPanel from '../components/OcupacaoPanel.vue'

const router = useRouter()
const toast = useToast()

const faixas = ref([])
const caminhoes = ref([])
const paletizacoes = ref([])
const ocupacao = ref(null)
const salvando = ref(false)
const erro = ref('')

const form = ref({
  data: null,
  faixaHorariaId: null,
  idPedido: null,
  fornecedor: '',
  emailFornecedor: '',
  tipoCaminhaoId: null,
  tipoPaletizacaoId: null,
  quantidadePallets: null,
  observacao: ''
})

onMounted(async () => {
  try {
    const [f, c, p] = await Promise.all([
      api.get('/faixas-horarias'),
      api.get('/tipos-caminhao'),
      api.get('/tipos-paletizacao')
    ])
    faixas.value = f.data
    caminhoes.value = c.data
    paletizacoes.value = p.data
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Erro ao carregar cadastros', detail: msgErro(e), life: 6000 })
  }
})

async function carregarOcupacao() {
  if (!form.value.data) {
    ocupacao.value = null
    return
  }
  try {
    ocupacao.value = (await api.get('/ocupacao', { params: { data: toIso(form.value.data) } })).data
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Erro ao consultar ocupação', detail: msgErro(e), life: 5000 })
  }
}
watch(() => form.value.data, carregarOcupacao)

// Faixa horaria com a ocupacao (%) do dia escolhido
const opcoesFaixa = computed(() =>
  faixas.value.map((f) => {
    const o = ocupacao.value?.faixas.find((x) => x.faixaId === f.id)
    return { id: f.id, label: o ? `${f.descricao}  ·  ${o.percentual}% ocupada` : f.descricao }
  })
)

const fator = computed(() => paletizacoes.value.find((p) => p.id === form.value.tipoPaletizacaoId)?.fatorPallets || 0)
const extra = computed(() => consumo(form.value.quantidadePallets, fator.value))

const previsao = computed(() => {
  if (!ocupacao.value || !extra.value) return []
  const avisos = []
  if (ocupacao.value.usado + extra.value > ocupacao.value.limite) {
    avisos.push(`Este agendamento deixa o dia acima da capacidade (${ocupacao.value.usado + extra.value} de ${ocupacao.value.limite} pallets).`)
  }
  const faixa = ocupacao.value.faixas.find((f) => f.faixaId === form.value.faixaHorariaId)
  if (faixa && faixa.usado + extra.value > faixa.limite) {
    avisos.push(`Este agendamento deixa a faixa ${faixa.descricao} acima da capacidade (${faixa.usado + extra.value} de ${faixa.limite} pallets).`)
  }
  return avisos
})

function validar() {
  const f = form.value
  const faltando = []
  if (!f.data) faltando.push('data')
  if (!f.faixaHorariaId) faltando.push('faixa horária')
  if (!f.idPedido) faltando.push('ID do pedido')
  if (!f.fornecedor.trim()) faltando.push('fornecedor')
  if (!f.emailFornecedor.trim()) faltando.push('e-mail do fornecedor')
  if (!f.tipoCaminhaoId) faltando.push('tipo de caminhão')
  if (!f.tipoPaletizacaoId) faltando.push('tipo de paletização')
  if (!f.quantidadePallets) faltando.push('quantidade de pallets')
  return faltando
}

async function salvar() {
  erro.value = ''
  const faltando = validar()
  if (faltando.length) {
    erro.value = `Preencha: ${faltando.join(', ')}.`
    return
  }
  salvando.value = true
  try {
    const { data } = await api.post('/agendamentos', { ...form.value, data: toIso(form.value.data) })
    data.avisos.forEach((aviso) =>
      toast.add({ severity: 'warn', summary: 'Capacidade excedida', detail: aviso, life: 9000 })
    )
    toast.add({
      severity: 'success',
      summary: 'Agendamento cadastrado',
      detail: 'O fornecedor receberá o e-mail de confirmação.',
      life: 5000
    })
    router.push('/agendamentos')
  } catch (e) {
    erro.value = msgErro(e)
  } finally {
    salvando.value = false
  }
}
</script>

<template>
  <div>
    <h1 class="text-2xl font-semibold">Novo agendamento</h1>

    <div class="mt-5 grid gap-6 lg:grid-cols-5">
      <form class="space-y-4 rounded-lg border border-slate-200 bg-white p-5 lg:col-span-3" @submit.prevent="salvar">
        <div class="grid gap-4 sm:grid-cols-2">
          <div>
            <label for="a-data" class="mb-1 block text-sm font-medium">Data *</label>
            <DatePicker v-model="form.data" inputId="a-data" dateFormat="dd/mm/yy" showIcon fluid />
          </div>
          <div>
            <label for="a-faixa" class="mb-1 block text-sm font-medium">Faixa horária *</label>
            <Select v-model="form.faixaHorariaId" inputId="a-faixa" :options="opcoesFaixa" optionLabel="label" optionValue="id" placeholder="Selecione" fluid />
          </div>
        </div>

        <div class="grid gap-4 sm:grid-cols-2">
          <div>
            <label for="a-pedido" class="mb-1 block text-sm font-medium">ID do pedido *</label>
            <InputNumber v-model="form.idPedido" inputId="a-pedido" :useGrouping="false" :min="1" fluid />
          </div>
          <div>
            <label for="a-qtd" class="mb-1 block text-sm font-medium">Quantidade de pallets *</label>
            <InputNumber v-model="form.quantidadePallets" inputId="a-qtd" :useGrouping="false" :min="1" fluid />
          </div>
        </div>

        <div>
          <label for="a-fornecedor" class="mb-1 block text-sm font-medium">Fornecedor *</label>
          <InputText id="a-fornecedor" v-model="form.fornecedor" maxlength="80" fluid />
        </div>
        <div>
          <label for="a-email" class="mb-1 block text-sm font-medium">E-mail do fornecedor *</label>
          <InputText id="a-email" v-model="form.emailFornecedor" type="email" maxlength="80" fluid />
        </div>

        <div class="grid gap-4 sm:grid-cols-2">
          <div>
            <label for="a-caminhao" class="mb-1 block text-sm font-medium">Tipo de caminhão *</label>
            <Select v-model="form.tipoCaminhaoId" inputId="a-caminhao" :options="caminhoes" optionLabel="nome" optionValue="id" placeholder="Selecione" fluid />
          </div>
          <div>
            <label for="a-palet" class="mb-1 block text-sm font-medium">Tipo de paletização *</label>
            <Select v-model="form.tipoPaletizacaoId" inputId="a-palet" :options="paletizacoes" optionLabel="nome" optionValue="id" placeholder="Selecione" fluid />
          </div>
        </div>
        <p v-if="fator > 1" class="text-sm text-slate-600">
          Carga não paletizada: cada pallet conta {{ fator }}x na ocupação da faixa
          <span v-if="extra">({{ extra }} pallets ocupados)</span>.
        </p>

        <div>
          <label for="a-obs" class="mb-1 block text-sm font-medium">Observação</label>
          <Textarea id="a-obs" v-model="form.observacao" rows="3" maxlength="500" fluid />
        </div>

        <Message v-for="p in previsao" :key="p" severity="warn" size="small">
          {{ p }} Você pode continuar: o limite é apenas um aviso.
        </Message>
        <Message v-if="erro" severity="error" size="small">{{ erro }}</Message>

        <div class="flex gap-2">
          <Button type="submit" label="Cadastrar agendamento" icon="pi pi-check" :loading="salvando" />
          <router-link to="/agendamentos"><Button type="button" label="Voltar" severity="secondary" text /></router-link>
        </div>
      </form>

      <div class="lg:col-span-2">
        <OcupacaoPanel v-if="ocupacao" :ocupacao="ocupacao" :extra="extra" :faixaId="form.faixaHorariaId" />
        <p v-else class="rounded-lg border border-dashed border-slate-300 p-5 text-sm text-slate-600">
          Escolha a data para ver a ocupação do dia e de cada faixa horária.
        </p>
      </div>
    </div>
  </div>
</template>
