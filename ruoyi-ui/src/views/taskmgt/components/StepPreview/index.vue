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

        <!-- 组任务时显示机器人选择 -->
        <div v-if="isGroupTask" class="step-robot-select">
          <el-select
            v-model="step.assignedRobotId"
            placeholder="请选择机器人"
            size="small"
            clearable
            @change="onRobotChange(step, $event)"
          >
            <el-option
              v-for="robot in availableRobots"
              :key="robot.id"
              :label="robot.name"
              :value="robot.id"
            >
              <span>{{ robot.name }}</span>
              <span style="float: right; color: #8492a6; font-size: 12px">
                电量{{ robot.battery }}%
              </span>
            </el-option>
          </el-select>
        </div>
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
    },
    isGroupTask: {
      type: Boolean,
      default: false
    },
    availableRobots: {
      type: Array,
      default: () => []
    }
  },
  methods: {
    onRobotChange(step, robotId) {
      this.$emit('robot-change', { step, robotId })
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
  margin-bottom: 8px;
}
.step-robot-select {
  padding-left: 28px;
  margin-top: 8px;
}
</style>
