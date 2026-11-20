<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../stores/auth'
import { msgErro } from '../services/api'

const auth = useAuth()
const router = useRouter()
const email = ref('')
const senha = ref('')
const erro = ref('')
const carregando = ref(false)

async function entrar() {
  erro.value = ''
  if (!email.value || !senha.value) {
    erro.value = 'Informe e-mail e senha.'
    return
  }
  carregando.value = true
  try {
    await auth.login(email.value, senha.value)
    router.push('/')
  } catch (e) {
    erro.value = msgErro(e)
  } finally {
    carregando.value = false
  }
}
</script>

<template>
  <main class="grid min-h-screen md:grid-cols-2">
    <section class="hidden flex-col justify-between p-10 text-white md:flex" style="background: var(--krill-ink)">
      <span class="text-lg font-semibold tracking-wide">Rede Krill</span>
      <div>
        <h1 class="max-w-sm text-4xl font-semibold leading-tight">Cada caminhão no horário certo.</h1>
        <p class="mt-4 max-w-sm text-slate-300">
          Agende recebimentos, acompanhe a ocupação do dia e das faixas e avise o fornecedor em um só lugar.
        </p>
      </div>
      <span class="text-sm text-slate-400">Recebimento de mercadorias</span>
    </section>

    <section class="flex items-center justify-center p-6">
      <form class="w-full max-w-sm space-y-4" @submit.prevent="entrar">
        <h2 class="text-2xl font-semibold">Entrar</h2>
        <div>
          <label for="email" class="mb-1 block text-sm font-medium">E-mail</label>
          <InputText id="email" v-model="email" type="email" autocomplete="username" fluid />
        </div>
        <div>
          <label for="senha" class="mb-1 block text-sm font-medium">Senha</label>
          <Password inputId="senha" v-model="senha" :feedback="false" toggleMask autocomplete="current-password" fluid />
        </div>
        <Message v-if="erro" severity="error" size="small">{{ erro }}</Message>
        <Button type="submit" label="Entrar" :loading="carregando" class="w-full" />
      </form>
    </section>
  </main>
</template>
