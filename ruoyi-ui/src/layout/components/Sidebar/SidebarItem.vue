<template>
  <div v-if="!item.hidden">

    <!-- ===== 单菜单 ===== -->
    <template v-if="hasOneShowingChild(item.children,item) && (!onlyOneChild.children||onlyOneChild.noShowingChildren)&&!item.alwaysShow">
      <app-link v-if="onlyOneChild.meta" :to="resolvePath(onlyOneChild.path, onlyOneChild.query)">
        <el-menu-item :index="resolvePath(onlyOneChild.path)" :class="{'submenu-title-noDropdown':!isNest}">
          <item :icon="onlyOneChild.meta.icon||(item.meta&&item.meta.icon)" :title="onlyOneChild.meta.title" />
        </el-menu-item>
      </app-link>
    </template>

    <!-- ===== 多级菜单 ===== -->
    <el-submenu v-else ref="subMenu" :index="resolvePath(item.path)" popper-append-to-body>

      <!-- 🔥 标题（重点改这里） -->
      <template slot="title">
        <div class="menu-title-wrapper">
          <item
            v-if="item.meta"
            :icon="item.meta && item.meta.icon"
            :title="item.meta.title"
          />

          <!-- ✅ 箭头 -->
<!--          <i-->
<!--            class="el-icon-arrow-right arrow-icon"-->
<!--            :class="{ rotated: isOpen }"-->
<!--          />-->
        </div>
      </template>

      <!-- 子菜单 -->
      <sidebar-item
        v-for="(child, index) in item.children"
        :key="child.path + index"
        :is-nest="true"
        :item="child"
        :base-path="resolvePath(child.path)"
        class="nest-menu"
      />

    </el-submenu>
  </div>
</template>

<script>
import path from 'path'
import {isExternal} from '@/utils/validate'
import Item from './Item'
import AppLink from './Link'
import FixiOSBug from './FixiOSBug'

export default {
  name: 'SidebarItem',
  components: { Item, AppLink },
  mixins: [FixiOSBug],
  props: {
    item: { type: Object, required: true },
    isNest: { type: Boolean, default: false },
    basePath: { type: String, default: '' }
  },
  data() {
    this.onlyOneChild = null
    return {}
  },

  computed: {
    // ✅ 判断是否展开（控制箭头旋转）
    isOpen() {
      return this.$route.path.startsWith(this.item.path)
    }
  },

  methods: {
    hasOneShowingChild(children = [], parent) {
      if (!children) children = []

      const showingChildren = children.filter(item => {
        if (item.hidden) return false
        this.onlyOneChild = item
        return true
      })

      if (showingChildren.length === 1) return true

      if (showingChildren.length === 0) {
        this.onlyOneChild = { ...parent, path: '', noShowingChildren: true }
        return true
      }

      return false
    },

    resolvePath(routePath, routeQuery) {
      if (isExternal(routePath)) return routePath
      if (isExternal(this.basePath)) return this.basePath

      if (routeQuery) {
        let query = JSON.parse(routeQuery)
        return { path: path.resolve(this.basePath, routePath), query }
      }

      return path.resolve(this.basePath, routePath)
    }
  }
}
</script>

<style lang="scss" scoped>

/* 标题区域 */
.menu-title-wrapper {
  display: flex;
  align-items: center;
  width: 100%;
}

/* 箭头 */
.arrow-icon {
  margin-left: auto;
  font-size: 12px;
  transition: transform 0.3s ease;
  color: #666;
}

/* 旋转 */
.arrow-icon.rotated {
  transform: rotate(90deg);
}

/* 子菜单缩进优化 */
.nest-menu .el-menu-item {
  padding-left: 48px !important;
  font-size: 13px;
}

::v-deep .el-submenu__icon-arrow {
  transition: transform 0.3s;
}

::v-deep .el-submenu.is-opened > .el-submenu__title .el-submenu__icon-arrow {
  transform: rotate(180deg);
}

</style>
