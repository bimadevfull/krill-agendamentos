<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../stores/auth'

const auth = useAuth()
const router = useRouter()

const itens = [
  { to: '/agendamentos', label: 'Agendamentos', icon: 'pi pi-calendar' },
  { to: '/ocupacao', label: 'Ocupação', icon: 'pi pi-chart-bar' },
  { to: '/agendamentos/novo', label: 'Novo agendamento', icon: 'pi pi-plus-circle', admin: true },
  { to: '/capacidade', label: 'Capacidade', icon: 'pi pi-gauge', admin: true },
  { to: '/faixas', label: 'Faixas de horário', icon: 'pi pi-clock', admin: true },
  { to: '/paletizacao', label: 'Paletização', icon: 'pi pi-box', admin: true },
  { to: '/caminhoes', label: 'Caminhões', icon: 'pi pi-truck', admin: true },
  { to: '/auditoria', label: 'Auditoria', icon: 'pi pi-shield', admin: true }
]
const menu = computed(() => itens.filter((i) => !i.admin || auth.isAdmin))

function sair() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <div class="flex min-h-screen flex-col md:flex-row">
    <aside class="flex shrink-0 flex-col text-slate-200 md:w-60" style="background: var(--krill-ink)">
      <div class="px-5 py-4 text-lg font-semibold text-white">Rede Krill</div>
      <nav class="flex gap-1 overflow-x-auto px-2 pb-2 md:flex-1 md:flex-col md:overflow-visible" aria-label="Principal">
        <router-link
          v-for="i in menu"
          :key="i.to"
          :to="i.to"
          exact-active-class="!bg-[var(--krill-yellow)] !text-slate-900 font-semibold"
          class="flex items-center gap-2 whitespace-nowrap rounded-md px-3 py-2 text-sm hover:bg-white/10"
        >
          <i :class="i.icon" aria-hidden="true" />
          {{ i.label }}
        </router-link>
      </nav>
      <div class="border-t border-white/10 px-4 py-3 text-sm">
        <div class="font-medium text-white">{{ auth.user?.nome }}</div>
        <div class="text-xs text-slate-400">{{ auth.isAdmin ? 'Administrador' : 'Usuário comum' }}</div>
        <button class="mt-2 text-xs text-slate-300 underline hover:text-white" @click="sair">Sair</button>
      </div>
    </aside>

    <main class="min-w-0 flex-1 p-4 md:p-8">
      <router-view />
    </main>
  </div>
</template>
