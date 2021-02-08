<template>
    <div>
<Table :columns="columns9" :data="data1"></Table>
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
           columns9: [
                    {
                        title: 'Name2',
                        key: 'name'
                    },
                    {
                        title: 'Age2',
                        key: 'age',
                        className: 'demo-table-info-column'
                    },
                    {
                        title: 'Address2',
                        key: 'address'
                    }
                ],
                data1: [
                    {
                        name: 'John Brown',
                        age: 18,
                        address: 'New York No. 1 Lake Park',
                        date: '2016-10-03'
                    },
                    {
                        name: 'Jim Green',
                        age: 24,
                        address: 'London No. 1 Lake Park',
                        date: '2016-10-01'
                    },
                    {
                        name: 'Joe Black',
                        age: 30,
                        address: 'Sydney No. 1 Lake Park',
                        date: '2016-10-02'
                    },
                    {
                        name: 'Jon Snow',
                        age: 26,
                        address: 'Ottawa No. 2 Lake Park',
                        date: '2016-10-04'
                    }
                ]
       }
    }
}
</script>