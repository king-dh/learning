<template>
    <div>
        <p>活动列表</p>
        <Tabs value="unStart" :animated="false" @on-click="tabChange">
            <TabPane label="未开始" name="unStart"></TabPane>
            <TabPane label="进行中" name="processing"></TabPane>
            <TabPane label="已结束" name="ending"></TabPane>
        </Tabs>
        <component
        :is="currentTab"
        :type="type"
        ></component>
    </div>
</template>
<script>
import Unstart from './component/unstart'
import Processing from './component/processing'
import Ending from './component/ending'
export default {
    components:{Unstart,Processing,Ending},
    data () {
        return {
          type: 'unStart'
        }
    },
    computed: {
        currentTab () {
            return this.type == 'unStart' ? Unstart : this.type == 'processing' ? Processing : Ending
        }
    },
    methods: {
        tabChange (value) {
            this.type = value
        }
    }
}
</script>