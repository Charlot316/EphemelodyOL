import { createStore } from 'vuex'
import createPersistedState from "vuex-persistedstate"
export default createStore({
    state: {
        tagsList: [],
        collapse: false,
        islogin: false,
        user: {},
        role: 2,
        refreshRate: 90,
        volume: 100,
    },
    mutations: {
        login(state, user) {
            state.islogin = true
            state.user = user
        },
    },
    actions: {},
    modules: {},
    plugins: [createPersistedState()]
})