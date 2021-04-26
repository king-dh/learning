import Vue from 'vue';
import Vuex from 'vuex';
import moduleA from './module/moduleA'
import moduleB from './module/moduleB'
Vue.use(Vuex)
export default new Vuex.Store({
    state: {
        name: '张三丰'
    },
    mutations: {
        change_name (state,name) {
            state.name = name
        }
    },
    actions: {
        HandleChangeName ({commit},params) {
            console.log('HandleChangeName',params,context)
            commit('change_name',params.name)
        }
    },
    modules: {
        a: moduleA,
        b: moduleB
    }
})