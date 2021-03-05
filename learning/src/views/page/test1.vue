<template>
  <div>
      <a-button type="primary" icon="search">search</a-button>
      <a-button type="primary" :ghost="true" loading>
          searcch
      </a-button>
      <a-row type="flex" justify="space-between">
        <a-col :span="4">a-col-4</a-col>
        <a-col :span="4">a-col-4</a-col>
      </a-row>
      <a-row>
          <a-col :span="24">
              <a-pagination 
                 v-model="current"
                :total="total" 
                :showLessItems="true" 
                :showQuickJumper="true" 
                :showSizeChanger="true"
                :pageSize="pageSize"
                show-size-changer
                @showSizeChange="onShowSizeChange"
                @change="change" />
          </a-col>
      </a-row>
      <div>
          <a-steps :current="currents">
            <a-step v-for="item in steps" :key="item.title" :title="item.title" />
          </a-steps>
      </div>
      <div>时间</div>
      <a-range-picker
      v-model="dateRange" 
      format="YYYY-MM-DD HH:mm:ss"
      :placeholder="['开始','22']"
      style="width: 360px" 
      @change="rangePicker" />
  </div>
</template>

<script>
export default {
    data () {
        return {
            current: 1,
            total: 200,
            pageSize: 10,
            currents: 1,
            placeholder: ['ds','22'],
            steps: [
                        {
                        title: 'First',
                        content: 'First-content',
                        },
                        {
                        title: 'Second',
                        content: 'Second-content',
                        },
                        {
                        title: 'Last',
                        content: 'Last-content',
                        },
            ],
            dateRange: [],
        }
    },
    methods: {
        change (page,pageSize) {
            this.page = page;
            this.pageSize = pageSize;
            console.log('change',this.page,this.pageSize)
        },
         onShowSizeChange(current, pageSize) {
        this.pageSize = pageSize;
        },
        rangePicker (date,dateString) {
            
            // this.dateRange = [
            //     this.$moment(`${dateString[0]} 00:00:00`,'YYYY:MM:DD HH:mm:ss'),
            //     this.$moment(`${dateString[1]} 23:59:59`,'YYYY:MM:DD HH:mm:ss')
            // ]
            this.dateRange = [dateString[0].slice(0,11) + '00:00:00', dateString[1].slice(0,11) + '11:11:11']
            console.log(dateString,this.dateRange)
        }
    }
}
</script>

<style>

</style>