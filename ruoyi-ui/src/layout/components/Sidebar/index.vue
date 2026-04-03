<template>
  <div
    class="custom-sidebar"
    :class="{ 'has-logo': showLogo }"
  >
    <!-- Logo -->
    <logo v-if="showLogo" :collapse="false" />

    <!-- 滚动区域 -->
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :unique-opened="true"
        :collapse-transition="false"
        mode="vertical"
      >
        <sidebar-item
          v-for="(route, index) in sidebarRouters"
          :key="route.path + index"
          :item="route"
          :base-path="route.path"
        />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script>
import {mapGetters, mapState} from "vuex"
import Logo from "./Logo"
import SidebarItem from "./SidebarItem"

export default {
  components: {SidebarItem, Logo},
  computed: {
    ...mapState(["settings"]),
    ...mapGetters(["sidebarRouters", "sidebar"]),

    // 当前激活菜单
    activeMenu() {
      const route = this.$route
      const {meta, path} = route
      if (meta.activeMenu) {
        return meta.activeMenu
      }
      return path
    },

    // 是否显示Logo
    showLogo() {
      return this.$store.state.settings.sidebarLogo
    },

    // 是否折叠
    isCollapse() {
      return !this.sidebar.opened
    }
  }
}
</script>

<style lang="scss" scoped>

.custom-sidebar {
  width: 220px;
  margin: 12px 0 12px 20px;
  height: calc(100vh - 24px);
  background: rgba(231, 237, 249) !important;
  backdrop-filter: blur(10px);
  border-radius: 16px;
  overflow: hidden;
}

/* ===== menu ===== */
.custom-sidebar ::v-deep(.el-menu) {
  background: transparent !important;
  border-right: none !important;
}

/* ===== menu item ===== */
.custom-sidebar ::v-deep(.el-menu-item) {
  background: transparent !important;  // 先确保背景透明
  color: #4D4D4D;
  border-radius: 8px;
  margin: 6px 12px;
  height: 42px;
  line-height: 42px;
  transition: all 0.2s;
}

/* hover */
.custom-sidebar ::v-deep(.el-menu-item:hover) {
  background: rgba(57, 118, 228, 0.12) !important;
  color: #3976E4 !important;
}

/* active */
.custom-sidebar ::v-deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #4e8cff, #6ea8ff) !important;
  color: #fff !important;
  box-shadow: 0 4px 12px rgba(57,118,228,0.3);
}

/* icon */
.custom-sidebar ::v-deep(.el-menu-item i) {
  color: #666;
}

/* active icon */
.custom-sidebar ::v-deep(.el-menu-item.is-active i) {
  color: #fff;
}

/* ===== submenu ===== */
.custom-sidebar ::v-deep(.el-submenu__title) {
  color: #4D4D4D;
  border-radius: 8px;
  margin: 6px 12px;
}

/* submenu hover */
.custom-sidebar ::v-deep(.el-submenu__title:hover) {
  background: rgba(57,118,228,0.1);
}

/* ===== 子菜单展开动画优化 ===== */
.custom-sidebar ::v-deep(.el-submenu.is-opened > .el-submenu__title) {
  color: #3976E4;
}

/* 箭头旋转 */
.custom-sidebar ::v-deep(.el-submenu__icon-arrow) {
  transition: transform 0.3s;
}

.custom-sidebar ::v-deep(.el-submenu.is-opened > .el-submenu__title .el-submenu__icon-arrow) {
  transform: rotate(180deg);
}

</style>
