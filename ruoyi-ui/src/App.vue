<template>
  <div id="app">
    <div class="top-bg-container">
      <div class="top-bg-pattern"></div>
    </div>
    <theme-picker/>
    <router-view />
  </div>
</template>

<script>
import ThemePicker from "@/components/ThemePicker"

export default {
  name: "App",
  components: { ThemePicker }
}
</script>
<style scoped>
#app .theme-picker {
  display: none;
}
.top-bg-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 216px;           /* 根据设计调整高度 */
  background: #5387E4;     /* 主背景色 */
  z-index: -1;             /* 置于底层，保证内容可见 */
}

.top-bg-pattern {
  width: 100%;
  height: 100%;
  background: url('./assets/images/background.png') no-repeat center center / cover;
}
</style>
