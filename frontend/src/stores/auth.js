import { defineStore } from 'pinia'
import { api } from '../services/api'

const lerUsuario = () => {
  try {
    return JSON.parse(localStorage.getItem('user') || 'null')
  } catch {
    return null
  }
}

export const useAuth = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token'),
    user: lerUsuario()
  }),
  getters: {
    logado: (s) => !!s.token,
    isAdmin: (s) => s.user?.perfil === 'ADMIN'
  },
  actions: {
    async login(email, senha) {
      const { data } = await api.post('/auth/login', { email, senha })
      this.token = data.token
      this.user = { nome: data.nome, email: data.email, perfil: data.perfil }
      localStorage.setItem('token', data.token)
      localStorage.setItem('user', JSON.stringify(this.user))
    },
    logout() {
      this.token = null
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
