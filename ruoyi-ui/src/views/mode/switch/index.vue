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
        <el-select v-model="selectedRobotId" placeholder="请选择机器人" filterable clearable style="flex: 1;" @change="onRobotChange">
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

      <!-- 模式切换按钮组 - 过滤掉 mode_id = 0 的"无"模式 -->
      <div class="mode-buttons">
        <div class="mode-btn" v-for="mode in switchableModes" :key="mode.modeId">
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
          <el-tag v-if="selectedRobotId === 'all'" type="warning" size="mini" style="margin-left: 10px;">
            <i class="el-icon-info"></i> 批量配置模式（将应用到所有机器人）
          </el-tag>
          <el-tag v-else type="success" size="mini" style="margin-left: 10px;">
            <i class="el-icon-info"></i> 单独配置模式（仅对当前机器人生效）
          </el-tag>
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

    <!-- 充电模式结果弹窗 -->
    <el-dialog :title="chargeResult.title" :visible.sync="chargeResultDialogVisible" width="30%">
      <div class="result-content">
        <i :class="chargeResult.icon" :style="{ color: chargeResult.color, fontSize: '48px' }"></i>
        <p>{{ chargeResult.message }}</p>
        <p v-if="chargeResult.detail" class="result-detail">{{ chargeResult.detail }}</p>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="chargeResultDialogVisible = false">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { listRobot, updateRobotMode, saveRobotModeConfig, batchSaveRobotModeConfig, getRobotModeConfig } from "@/api/mode/robot";
import { listMode, getMode } from "@/api/mode/mode";
import { addHistory } from "@/api/mode/history";
import request from '@/utils/request';

export default {
  name: "ModeSwitch",
  data() {
    return {
      selectedRobotId: null,
      selectedModeId: null,
      robotOptions: [],
      allModes: [],        // 所有模式（包含"无"模式，用于显示机器人当前模式）
      switchableModes: [], // 可切换的模式（不包含"无"模式，用于模式卡片）
      configData: {},
      savedConfigData: {},
      loading: false,
      // 充电模式结果弹窗
      chargeResultDialogVisible: false,
      chargeResult: {
        title: '',
        icon: '',
        color: '',
        message: '',
        detail: ''
      }
    };
  },
  computed: {
    selectedMode() {
      return this.switchableModes.find(m => m.modeId === this.selectedModeId);
    },
    activeModeName() {
      if (this.selectedModeId) {
        const mode = this.switchableModes.find(m => m.modeId === this.selectedModeId);
        return mode ? mode.modeName : '待机模式';
      }
      return '待机模式';
    }
  },
  created() {
    this.initData();
  },
  methods: {
    async initData() {
      await this.getModeList();
      await this.getRobotList();
    },

    getRobotList() {
      return listRobot({ pageNum: 1, pageSize: 100 }).then(response => {
        this.robotOptions = response.rows.map(robot => ({
          ...robot,
          robotId: robot.robotId || robot.id,
          robotName: robot.robotName || robot.name,
          name: robot.robotName || robot.name
        }));
        this.updateRobotModeNames();
      }).catch(error => {
        console.error('获取机器人列表失败', error);
        this.$message.error('获取机器人列表失败');
      });
    },

    updateRobotModeNames() {
      this.robotOptions.forEach(robot => {
        // 使用 allModes（包含"无"模式）来显示机器人当前模式
        const mode = this.allModes.find(m => m.modeId === robot.currentMode);
        robot.currentModeName = mode ? mode.modeName : '未知';
      });
    },

    getModeList() {
      return listMode({ pageNum: 1, pageSize: 100, enabled: '1' }).then(response => {
        // 保存所有模式（包含 mode_id = 0 的"无"模式，用于显示）
        const allRows = response.rows || [];

        // 过滤掉 mode_id = 0 的"无"模式，作为可切换的模式
        const filteredRows = allRows.filter(mode => mode.modeId !== 0);

        const modePromises = allRows.map(mode => {
          return getMode(mode.modeId).then(detail => {
            mode.modeParams = detail.data.modeParams || [];
            return mode;
          }).catch(() => {
            mode.modeParams = [];
            return mode;
          });
        });

        return Promise.all(modePromises).then(modes => {
          // 所有模式（包含"无"模式）
          this.allModes = modes;
          // 可切换的模式（不包含"无"模式）
          this.switchableModes = modes.filter(mode => mode.modeId !== 0);
        });
      }).catch(error => {
        console.error('获取模式列表失败', error);
        this.$message.error('获取模式列表失败');
      });
    },

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

    getStatusType(status) {
      const map = { 0: 'info', 1: 'success', 2: 'warning' };
      return map[status] || 'info';
    },

    getStatusText(status) {
      const map = { 0: '离线', 1: '在线', 2: '待机中' };
      return map[status] || '未知';
    },

    /** 机器人选择变化时重新加载配置 */
    onRobotChange() {
      if (this.selectedModeId) {
        const mode = this.switchableModes.find(m => m.modeId === this.selectedModeId);
        if (mode) {
          this.loadModeConfig(mode);
        }
      }
    },

    selectMode(mode) {
      this.selectedModeId = mode.modeId;
      this.loadModeConfig(mode);
    },

    /** 加载模式配置 */
    loadModeConfig(mode) {
      // 如果没有选择机器人，只加载模式默认配置
      if (!this.selectedRobotId) {
        this.loadDefaultModeConfig(mode);
        return;
      }

      // 如果是"所有机器人"，加载第一个机器人的配置作为参考
      if (this.selectedRobotId === 'all') {
        this.loadBatchModeConfig(mode);
        return;
      }

      // 获取该机器人的配置
      getRobotModeConfig(this.selectedRobotId, mode.modeId).then(response => {
        if (response.code === 200 && response.data) {
          this.loadConfigFromServer(mode, response.data);
        } else {
          this.loadDefaultModeConfig(mode);
        }
      }).catch(() => {
        this.loadDefaultModeConfig(mode);
      });
    },

    /** 批量模式配置 - 显示默认配置，保存时应用到所有机器人 */
    loadBatchModeConfig(mode) {
      // 尝试获取第一个机器人的配置作为参考
      if (this.robotOptions.length > 0) {
        const firstRobotId = this.robotOptions[0].robotId;
        getRobotModeConfig(firstRobotId, mode.modeId).then(response => {
          if (response.code === 200 && response.data) {
            this.loadConfigFromServer(mode, response.data);
          } else {
            this.loadDefaultModeConfig(mode);
          }
        }).catch(() => {
          this.loadDefaultModeConfig(mode);
        });
      } else {
        this.loadDefaultModeConfig(mode);
      }
    },

    /** 从服务器加载配置 */
    loadConfigFromServer(mode, serverConfig) {
      const newConfigData = {};
      if (mode.modeParams && mode.modeParams.length > 0) {
        mode.modeParams.forEach(param => {
          const key = `param_${param.paramId}`;
          // 优先使用服务器保存的值，否则使用默认值
          let defaultValue = serverConfig[param.paramName] !== undefined
            ? serverConfig[param.paramName]
            : param.paramValue;

          if (param.paramType === 'boolean') {
            defaultValue = defaultValue === 'true' || defaultValue === true;
          } else if (param.paramType === 'number' || param.paramType === 'range') {
            defaultValue = Number(defaultValue) || 0;
          }
          newConfigData[key] = defaultValue;
        });
      }
      this.configData = newConfigData;
      this.savedConfigData = JSON.parse(JSON.stringify(newConfigData));
    },

    /** 加载模式默认配置 */
    loadDefaultModeConfig(mode) {
      const newConfigData = {};
      if (mode.modeParams && mode.modeParams.length > 0) {
        mode.modeParams.forEach(param => {
          const key = `param_${param.paramId}`;
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
      this.savedConfigData = JSON.parse(JSON.stringify(newConfigData));
    },

    getSwitchWarning() {
      if (!this.selectedMode) return '请先选择目标机器人和模式';

      const warnings = {
        1: '切换到待机模式将降低功耗，等待手动或定时唤醒',
        2: '切换到维护模式将暂停所有任务，机器人将进入调试状态',
        3: '切换到充电模式机器人将返回充电站'
      };
      return warnings[this.selectedMode.modeId] || '切换模式将立即生效，当前任务可能会被中断';
    },

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

    /** 保存配置 - 支持单个机器人和所有机器人 */
    saveConfig() {
      if (!this.selectedRobotId) {
        this.$message.warning('请先选择机器人');
        return;
      }

      // 构建配置对象
      const config = {};
      if (this.selectedMode && this.selectedMode.modeParams) {
        this.selectedMode.modeParams.forEach(param => {
          const key = `param_${param.paramId}`;
          let value = this.configData[key];

          if (param.paramType === 'boolean') {
            value = value === true ? 'true' : 'false';
          } else if (param.paramType === 'number' || param.paramType === 'range') {
            value = String(value || 0);
          } else {
            value = value || '';
          }
          config[param.paramName] = value;
        });
      }

      const loading = this.$loading({
        lock: true,
        text: '正在保存配置...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      });

      // 判断是单个机器人还是所有机器人
      if (this.selectedRobotId === 'all') {
        // 所有机器人：批量保存配置
        const robotIds = this.robotOptions.map(r => r.robotId);

        batchSaveRobotModeConfig({
          robotIds: robotIds,
          modeId: this.selectedModeId,
          config: config
        }).then(response => {
          loading.close();
          if (response.code === 200) {
            this.savedConfigData = JSON.parse(JSON.stringify(this.configData));
            this.$message.success(`已成功将配置应用到所有 ${this.robotOptions.length} 个机器人`);
            this.recordHistory('config-change', `批量保存${this.selectedMode.modeName}配置到所有机器人`);
          } else {
            this.$message.error(response.msg || '批量保存失败');
          }
        }).catch(error => {
          loading.close();
          console.error('批量保存配置失败', error);
          this.$message.error('批量保存配置失败');
        });
      } else {
        // 单个机器人
        saveRobotModeConfig({
          robotId: this.selectedRobotId,
          modeId: this.selectedModeId,
          config: config
        }).then(response => {
          loading.close();
          if (response.code === 200) {
            this.savedConfigData = JSON.parse(JSON.stringify(this.configData));
            this.$message.success('配置保存成功');
            this.recordHistory('config-change', `保存${this.selectedMode.modeName}配置`);
          } else {
            this.$message.error(response.msg || '保存失败');
          }
        }).catch(error => {
          loading.close();
          console.error('保存配置失败', error);
          this.$message.error('保存配置失败');
        });
      }
    },

    confirmSwitch() {
      if (!this.selectedModeId || !this.selectedRobotId) {
        this.$message.warning('请选择目标机器人和模式');
        return;
      }

      let robotName = '';
      if (this.selectedRobotId === 'all') {
        robotName = `所有机器人 (共${this.robotOptions.length}个)`;
      } else {
        const robot = this.robotOptions.find(r => r.robotId === this.selectedRobotId);
        robotName = robot ? robot.name : '未知';
      }

      const modeName = this.selectedMode.modeName;

      let additionalTip = '';
      if (this.selectedModeId === 3) {
        additionalTip = '\n\n充电策略将根据每个机器人的配置决定（立即充电或完成任务后充电）';
      }

      this.$confirm(`确定要将 ${robotName} 切换为 ${modeName} 吗？${additionalTip}`, '切换确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.doSwitch();
      }).catch(() => {});
    },

    async doSwitch() {
      if (this.loading) return;
      this.loading = true;

      const robotIds = this.selectedRobotId === 'all'
        ? this.robotOptions.map(r => r.robotId)
        : [this.selectedRobotId];

      const robotNames = this.selectedRobotId === 'all'
        ? `所有机器人 (共${this.robotOptions.length}个)`
        : this.robotOptions.find(r => r.robotId === this.selectedRobotId)?.name;

      // 如果是充电模式，使用专门的 chargeMode API
      if (this.selectedModeId === 3) {
        await this.executeChargeMode(robotIds, robotNames);
        this.loading = false;
        return;
      }

      // 其他模式使用原来的方式
      const loadingInstance = this.$loading({
        lock: true,
        text: '正在切换模式...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      });

      try {
        // 逐个调用更新模式接口
        for (const robotId of robotIds) {
          await updateRobotMode({ robotId: robotId, modeId: this.selectedModeId });
        }

        const successMsg = `已成功将 ${robotNames} 切换为 ${this.selectedMode.modeName}`;
        this.$message.success(successMsg);
        this.recordHistory('mode-switch', `切换为${this.selectedMode.modeName}`);
        await this.getRobotList();
      } catch (error) {
        console.error('切换失败', error);
        const errorMsg = error.response?.data?.msg || error.message || '网络错误';
        this.$message.error(`切换失败：${errorMsg}`);
        this.recordHistory('mode-switch', `切换为${this.selectedMode.modeName}失败: ${errorMsg}`, 'fail');
      } finally {
        this.loading = false;
        loadingInstance.close();
      }
    },

    /** 执行充电模式切换（带详细统计） */
    async executeChargeMode(robotIds, robotNames) {
      const loadingInstance = this.$loading({
        lock: true,
        text: '正在切换充电模式...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      });

      try {
        // 直接使用 request 调用充电模式接口
        const response = await request({
          url: '/mode/robots/chargeMode',
          method: 'put',
          data: robotIds
        });

        loadingInstance.close();
        console.log('充电模式响应:', response);

        if (response.code === 200) {
          const successCount = response.data?.successCount || 0;
          const immediateCount = response.data?.immediateCount || 0;
          const waitingCount = response.data?.waitingCount || 0;

          let message = '';
          let detail = '';

          if (successCount === 0) {
            message = `充电模式切换失败`;
            detail = '请检查机器人是否在线';
          } else if (waitingCount > 0) {
            if (immediateCount > 0) {
              message = `已命令 ${successCount} 个机器人切换为充电模式：${immediateCount} 个立即充电，${waitingCount} 个等待任务完成后自动充电`;
              detail = '有任务的机器人会在任务完成后自动开始充电';
            } else {
              message = `已命令 ${successCount} 个机器人切换为充电模式，其中 ${waitingCount} 个机器人将等待任务完成后自动充电`;
              detail = '有任务的机器人会在任务完成后自动开始充电';
            }
          } else if (immediateCount > 0) {
            message = `已将 ${successCount} 个机器人切换为充电模式`;
            detail = '机器人正在前往充电站充电';
          } else {
            message = `已对 ${successCount} 个机器人执行充电模式操作`;
            detail = '操作执行成功';
          }

          // 显示结果弹窗
          this.chargeResult = {
            title: '操作完成',
            icon: 'el-icon-success',
            color: '#67c23a',
            message: message,
            detail: detail
          };
          this.chargeResultDialogVisible = true;

          // 记录历史
          this.recordHistory('mode-switch', message);
          this.$message.success(message);

          // 刷新机器人列表
          setTimeout(() => {
            this.getRobotList();
          }, 1000);
        } else {
          this.$message.error(response.msg || '充电模式切换失败');
          this.recordHistory('mode-switch', `充电模式切换失败: ${response.msg || '未知错误'}`, 'fail');
        }
      } catch (error) {
        loadingInstance.close();
        console.error('充电模式切换失败:', error);
        const errorMsg = error.response?.data?.msg || error.message || '网络错误';
        this.$message.error(`充电模式切换失败：${errorMsg}`);
        this.recordHistory('mode-switch', `充电模式切换失败: ${errorMsg}`, 'fail');
      }
    },

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

      addHistory(historyData).catch(error => {
        console.error('保存历史记录失败', error);
      });
    }
  }
};
</script>

<style scoped>
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
  display: flex;
  align-items: center;
}

.sub-card-header i {
  color: #3976E4;
  margin-right: 8px;
}

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

.result-content {
  text-align: center;
  padding: 20px;
}

.result-content p {
  margin-top: 15px;
  font-size: 16px;
}

.result-detail {
  font-size: 14px;
  color: #909399;
}

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
