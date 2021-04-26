
const moduleA = {
    namespaced: true,
    state: {
        name: 'moduleA'
    },
    mutations: {
        moduleAChange(state,name) {
            state.name = name
        }
    },
    actions: {
        actionAname ({commit},name) {
            commit('moduleAChange',name)
        }
    }
}
export default moduleA