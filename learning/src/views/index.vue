<template>
<div>
    <div>欢迎</div>
    <button @click="routerHandle('index')">Login</button>
    <button @click="routerHandle('test1')">test1</button>
    <button @click="routerHandle('less1')">less1</button>
     <button @click="routerHandle('table')">table</button>
     <button @click="routerHandle('store')">store</button>
</div>
</template>
<script>
export default {
    data () {
        return {
            pageConfig: {
                index: '/activity/index',
                test1: '/test1',
                less1: '/less',
                table: '/table',
                store: '/store'
            }
        }
    },
    methods: {
        routerHandle (config) {
            let url = this.pageConfig[config]
            this.$router.push(url)
        }
    }
}
</script>