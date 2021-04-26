// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import ViewUI from 'view-design';
import 'view-design/dist/styles/iview.css';
import Antd from 'ant-design-vue';
import 'ant-design-vue/dist/antd.css';
import moment from "moment";
import store from './store/index'

Vue.use(ViewUI);

Vue.use(Antd);

Vue.prototype.$moment = moment;

Vue.config.productionTip = false

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  components: { App },
  template: '<App/>'
})
