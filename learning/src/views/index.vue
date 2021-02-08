<template>
<div>
    <div>欢迎</div>
    <button>Login<button>
    <button @click="routerHandle">Login</button>
</div>
</template>
<script>
export default {
    data () {
        return {}
    },
    methods: {
        routerHandle () {
            this.$router.push('/activity/index')
        }
    }
}
</script>