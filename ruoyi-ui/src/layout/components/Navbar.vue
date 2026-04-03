<template>
  <div class="navbar">

    <!-- ❌ 不要折叠按钮了 -->

    <!-- 面包屑 -->
    <breadcrumb class="breadcrumb-container" />

    <!-- 右侧 -->
    <div class="right-menu">

      <!-- 图标区 -->
      <div class="header-icons">
        <i class="fas fa-search"></i>
        <i class="fas fa-bell"></i>
        <i class="fas fa-cog"></i>
        <div class="divider"></div>
      </div>

      <!-- 用户 -->
      <el-dropdown class="avatar-container" trigger="hover">
        <div class="avatar-wrapper">
          <img :src="avatar" class="user-avatar">
          <span class="user-nickname">{{ nickName }}</span>
        </div>

        <el-dropdown-menu slot="dropdown">
          <router-link to="/user/profile">
            <el-dropdown-item>个人中心</el-dropdown-item>
          </router-link>

          <el-dropdown-item divided @click.native="logout">
            退出登录
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>

    </div>
  </div>
</template>

<script>
import {mapGetters} from 'vuex'
import Breadcrumb from '@/components/Breadcrumb'

export default {
  components: {
    Breadcrumb
  },
  computed: {
    ...mapGetters([
      'avatar',
      'nickName'
    ])
  },
  methods: {
    logout() {
      this.$confirm('确定退出吗？', '提示')
        .then(() => {
          this.$store.dispatch('LogOut').then(() => {
            location.href = '/index'
          })
        })
    }
  }
}
</script>

<style lang="scss" scoped>

.navbar {
  height: 50px;
  display: flex;
  align-items: center;
  padding: 0 16px;

  /* ✅ 完全透明（关键） */
  background: transparent !important;
  box-shadow: none !important;

  margin-bottom: 12px;

  .breadcrumb-container {
    flex: 1;
    padding-left: 20px
  }

  /* ===== 右侧 ===== */
  .right-menu {
    display: flex;
    align-items: center;
  }

  /* 图标区 */
  .header-icons {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-right: 12px;

    i {
      font-size: 16px;
      color: #fff;   /* ❗ 白色（配蓝背景） */
      opacity: 0.8;
      cursor: pointer;

      &:hover {
        opacity: 1;
      }
    }
  }

  .divider {
    width: 1px;
    height: 20px;
    background: rgba(255,255,255,0.4);
  }

  /* 用户 */
  .avatar-container {
    .avatar-wrapper {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;

      .user-avatar {
        width: 30px;
        height: 30px;
        border-radius: 50%;
      }

      .user-nickname {
        font-size: 14px;
        font-weight: 500;
        color: #fff;  /* ❗ 白色 */
      }
    }
  }
}
</style>
