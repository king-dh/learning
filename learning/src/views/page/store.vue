<template>
    <div class="store">
        <h1>STOTE</h1>
        <p>{{$store.state.name}}</p>
        <p>{{$store.state.a.name}}</p>
        <p @click="changeName">改变名字</p>
        <p @click="modulea">改变modulea名字</p>
    </div>
</template>
<script>
export default {
    data(){
        return {}
    },
    methods: {
        changeName () {
            this.$store.dispatch('HandleChangeName',{name: 'dave',phone: '15267352676'})
        },
        modulea () {
            this.$store.dispatch('a/actionAname','dave111')
        }
    }
}
</script>