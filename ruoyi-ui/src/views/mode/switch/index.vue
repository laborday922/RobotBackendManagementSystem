<template>
  <div class="card">
    <div class="card-header">
      <div class="card-title">
        <i class="fas fa-exchange-alt"></i> 模式切换
      </div>
      <el-tag type="info" size="small">当前生效模式: {{ activeModeName }}</el-tag>
    </div>

    <div class="card-body">
      <!-- 机器人选择 -->
      <div class="filter-bar">
        <span><i class="fas fa-robot"></i> 选择目标机器人</span>
        <el-select v-model="selectedRobotId" placeholder="请选择机器人" filterable clearable style="flex: 1;">
          <el-option label="所有机器人" value="all"></el-option>
          <el-option
            v-for="robot in robotOptions"
            :key="robot.robotId"
            :label="robot.name"
            :value="robot.robotId">
            <span style="float: left">{{ robot.name }}</span>
            <span style="float: right; color: #8492a6; font-size: 13px">
              <el-tag :type="getStatusType(robot.status)" size="mini">
                {{ getStatusText(robot.status) }}
              </el-tag>
              <span style="margin-left: 5px;">当前模式: {{ robot.currentModeName || '未知' }}</span>
            </span>
          </el-option>
        </el-select>
      </div>

      <!-- 模式切换按钮组 -->
      <div class="mode-buttons">
        <div class="mode-btn" v-for="mode in modeOptions" :key="mode.modeId">
          <div
            class="mode-btn-inner"
            :class="{ active: selectedModeId === mode.modeId }"
            :style="{ borderColor: selectedModeId === mode.modeId ? mode.modeColor : '' }"
            @click="selectMode(mode)">
            <i :class="mode.modeIcon" :style="{ color: selectedModeId === mode.modeId ? 'white' : mode.modeColor }"></i>
            <span class="mode-name">{{ mode.modeName }}</span>
            <span class="mode-desc">{{ mode.description }}</span>
          </div>
        </div>
      </div>

      <!-- 模式配置区域 - 动态生成 -->
      <div class="sub-card" v-if="selectedMode">
        <div class="sub-card-header">
          <span><i class="fas fa-sliders-h"></i> {{ selectedMode.modeName }} 配置</span>
        </div>

        <el-form :model="configData" label-width="140px">
          <!-- 动态生成参数配置项 -->
          <template v-if="selectedMode.modeParams && selectedMode.modeParams.length > 0">
            <div v-for="param in selectedMode.modeParams" :key="param.paramId" class="param-config-item">
              <el-form-item :label="param.paramName + (param.paramUnit ? ` (${param.paramUnit})` : '')">
                <!-- 根据参数类型显示不同的控件 -->
                <template v-if="param.paramType === 'boolean'">
                  <el-switch
                    v-model="configData[`param_${param.paramId}`]"
                    active-text="开启"
                    inactive-text="关闭">
                  </el-switch>
                  <span class="param-desc" v-if="param.paramDescription">{{ param.paramDescription }}</span>
                </template>

                <template v-else-if="param.paramType === 'number' || param.paramType === 'range'">
                  <el-slider
                    v-model="configData[`param_${param.paramId}`]"
                    :min="param.paramMin || 0"
                    :max="param.paramMax || 100"
                    show-input
                    :format-tooltip="val => val + (param.paramUnit || '')">
                  </el-slider>
                  <span class="param-desc" v-if="param.paramDescription">{{ param.paramDescription }}</span>
                </template>

                <template v-else-if="param.paramType === 'select'">
                  <el-select
                    v-model="configData[`param_${param.paramId}`]"
                    placeholder="请选择"
                    style="width: 100%;">
                    <el-option
                      v-for="opt in getParamOptions(param)"
                      :key="opt.value"
                      :label="opt.label"
                      :value="opt.value">
                    </el-option>
                  </el-select>
                  <span class="param-desc" v-if="param.paramDescription">{{ param.paramDescription }}</span>
                </template>

                <template v-else>
                  <el-input
                    v-model="configData[`param_${param.paramId}`]"
                    :placeholder="`请输入${param.paramName}`"
                    clearable>
                  </el-input>
                  <span class="param-desc" v-if="param.paramDescription">{{ param.paramDescription }}</span>
                </template>
              </el-form-item>
            </div>
          </template>

          <!-- 如果没有参数配置，显示提示 -->
          <div v-else class="no-params-tip">
            <i class="el-icon-info"></i> 该模式暂无配置参数，可直接切换
          </div>
        </el-form>

        <div class="config-actions">
          <el-button @click="resetConfig">重置</el-button>
          <el-button type="primary" @click="saveConfig">保存配置</el-button>
        </div>
      </div>

      <!-- 切换确认区域 -->
      <div class="sub-card">
        <div class="switch-info">
          <i class="fas fa-info-circle"></i>
          <span>{{ getSwitchWarning() }}</span>
        </div>
        <div class="switch-actions">
          <el-button type="primary" size="large" @click="confirmSwitch" :disabled="!selectedModeId || !selectedRobotId">
            <i class="fas fa-check"></i> 确认切换模式
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { listRobot, updateRobotMode } from "@/api/mode/robot";
import { listMode, getMode } from "@/api/mode/mode";
import { addHistory } from "@/api/mode/history";

export default {
  name: "ModeSwitch",
  data() {
    return {
      // 选中的机器人ID
      selectedRobotId: null,
      // 选中的模式ID
      selectedModeId: null,
      // 机器人列表
      robotOptions: [],
      // 模式列表（包含参数）
      modeOptions: [],
      // 配置数据（动态存储）
      configData: {},
      // 保存的配置（用于重置）
      savedConfigData: {},
      // 加载状态
      loading: false
    };
  },
  computed: {
    /** 当前选中的模式 */
    selectedMode() {
      return this.modeOptions.find(m => m.modeId === this.selectedModeId);
    },
    /** 当前生效模式名称 */
    activeModeName() {
      if (this.selectedModeId) {
        const mode = this.modeOptions.find(m => m.modeId === this.selectedModeId);
        return mode ? mode.modeName : '待机模式';
      }
      return '待机模式';
    }
  },
  created() {
    this.initData();
  },
  methods: {
    /** 初始化数据 */
    async initData() {
      await this.getModeList();
      await this.getRobotList();
    },
    /** 查询机器人列表 - 修复版 */
    getRobotList() {
      return listRobot({ pageNum: 1, pageSize: 100 }).then(response => {
        console.log('原始机器人数据:', response.rows);
if (response.rows && response.rows[0]) {
      console.log('第一个机器人的所有字段:', Object.keys(response.rows[0]));
      console.log('currentMode:', response.rows[0].currentMode);
      console.log('current_mode:', response.rows[0].current_mode);
    }
        // 确保每个机器人都有正确的 robotId
        this.robotOptions = response.rows.map(robot => ({
          ...robot,
          robotId: robot.robotId || robot.id,  // 兼容两种字段名
          robotName: robot.robotName || robot.name,
          name: robot.robotName || robot.name
        }));

        console.log('处理后的机器人列表:', this.robotOptions);

        // 更新每个机器人的当前模式名称
        this.updateRobotModeNames();
      }).catch(error => {
        console.error('获取机器人列表失败', error);
        this.$message.error('获取机器人列表失败');
      });
    },
    /** 更新所有机器人的模式名称 */
    updateRobotModeNames() {
      this.robotOptions.forEach(robot => {
        const mode = this.modeOptions.find(m => m.modeId === robot.currentMode);
        robot.currentModeName = mode ? mode.modeName : '未知';
      });
    },
    /** 查询模式列表（包含参数） */
    getModeList() {
      return listMode({ pageNum: 1, pageSize: 100, enabled: '1' }).then(response => {
        // 为每个模式加载参数配置
        const modePromises = response.rows.map(mode => {
          return getMode(mode.modeId).then(detail => {
            mode.modeParams = detail.data.modeParams || [];
            return mode;
          }).catch(() => {
            mode.modeParams = [];
            return mode;
          });
        });

        return Promise.all(modePromises).then(modes => {
          this.modeOptions = modes;
          console.log('加载的模式列表（含参数）:', this.modeOptions);
        });
      }).catch(error => {
        console.error('获取模式列表失败', error);
        this.$message.error('获取模式列表失败');
      });
    },
    /** 获取参数选项 */
    getParamOptions(param) {
      if (!param.paramOptions) return [];
      if (Array.isArray(param.paramOptions)) return param.paramOptions;
      if (typeof param.paramOptions === 'string') {
        try {
          const parsed = JSON.parse(param.paramOptions);
          return Array.isArray(parsed) ? parsed : [];
        } catch (e) {
          return [];
        }
      }
      return [];
    },
    /** 获取状态类型 */
    getStatusType(status) {
      const map = {
        0: 'info',      // 离线
        1: 'success',   // 在线
        2: 'warning'    // 忙碌/待机
      };
      return map[status] || 'info';
    },
    /** 获取状态文本 */
    getStatusText(status) {
      const map = {
        0: '离线',
        1: '在线',
        2: '待机中'
      };
      return map[status] || '未知';
    },
    /** 选择模式 */
    selectMode(mode) {
      this.selectedModeId = mode.modeId;
      // 加载该模式的配置数据
      this.loadModeConfig(mode);
    },
    /** 加载模式配置 */
    loadModeConfig(mode) {
      const newConfigData = {};
      if (mode.modeParams && mode.modeParams.length > 0) {
        mode.modeParams.forEach(param => {
          const key = `param_${param.paramId}`;
          // 根据参数类型处理默认值
          let defaultValue = param.paramValue;
          if (param.paramType === 'boolean') {
            defaultValue = defaultValue === 'true' || defaultValue === true;
          } else if (param.paramType === 'number' || param.paramType === 'range') {
            defaultValue = Number(defaultValue) || 0;
          }
          newConfigData[key] = defaultValue;
        });
      }
      this.configData = newConfigData;
      // 保存一份原始配置用于重置
      this.savedConfigData = JSON.parse(JSON.stringify(newConfigData));
    },
    /** 获取切换警告信息 */
    getSwitchWarning() {
      if (!this.selectedMode) return '请先选择目标机器人和模式';

      const warnings = {
        1: '切换到待机模式将降低功耗，等待手动或定时唤醒',
        2: '切换到维护模式将暂停所有任务，机器人将进入调试状态',
        3: '切换到充电模式机器人将返回充电站'
      };
      return warnings[this.selectedMode.modeId] || '切换模式将立即生效，当前任务可能会被中断';
    },
    /** 重置配置 */
    resetConfig() {
      this.$confirm('确定要重置当前配置为默认值吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.configData = JSON.parse(JSON.stringify(this.savedConfigData));
        this.$message.success('重置成功');
      }).catch(() => {});
    },
    /** 保存配置 */
    saveConfig() {
      this.savedConfigData = JSON.parse(JSON.stringify(this.configData));
      this.$message.success('配置保存成功');

      // 记录操作历史
      const modeName = this.selectedMode ? this.selectedMode.modeName : '未知';
      this.recordHistory('config-change', `保存${modeName}配置`);
    },
    /** 确认切换 */
    confirmSwitch() {
      console.log('当前选中的机器人ID:', this.selectedRobotId);
      console.log('机器人列表:', this.robotOptions);

      if (!this.selectedModeId || !this.selectedRobotId) {
        this.$message.warning('请选择目标机器人和模式');
        return;
      }

      // 获取目标机器人名称
      let robotName = '';
      if (this.selectedRobotId === 'all') {
        robotName = '所有机器人';
      } else {
        const robot = this.robotOptions.find(r => r.robotId === this.selectedRobotId);
        robotName = robot ? robot.name : '未知';
      }

      const modeName = this.selectedMode.modeName;

      this.$confirm(`确定要将 ${robotName} 切换为 ${modeName} 吗？`, '切换确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.doSwitch();
      }).catch(() => {});
    },
    /** 执行切换 */
    doSwitch() {
      if (this.loading) return;
      this.loading = true;

      const robotIds = this.selectedRobotId === 'all'
        ? this.robotOptions.map(r => r.robotId)
        : [this.selectedRobotId];

      const robotNames = this.selectedRobotId === 'all'
        ? '所有机器人'
        : this.robotOptions.find(r => r.robotId === this.selectedRobotId)?.name;

      // 显示加载状态
      const loadingInstance = this.$loading({
        lock: true,
        text: '正在切换模式...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      });

      // 调用更新机器人模式API
      const promises = robotIds.map(robotId =>
        updateRobotMode({ robotId: robotId, modeId: this.selectedModeId })
      );

      Promise.all(promises)
        .then(() => {
          this.$message.success(`已成功将 ${robotNames} 切换为 ${this.selectedMode.modeName}`);

          // 记录操作历史
          this.recordHistory('mode-switch', `切换为${this.selectedMode.modeName}`);

          // 刷新机器人列表以更新显示
          this.getRobotList().then(() => {
            if (this.selectedRobotId !== 'all') {
              const updatedRobot = this.robotOptions.find(r => r.robotId === this.selectedRobotId);
              if (updatedRobot) {
                console.log(`机器人 ${updatedRobot.name} 模式已更新为: ${updatedRobot.currentModeName}`);
              }
            }
          });
        })
        .catch(error => {
          console.error('切换失败', error);
          const errorMsg = error.response?.data?.msg || error.message || '网络错误';
          this.$message.error(`切换失败：${errorMsg}`);

          // 记录失败历史
          this.recordHistory('mode-switch', `切换为${this.selectedMode.modeName}失败: ${errorMsg}`, 'fail');
        })
        .finally(() => {
          this.loading = false;
          loadingInstance.close();
        });
    },
    /** 记录操作历史 */
    recordHistory(type, content, status = 'success') {
      const historyData = {
        operationTime: new Date().toISOString(),
        operationType: type,
        robotId: this.selectedRobotId !== 'all' ? this.selectedRobotId : null,
        robotName: this.selectedRobotId === 'all' ? '所有机器人' :
          this.robotOptions.find(r => r.robotId === this.selectedRobotId)?.name,
        content: content,
        operator: this.$store.state.user?.name || this.$store.state.user?.nickName || '系统',
        status: status
      };

      addHistory(historyData).then(response => {
        if (response.code === 200) {
          console.log('历史记录已保存');
        } else {
          console.error('保存历史记录失败', response.msg);
        }
      }).catch(error => {
        console.error('保存历史记录失败', error);
      });
    }
  }
};
</script>

<style scoped>
/* ========== 卡片样式 ========== */
.card {
  background: white;
  border-radius: 10px;
  box-shadow: 0px 2px 2px rgba(0, 0, 0, 0.5);
  margin-bottom: 24px;
  overflow: hidden;
  position: relative;
  z-index: 1;
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid #E5E7EB;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #000;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title i {
  color: #3976E4;
}

.card-body {
  padding: 24px 20px;
}

/* 筛选栏样式 */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
  background-color: #f8f9fa;
  padding: 12px 16px;
  border-radius: 8px;
  border: 1px solid #E5E7EB;
}

.filter-bar span {
  font-weight: 500;
  color: #4D4D4D;
  white-space: nowrap;
}

.filter-bar span i {
  color: #3976E4;
  margin-right: 6px;
}

.filter-bar .el-select {
  min-width: 300px;
}

/* 子卡片样式 */
.sub-card {
  background: #ffffff;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #E5E7EB;
  margin-bottom: 20px;
}

.sub-card-header {
  font-weight: 600;
  color: #1e2a3a;
  margin-bottom: 20px;
  font-size: 15px;
}

.sub-card-header i {
  color: #3976E4;
  margin-right: 8px;
}

/* 模式按钮组 */
.mode-buttons {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.mode-btn {
  width: 100%;
}

.mode-btn-inner {
  padding: 20px;
  border: 3px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
  background-color: white;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
}

.mode-btn-inner:hover {
  transform: translateY(-5px);
  border-color: #3976E4;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.mode-btn-inner.active {
  color: white;
  background-color: #3976E4;
  border-color: #3976E4;
}

.mode-btn-inner i {
  font-size: 32px;
  margin-bottom: 10px;
}

.mode-btn-inner .mode-name {
  display: block;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 5px;
}

.mode-btn-inner .mode-desc {
  display: block;
  font-size: 12px;
  color: #909399;
}

.mode-btn-inner.active .mode-desc {
  color: rgba(255, 255, 255, 0.8);
}

/* 参数配置项样式 */
.param-config-item {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #f0f0f0;
}

.param-config-item:last-child {
  border-bottom: none;
}

.param-desc {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  line-height: 1.4;
}

.no-params-tip {
  padding: 20px;
  text-align: center;
  color: #909399;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.no-params-tip i {
  margin-right: 5px;
}

/* 配置操作按钮 */
.config-actions {
  margin-top: 20px;
  text-align: right;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.config-actions .el-button {
  padding: 8px 20px;
  border-radius: 6px;
}

/* 切换信息 */
.switch-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background-color: #ecf5ff;
  border-left: 4px solid #3976E4;
  border-radius: 4px;
  margin-bottom: 20px;
}

.switch-info i {
  color: #3976E4;
  font-size: 18px;
}

.switch-info span {
  color: #1e2a3a;
  font-size: 14px;
}

.switch-actions {
  text-align: right;
}

.switch-actions .el-button {
  padding: 12px 30px;
  border-radius: 30px;
  font-size: 15px;
}

/* 表单样式 */
.el-form-item {
  margin-bottom: 20px;
}

.el-form-item__label {
  font-weight: 500;
  color: #4D4D4D;
}

.el-slider {
  width: 80%;
  margin-right: 15px;
}

/* 响应式调整 */
@media (max-width: 992px) {
  .mode-buttons {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-bar span {
    margin-bottom: 8px;
  }

  .filter-bar .el-select {
    width: 100% !important;
    min-width: auto;
  }

  .mode-buttons {
    grid-template-columns: 1fr;
  }

  .config-actions {
    flex-direction: column;
  }

  .config-actions .el-button {
    width: 100%;
  }

  .switch-actions .el-button {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .switch-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
