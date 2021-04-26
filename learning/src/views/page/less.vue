<template>
  <div>
      <h1>LESS</h1>
      <div>测试switch{{currentNumber}}</div>
  </div>
</template>

<script>
export default {
   data () {
       return {
           current: 5,
           treeData: [
               {
                   id: 1,
                   parentId: 0,
                   name: '测试name1'
               },
               {
                   id: 2,
                   parentId: 0,
                   name: '测试name2'
               },
               {
                   id: 3,
                   parentId: 0,
                   name: '测试name3'
               },
               {
                   id: 3,
                   parentId: 1,
                   name: '测试name3'
               }
           ]
       }
   },
   computed: {
        currentNumber () {
            let num = null;
            switch (this.current) {
                case 0:
                num = '参数是0';
                break;
                case 1:
                num = '参数是1';
                break;
                case 2:
                num = '参数是2';
                break;
                default:
                num = '为定义';
            }
            return num
        }
   },
   mounted () {
      this.test()
   },
   methods: {
       test () {
           let p = new Promise((resolve)=>{
               resolve(11)
           })
           const {res} = p
           console.log('Promise',res)
       }
   }
}
</script>