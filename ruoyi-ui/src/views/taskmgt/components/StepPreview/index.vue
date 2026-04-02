<template>
  <div v-if="steps && steps.length > 0">
    <el-divider content-position="left">{{ title }}</el-divider>
    <div class="step-preview-container" :style="{ maxHeight: maxHeight + 'px' }">
      <div v-for="(step, index) in steps" :key="index" class="step-preview-item">
        <div class="step-preview-header">
          <span class="step-number">{{ index + 1 }}</span>
          <span class="step-name">{{ step.stepName || step.name }}</span>
        </div>
        <div class="step-preview-desc">{{ step.description || '暂无描述' }}</div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'StepPreview',
  props: {
    steps: {
      type: Array,
      default: () => []
    },
    title: {
      type: String,
      default: '作业步骤预览（自动填充）'
    },
    maxHeight: {
      type: Number,
      default: 300
    }
  }
}
</script>

<style scoped>
.step-preview-container {
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  padding: 15px;
  background: #fafafa;
  margin-bottom: 20px;
  overflow-y: auto;
}
.step-preview-item {
  background: white;
  padding: 12px;
  border-radius: 4px;
  margin-bottom: 10px;
  border: 1px solid #e8e8e8;
}
.step-preview-item:last-child {
  margin-bottom: 0;
}
.step-preview-header {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
  font-weight: bold;
}
.step-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  background: #409eff;
  color: white;
  border-radius: 50%;
  font-size: 12px;
  margin-right: 8px;
}
.step-name {
  color: #303133;
}
.step-preview-desc {
  color: #606266;
  font-size: 13px;
  line-height: 1.5;
  padding-left: 28px;
}
</style>
