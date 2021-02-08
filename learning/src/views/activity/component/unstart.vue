<template>
    <div>
      <Table row-key="id" :load-data="handleLoadData" :columns="columns16" :data="data13" border></Table>
    </div>
</template>
<script>
export default {
    props: {
        type: {
            type: String,
            required: true,
            default: ''
        }
    },
    data () {
        return {
 columns16: [
                    {
                        title: 'Name',
                        key: 'name',
                        tree: true
                    },
                    {
                        title: 'Age',
                        key: 'age'
                    },
                    {
                        title: 'Address',
                        key: 'address'
                    }
                ],
                data13: [
                    {
                        id: '100',
                        name: 'John Brown',
                        age: 18,
                        address: 'New York No. 1 Lake Park'
                    },
                    {
                        id: '101',
                        name: 'Jim Green',
                        age: 24,
                        address: 'London No. 1 Lake Park',
                        _loading: false,
                        children: []
                    },
                    {
                        id: '102',
                        name: 'Joe Black',
                        age: 30,
                        address: 'Sydney No. 1 Lake Park'
                    },
                    {
                        id: '103',
                        name: 'Jon Snow',
                        age: 26,
                        address: 'Ottawa No. 2 Lake Park'
                    }
                ]
        }
    },
    methods: {
        handleLoadData (row,cb) {
            console.log(row)
            const data = [
                        {
                            id: '10100',
                            name: 'John Brown',
                            age: 18,
                            address: 'New York No. 1 Lake Park'
                        },
                        {
                            id: '10101',
                            name: 'Joe Blackn',
                            age: 30,
                            address: 'Sydney No. 1 Lake Park'
                        }
                    ];
            cb(data)
        }
    }
}
</script>