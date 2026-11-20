import { createRouter, createWebHistory } from 'vue-router'
import { useAuth } from '../stores/auth'
import LoginView from '../views/LoginView.vue'
import AppLayout from '../views/AppLayout.vue'
import AgendamentosView from '../views/AgendamentosView.vue'
import NovoAgendamentoView from '../views/NovoAgendamentoView.vue'
import OcupacaoView from '../views/OcupacaoView.vue'
import CapacidadeView from '../views/CapacidadeView.vue'
import AuditoriaView from '../views/AuditoriaView.vue'
import CrudPage from '../components/CrudPage.vue'

const routes = [
  { path: '/login', component: LoginView, meta: { public: true } },
  {
    path: '/',
    component: AppLayout,
    redirect: '/agendamentos',
    children: [
      { path: 'agendamentos', component: AgendamentosView },
      { path: 'agendamentos/novo', component: NovoAgendamentoView, meta: { admin: true } },
      { path: 'ocupacao', component: OcupacaoView },
      {
        path: 'caminhoes',
        component: CrudPage,
        meta: { admin: true },
        props: {
          title: 'Tipos de caminhão',
          endpoint: '/tipos-caminhao',
          fields: [{ key: 'nome', label: 'Nome', type: 'text', max: 50 }]
        }
      },
      {
        path: 'paletizacao',
        component: CrudPage,
        meta: { admin: true },
        props: {
          title: 'Tipos de paletização',
          endpoint: '/tipos-paletizacao',
          hint: 'O fator multiplica os pallets ocupados na faixa. Não paletizado usa fator 2 (dobro).',
          fields: [
            { key: 'nome', label: 'Nome', type: 'text', max: 50 },
            { key: 'fatorPallets', label: 'Fator de pallets', type: 'number', min: 1 }
          ]
        }
      },
      {
        path: 'faixas',
        component: CrudPage,
        meta: { admin: true },
        props: {
          title: 'Faixas de horário',
          endpoint: '/faixas-horarias',
          hint: 'São 4 faixas de 2 horas cada, sem sobreposição.',
          fields: [
            { key: 'descricao', label: 'Descrição', type: 'text', max: 50 },
            { key: 'horaInicio', label: 'Início', type: 'time' },
            { key: 'horaFim', label: 'Fim', type: 'time' }
          ]
        }
      },
      { path: 'capacidade', component: CapacidadeView, meta: { admin: true } },
      { path: 'auditoria', component: AuditoriaView, meta: { admin: true } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to) => {
  const auth = useAuth()
  if (!to.meta.public && !auth.logado) return '/login'
  if (to.path === '/login' && auth.logado) return '/'
  if (to.meta.admin && !auth.isAdmin) return '/agendamentos'
})

export default router
