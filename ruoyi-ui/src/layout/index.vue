<template>
  <div :class="classObj" class="app-wrapper" :style="{ '--current-color': theme }">

    <!-- ✅ 顶部蓝色背景 -->
    <div class="top-bg-container"></div>

    <!-- 移动端遮罩 -->
    <div v-if="device==='mobile'&&sidebar.opened" class="drawer-bg" @click="handleClickOutside"/>

    <!-- Sidebar -->
    <sidebar v-if="!sidebar.hide" class="sidebar-container"/>

    <!-- 主内容 -->
    <div :class="{hasTagsView:needTagsView,sidebarHide:sidebar.hide}" class="main-container">

      <!-- 顶部导航 -->
      <div :class="{'fixed-header':fixedHeader}">
        <navbar @setLayout="setLayout"/>
        <tags-view v-if="needTagsView"/>
      </div>

      <!-- 页面内容 -->
      <app-main/>

      <settings ref="settingRef"/>
    </div>
  </div>
</template>

<script>
import {AppMain, Navbar, Settings, Sidebar, TagsView} from './components'
import ResizeMixin from './mixin/ResizeHandler'
import {mapState} from 'vuex'

export default {
  name: 'Layout',
  components: {
    AppMain,
    Navbar,
    Settings,
    Sidebar,
    TagsView
  },
  mixins: [ResizeMixin],
  computed: {
    ...mapState({
      theme: state => state.settings.theme,
      sidebar: state => state.app.sidebar,
      device: state => state.app.device,
      needTagsView: state => state.settings.tagsView,
      fixedHeader: state => state.settings.fixedHeader
    }),
    classObj() {
      return {
        hideSidebar: !this.sidebar.opened,
        openSidebar: this.sidebar.opened,
        withoutAnimation: this.sidebar.withoutAnimation,
        mobile: this.device === 'mobile'
      }
    }
  },
  methods: {
    handleClickOutside() {
      this.$store.dispatch('app/closeSideBar', { withoutAnimation: false })
    },
    setLayout() {
      this.$refs.settingRef.openSetting()
    }
  }
}
</script>

<style lang="scss" scoped>
@import "~@/assets/styles/mixin.scss";

/* ===== 整体容器 ===== */
.app-wrapper {
  @include clearfix;
  //position: relative;
  height: 100%;
  width: 100%;
  //background: #F0F2F5; /* 灰底 */
}

/* ✅ 顶部蓝色背景 */
/* 蓝色底 */
.top-bg-container {
  width: 100%;
  height: 240px;
  position: absolute;
  top: 0;
  left: 0;
  background: #5387E4;
  z-index: -1;
}

/* 图片层 */
.top-bg-container::after {
  content: "";
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  background: url("~@/assets/images/background.png") no-repeat center/cover;
  background-position: center top; /* 让图片顶部对齐 */
  opacity: 0.8; /* 控制透明度（很关键） */
}

/* ===== 主内容区 ===== */
.main-container {
  position: relative;
  z-index: 1;
  margin: 20px 20px 0 0;
}

/* 内容区域增加间距 */
.app-main {
  padding: 20px;
}

/* ===== Header ===== */
.fixed-header {
  position: relative; /* ⚠️ 改成 relative（不再贴顶） */
  margin-bottom: 12px;
}

/* ===== 移动端遮罩 ===== */
.drawer-bg {
  background: #000;
  opacity: 0.3;
  width: 100%;
  top: 0;
  height: 100%;
  position: absolute;
  z-index: 999;
}

/* ===== 响应式 ===== */
.mobile .main-container {
  margin: 0;
}
</style>
