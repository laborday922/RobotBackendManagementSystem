import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

/* Layout */
import Layout from '@/layout'

/**
 * Note: 路由配置项
 *
 * hidden: true                     // 当设置 true 的时候该路由不会再侧边栏出现 如401，login等页面，或者如一些编辑页面/edit/1
 * alwaysShow: true                 // 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
 *                                  // 只有一个时，会将那个子路由当做根路由显示在侧边栏--如引导页面
 *                                  // 若你想不管路由下面的 children 声明的个数都显示你的根路由
 *                                  // 你可以设置 alwaysShow: true，这样它就会忽略之前定义的规则，一直显示根路由
 * redirect: noRedirect             // 当设置 noRedirect 的时候该路由在面包屑导航中不可被点击
 * name:'router-name'               // 设定路由的名字，一定要填写不然使用<keep-alive>时会出现各种问题
 * query: '{"id": 1, "name": "ry"}' // 访问路由的默认传递参数
 * roles: ['admin', 'common']       // 访问路由的角色权限
 * permissions: ['a:a:a', 'b:b:b']  // 访问路由的菜单权限
 * meta : {
    noCache: true                   // 如果设置为true，则不会被 <keep-alive> 缓存(默认 false)
    title: 'title'                  // 设置该路由在侧边栏和面包屑中展示的名字
    icon: 'svg-name'                // 设置该路由的图标，对应路径src/assets/icons/svg
    breadcrumb: false               // 如果设置为false，则不会在breadcrumb面包屑中显示
    activeMenu: '/system/user'      // 当路由设置了该属性，则会高亮相对应的侧边栏。
  }
 */

// 公共路由
export const constantRoutes = [
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect')
      }
    ]
  },
  {
    path: '/login',
    component: () => import('@/views/login'),
    hidden: true
  },
  {
    path: '/register',
    component: () => import('@/views/register'),
    hidden: true
  },
  {
    path: '/404',
    component: () => import('@/views/error/404'),
    hidden: true
  },
  {
    path: '/401',
    component: () => import('@/views/error/401'),
    hidden: true
  },
  {
    path: '',
    component: Layout,
    redirect: 'index',
    children: [
      {
        path: 'index',
        component: () => import('@/views/index'),
        name: 'Index',
        meta: { title: '首页', icon: 'dashboard', affix: true }
      }
    ]
  },
  {
    path: '/user',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'profile',
        component: () => import('@/views/system/user/profile/index'),
        name: 'Profile',
        meta: { title: '个人中心', icon: 'user' }
      }
    ]
  },
  {
    path: '/taskmgt',
    component: Layout,
    redirect: '/taskmgt/tasks',
    hidden: false,
    meta: { title: '任务管理', icon: 'job' },
    children: [
      {
        path: 'templates',
        name: 'TaskTemplate',
        component: () => import('@/views/taskmgt/TaskTemplate'),
        meta: { title: '模板管理', icon: 'documentation' }
      },
      {
        path: 'tasks',
        name: 'TaskList',
        component: () => import('@/views/taskmgt/TaskList'),
        meta: { title: '任务列表', icon: 'list' }
      },
      {
        path: 'schedule',
        name: 'TaskSchedule',
        component: () => import('@/views/taskmgt/TaskSchedule'),
        meta: { title: '计划管理', icon: 'time' }
      },
      {
        path: 'abnormal',
        name: 'TaskAbnormal',
        component: () => import('@/views/taskmgt/TaskAbnormal'),
        meta: { title: '异常处理', icon: 'bug' }
      }
    ]
  },
  // ========== 模式管理模块 ==========
  {
    path: '/mode',
    component: Layout,
    redirect: '/mode/quick',
    name: 'Mode',
    meta: {
      title: '模式管理',
      icon: 'el-icon-setting',
      roles: ['admin']
    },
    children: [
      {
        path: 'quick',
        component: () => import('@/views/mode/quick/index'),
        name: 'Quick',
        meta: {
          title: '快捷操作',
          icon: 'el-icon-thumb',
          noCache: true
        }
      },
      {
        path: 'switch',
        component: () => import('@/views/mode/switch/index'),
        name: 'Switch',
        meta: {
          title: '模式切换',
          icon: 'el-icon-refresh',
          noCache: true
        }
      },
      {
        path: 'schedule',
        component: () => import('@/views/mode/schedule/index'),
        name: 'Schedule',
        meta: {
          title: '模式排程',
          icon: 'el-icon-date',
          noCache: true
        }
      },
      {
        path: 'history',
        component: () => import('@/views/mode/history/index'),
        name: 'History',
        meta: {
          title: '历史记录',
          icon: 'el-icon-time',
          noCache: true
        }
      },
      {
        path: 'edit',
        component: () => import('@/views/mode/mode/index.vue'),
        name: 'Edit',
        meta: {
          title: '编辑模式',
          icon: 'el-icon-edit',
          noCache: true
        }
      }
    ]
  },
  // ========== 功能管理模块 ==========
  {
    path: '/func',
    component: Layout,
    hidden: false,
    redirect: 'noredirect',
    name: 'FuncManagement',
    meta: {
      title: '功能管理',
      icon: 'el-icon-collection'
    },
    children: [
      {
        path: 'reception',
        component: () => import('@/views/func/reception/index.vue'),
        name: 'Reception',
        meta: {
          title: '业务接待',
          icon: 'el-icon-service'
        }
      },
      {
        path: 'nav',
        component: () => import('@/views/func/nav/index.vue'),
        name: 'Nav',
        meta: {
          title: '导航指引',
          icon: 'el-icon-location'
        }
      },
      {
        path: 'tour',
        component: () => import('@/views/func/tour/index'),
        name: 'Tour',
        meta: {
          title: '智能讲解',
          icon: 'el-icon-microphone'
        }
      }
    ]
  }
]

// 动态路由，基于用户权限动态去加载
export const dynamicRoutes = [
  {
    path: '/system/user-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:user:edit'],
    children: [
      {
        path: 'role/:userId(\\d+)',
        component: () => import('@/views/system/user/authRole'),
        name: 'AuthRole',
        meta: { title: '分配角色', activeMenu: '/system/user' }
      }
    ]
  },
  {
    path: '/system/role-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:role:edit'],
    children: [
      {
        path: 'user/:roleId(\\d+)',
        component: () => import('@/views/system/role/authUser'),
        name: 'AuthUser',
        meta: { title: '分配用户', activeMenu: '/system/role' }
      }
    ]
  },
  {
    path: '/system/dict-data',
    component: Layout,
    hidden: true,
    permissions: ['system:dict:list'],
    children: [
      {
        path: 'index/:dictId(\\d+)',
        component: () => import('@/views/system/dict/data'),
        name: 'Data',
        meta: { title: '字典数据', activeMenu: '/system/dict' }
      }
    ]
  },
  {
    path: '/monitor/job-log',
    component: Layout,
    hidden: true,
    permissions: ['monitor:job:list'],
    children: [
      {
        path: 'index/:jobId(\\d+)',
        component: () => import('@/views/monitor/job/log'),
        name: 'JobLog',
        meta: { title: '调度日志', activeMenu: '/monitor/job' }
      }
    ]
  },
  {
    path: '/tool/gen-edit',
    component: Layout,
    hidden: true,
    permissions: ['tool:gen:edit'],
    children: [
      {
        path: 'index/:tableId(\\d+)',
        component: () => import('@/views/tool/gen/editTable'),
        name: 'GenEdit',
        meta: { title: '修改生成配置', activeMenu: '/tool/gen' }
      }
    ]
  }
]

// 防止连续点击多次路由报错
let routerPush = Router.prototype.push
let routerReplace = Router.prototype.replace
// push
Router.prototype.push = function push(location) {
  return routerPush.call(this, location).catch(err => err)
}
// replace
Router.prototype.replace = function push(location) {
  return routerReplace.call(this, location).catch(err => err)
}

export default new Router({
  mode: 'history', // 去掉url中的#
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
})
