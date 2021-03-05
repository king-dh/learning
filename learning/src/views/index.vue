<template>
<div>
    <div>欢迎</div>
    <button @click="routerHandle('index')">Login</button>
    <button @click="routerHandle('test1')">test1</button>
</div>
</template>
<script>
export default {
    data () {
        return {
            pageConfig: {
                index: '/activity/index',
                test1: '/test1'
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