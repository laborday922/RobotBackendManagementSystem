<template>
  <div class="sidebar-logo-container" :class="{'collapse':collapse}" :style="{ backgroundColor: sideTheme === 'theme-dark' && navType !== 3 ? variables.menuBackground : variables.menuLightBackground }">
    <transition name="sidebarLogoFade">
      <router-link v-if="collapse" key="collapse" class="sidebar-logo-link" to="/">
        <img v-if="logo" :src="logo" class="sidebar-logo" />
        <h1 v-else class="sidebar-title" :style="{ color: sideTheme === 'theme-dark' && navType !== 3 ? variables.logoTitleColor : variables.logoLightTitleColor }">{{ title }} </h1>
      </router-link>
      <router-link v-else key="expand" class="sidebar-logo-link" to="/">
        <h1 class="sidebar-title" :style="{ color: sideTheme === 'theme-dark' && navType !== 3 ? variables.logoTitleColor : variables.logoLightTitleColor }">{{ title }} </h1>
      </router-link>
    </transition>
  </div>
</template>

<script>
import logoImg from '@/assets/logo/logo.png'
import variables from '@/assets/styles/custom-theme.scss'

export default {
  name: 'SidebarLogo',
  props: {
    collapse: {
      type: Boolean,
      required: true
    }
  },
  computed: {
    variables() {
      return variables
    },
    sideTheme() {
      return this.$store.state.settings.sideTheme
    },
    navType() {
      return this.$store.state.settings.navType
    }
  },
  data() {
    return {
      title: '机器人管理系统',
      logo: logoImg
    }
  }
}
</script>

<style lang="scss" scoped>
.sidebarLogoFade-enter-active {
  transition: opacity 1.5s;
}

.sidebarLogoFade-enter,
.sidebarLogoFade-leave-to {
  opacity: 0;
}

.sidebar-logo-container {
  height: 60px;

  /* ❗改成透明玻璃 */
  background: transparent !important;

  display: flex;
  align-items: center;
  justify-content: center;

  & .sidebar-logo-link {
    display: flex;
    align-items: center;      // 保证图片和文字在交叉轴居中
    justify-content: center;
    text-decoration: none;
    line-height: 1;

    & .sidebar-logo {
      width: 36px;
      height: 36px;
      border-radius: 10px;
      vertical-align: middle;  // 明确图片垂直对齐方式
    }

    & .sidebar-title {
      font-size: 16px;
      font-weight: 600;
      color: #3976E4 !important;
      margin-left: 40px;               // 移除 h1 默认边距
      line-height: 36px;       // 设置与图片高度相同的行高，保证完全居中
      white-space: nowrap;     // 防止文字换行（可选）
    }
  }

  &.collapse {
    .sidebar-logo {
      margin-right: 0px;
    }
  }
}
</style>
