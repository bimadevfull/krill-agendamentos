import { createApp } from 'vue'
import { createPinia } from 'pinia'
import PrimeVue from 'primevue/config'
import ToastService from 'primevue/toastservice'
import ConfirmationService from 'primevue/confirmationservice'
import { definePreset } from '@primeuix/themes'
import Aura from '@primeuix/themes/aura'
import 'primeicons/primeicons.css'

import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import Password from 'primevue/password'
import Textarea from 'primevue/textarea'
import DatePicker from 'primevue/datepicker'
import Select from 'primevue/select'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Dialog from 'primevue/dialog'
import Tag from 'primevue/tag'
import Message from 'primevue/message'

import './style.css'
import App from './App.vue'
import router from './router'
import { ptBR } from './utils/locale'

const Krill = definePreset(Aura, {
  semantic: {
    primary: {
      50: '{blue.50}', 100: '{blue.100}', 200: '{blue.200}', 300: '{blue.300}', 400: '{blue.400}',
      500: '{blue.500}', 600: '{blue.600}', 700: '{blue.700}', 800: '{blue.800}', 900: '{blue.900}', 950: '{blue.950}'
    }
  }
})

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(PrimeVue, {
  locale: ptBR,
  theme: {
    preset: Krill,
    options: {
      darkModeSelector: 'none',
      cssLayer: { name: 'primevue', order: 'theme, base, primevue' }
    }
  }
})
app.use(ToastService)
app.use(ConfirmationService)

const componentes = { Button, InputText, InputNumber, Password, Textarea, DatePicker, Select, DataTable, Column, Dialog, Tag, Message }
Object.entries(componentes).forEach(([nome, comp]) => app.component(nome, comp))

app.mount('#app')
