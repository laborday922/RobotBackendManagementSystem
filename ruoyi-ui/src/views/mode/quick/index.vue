<template>
  <div class="card">
    <div class="card-header">
      <div class="card-title">
        <i class="fas fa-bolt"></i> 快捷操作
      </div>
      <div class="header-actions">
        <el-button size="small" type="warning" @click="showDebugPanel = true">
          <i class="el-icon-setting"></i> 调试工具
        </el-button>
        <el-button size="small" type="primary" @click="refreshRobotList" :loading="refreshing">
          <i class="el-icon-refresh"></i> 刷新
        </el-button>
      </div>
    </div>

    <div class="card-body">
      <!-- 机器人选择区域 -->
      <div class="filter-bar">
        <span><i class="fas fa-robot"></i> 选择操作对象</span>
      </div>

      <!-- 选择方式选项 -->
      <el-radio-group v-model="selectionMode" @change="handleSelectionModeChange" style="margin-bottom: 20px;">
        <el-radio-button label="all">全部机器人</el-radio-button>
        <el-radio-button label="single">单个机器人</el-radio-button>
        <el-radio-button label="custom">自定义选择</el-radio-button>
      </el-radio-group>

      <!-- 单个机器人选择 -->
      <div v-if="selectionMode === 'single'" class="selection-section">
        <el-select v-model="selectedRobotId" placeholder="请选择机器人" filterable clearable style="width: 100%;">
          <el-option
            v-for="robot in robotOptions"
            :key="robot.robotId"
            :label="robot.robotName"
            :value="robot.robotId">
            <span style="float: left">{{ robot.robotName }}</span>
            <span style="float: right; color: #8492a6; font-size: 13px">
              <el-tag :type="getStatusType(robot.status)" size="mini">
                {{ getStatusText(robot.status) }}
              </el-tag>
              <span style="margin-left: 5px;">{{ robot.battery }}%</span>
            </span>
          </el-option>
        </el-select>
      </div>

      <!-- 自定义选择机器人 -->
      <div v-if="selectionMode === 'custom'" class="selection-section">
        <div class="selection-header">
          <span>已选择 {{ selectedRobots.length }} 个机器人</span>
          <div>
            <el-button size="mini" @click="selectAllRobots">全选</el-button>
            <el-button size="mini" @click="clearSelection">清空</el-button>
            <el-button size="mini" type="primary" @click="selectOnlineRobots">在线机器人</el-button>
          </div>
        </div>

        <el-checkbox-group v-model="selectedRobots" class="robot-grid">
          <el-checkbox
            v-for="robot in robotOptions"
            :key="robot.robotId"
            :label="robot.robotId"
            class="robot-item">
            <div class="robot-info">
              <span class="robot-name">{{ robot.robotName }}</span>
              <el-tag :type="getStatusType(robot.status)" size="mini" class="status-tag">
                {{ getStatusText(robot.status) }}
              </el-tag>
              <span class="battery" :class="getBatteryClass(robot.battery)">
                {{ robot.battery }}%
              </span>
            </div>
          </el-checkbox>
        </el-checkbox-group>
      </div>

      <!-- 已选择机器人信息 -->
      <div v-if="getSelectedRobotInfo()" class="selected-info">
        <i class="el-icon-info"></i>
        <span>操作对象：{{ getSelectedRobotInfo() }}</span>
      </div>

      <!-- 快捷操作按钮组 -->
      <el-row :gutter="20" class="operation-section">
        <!-- 紧急操作组 -->
        <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
          <el-card class="operation-card emergency">
            <div slot="header">
              <span><i class="fas fa-exclamation-triangle"></i> 紧急操作</span>
            </div>
            <div class="button-group">
              <el-button
                type="danger"
                class="operation-btn emergency-stop"
                @click="executeOperation('emergency_stop')"
                :disabled="!hasSelectedRobot() || loading">
                <i class="fas fa-stop-circle"></i> 紧急停止
              </el-button>
              <el-button
                type="warning"
                class="operation-btn emergency-evacuation"
                @click="executeOperation('emergency_evacuation')"
                :disabled="!hasSelectedRobot() || loading">
                <i class="fas fa-arrow-left"></i> 紧急撤离
              </el-button>
              <el-button
                type="info"
                class="operation-btn batch-restart"
                @click="executeOperation('batch_restart')"
                :disabled="!hasSelectedRobot() || loading">
                <i class="fas fa-sync-alt"></i> 批量重启
              </el-button>
            </div>
          </el-card>
        </el-col>

        <!-- 状态操作组 -->
        <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
          <el-card class="operation-card status">
            <div slot="header">
              <span><i class="fas fa-chart-line"></i> 状态操作</span>
            </div>
            <div class="button-group">
              <el-button
                type="success"
                class="operation-btn"
                @click="executeOperation('refresh_status')"
                :disabled="!hasSelectedRobot() || loading">
                <i class="fas fa-sync-alt"></i> 刷新状态
              </el-button>
              <el-button
                type="warning"
                class="operation-btn"
                @click="executeOperation('test_alert')"
                :disabled="!hasSelectedRobot() || loading">
                <i class="fas fa-bell"></i> 测试告警
              </el-button>
              <el-button
                type="info"
                class="operation-btn"
                @click="executeOperation('clear_alerts')"
                :disabled="!hasSelectedRobot() || loading">
                <i class="fas fa-bell-slash"></i> 清除告警
              </el-button>
            </div>
          </el-card>
        </el-col>

        <!-- 系统操作组 -->
        <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
          <el-card class="operation-card system">
            <div slot="header">
              <span><i class="fas fa-cogs"></i> 系统操作</span>
            </div>
            <div class="button-group">
              <el-button
                type="primary"
                class="operation-btn"
                @click="executeOperation('standby_mode')"
                :disabled="!hasSelectedRobot() || loading">
                <i class="fas fa-pause-circle"></i> 待机模式
              </el-button>
              <el-button
                type="primary"
                class="operation-btn"
                @click="executeOperation('maintenance_mode')"
                :disabled="!hasSelectedRobot() || loading">
                <i class="fas fa-tools"></i> 维护模式
              </el-button>
              <el-button
                type="primary"
                class="operation-btn"
                @click="executeOperation('charge_mode')"
                :disabled="!hasSelectedRobot() || loading">
                <i class="fas fa-bolt"></i> 充电模式
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 操作结果提示 -->
      <el-dialog :title="operationResult.title" :visible.sync="resultDialogVisible" width="30%">
        <div class="result-content">
          <i :class="operationResult.icon" :style="{ color: operationResult.color, fontSize: '48px' }"></i>
          <p>{{ operationResult.message }}</p>
          <p v-if="operationResult.detail" class="result-detail">{{ operationResult.detail }}</p>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button type="primary" @click="resultDialogVisible = false">确 定</el-button>
        </span>
      </el-dialog>
    </div>

    <!-- 调试面板 -->
    <div v-if="showDebugPanel" class="custom-debug-panel">
      <div class="debug-panel-header">
        <h3><i class="el-icon-setting"></i> 调试工具</h3>
        <el-button type="text" @click="showDebugPanel = false" icon="el-icon-close"></el-button>
      </div>
      <div class="debug-panel-body">
        <div class="debug-section">
          <h4><i class="el-icon-robot"></i> 选择机器人</h4>
          <el-select
            v-model="debugRobotId"
            placeholder="请选择机器人"
            filterable
            clearable
            style="width: 100%;">
            <el-option
              v-for="robot in robotOptions"
              :key="robot.robotId"
              :label="robot.robotName"
              :value="robot.robotId">
              <span style="float: left">{{ robot.robotName }}</span>
              <span style="float: right; color: #8492a6">
                {{ getStatusText(robot.status) }} | {{ robot.battery }}%
              </span>
            </el-option>
          </el-select>
        </div>

        <div class="debug-section">
          <h4><i class="el-icon-setting"></i> 调试操作</h4>
          <div class="debug-buttons">
            <el-button type="primary" size="small" @click="debugGetRobotStatus" :loading="debugLoading">
              <i class="el-icon-search"></i> 查询状态
            </el-button>
            <el-button type="success" size="small" @click="debugForceUpdateOnline" :loading="debugLoading">
              <i class="el-icon-edit"></i> 强制设为在线
            </el-button>
            <el-button type="info" size="small" @click="debugRefreshList" :loading="debugLoading">
              <i class="el-icon-refresh"></i> 刷新列表
            </el-button>
            <el-button type="warning" size="small" @click="debugClearCache" :loading="debugLoading">
              <i class="el-icon-delete"></i> 清除缓存
            </el-button>
            <el-button type="danger" size="small" @click="debugTestRestart" :loading="debugLoading">
              <i class="el-icon-video-play"></i> 测试重启
            </el-button>
          </div>
        </div>

        <div class="debug-section" v-if="debugResult">
          <h4><i class="el-icon-document"></i> 查询结果</h4>
          <div class="debug-result">
            <pre>{{ JSON.stringify(debugResult, null, 2) }}</pre>
          </div>
        </div>

        <div class="debug-section">
          <h4><i class="el-icon-tickets"></i> 操作日志</h4>
          <div class="debug-logs">
            <div class="log-list" ref="logList">
              <div v-for="(log, index) in debugLogs" :key="index" class="log-item" :class="'log-' + log.type">
                <span class="log-time">{{ log.time }}</span>
                <span class="log-message">{{ log.message }}</span>
                <pre v-if="log.data" class="log-data">{{ JSON.stringify(log.data, null, 2) }}</pre>
              </div>
              <div v-if="debugLogs.length === 0" class="log-empty">暂无日志，请执行操作</div>
            </div>
          </div>
          <div class="debug-actions">
            <el-button size="small" @click="clearDebugLogs">清空日志</el-button>
            <el-button size="small" type="primary" @click="exportDebugLogs">导出日志</el-button>
          </div>
        </div>

        <div class="debug-section">
          <h4><i class="el-icon-console"></i> 控制台命令</h4>
          <div class="console-commands">
            <div class="command-item" @click="copyToClipboard('console.table(this.$children.find(c => c.$options.name === \'QuickOperation\').robotOptions)')">
              <code>查看机器人列表</code>
              <i class="el-icon-document-copy"></i>
            </div>
            <div class="command-item" @click="copyToClipboard('fetch(\'/system/robot/debug/all\').then(r => r.json()).then(console.log)')">
              <code>查看数据库状态</code>
              <i class="el-icon-document-copy"></i>
            </div>
            <div class="command-item" @click="copyToClipboard('fetch(\'/system/robot/debug/1\').then(r => r.json()).then(console.log)')">
              <code>查看单个机器人</code>
              <i class="el-icon-document-copy"></i>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {
  listRobot,
  batchRestart,
  emergencyStop,
  emergencyEvacuation,
  refreshStatus,
  testAlert,
  clearAlerts,
  standbyMode,
  maintenanceMode,
  chargeMode
} from "@/api/mode/robot";
import { listMode } from "@/api/mode/mode";
import { addHistory } from "@/api/mode/history";
import request from '@/utils/request';

export default {
  name: "QuickOperation",
  data() {
    return {
      selectionMode: 'all',
      selectedRobotId: null,
      selectedRobots: [],
      robotOptions: [],
      modeOptions: [],
      resultDialogVisible: false,
      loading: false,
      refreshing: false,
      operationResult: {
        title: '',
        icon: '',
        color: '',
        message: '',
        detail: ''
      },
      testAlertActive: false,
      pollingTimer: null,
      showDebugPanel: false,
      debugRobotId: null,
      debugLoading: false,
      debugResult: null,
      debugLogs: []
    };
  },
  created() {
    this.getRobotList();
    this.getModeList();
    this.addDebugLog('页面初始化完成', null, 'info');
  },
  beforeDestroy() {
    if (this.pollingTimer) {
      clearTimeout(this.pollingTimer);
    }
  },
  methods: {
    getRobotList() {
      this.addDebugLog('开始获取机器人列表', null, 'info');
      return request({
        url: '/mode/robots/list',
        method: 'get'
      }).then(response => {
        if (response.code === 200) {
          const rows = response.rows || [];
          this.robotOptions = rows.map(robot => ({
            robotId: robot.id,
            robotName: robot.name,
            robotCode: robot.code,
            status: robot.status,
            statusText: this.getStatusText(robot.status),
            statusType: this.getStatusType(robot.status),
            battery: robot.battery,
            area: robot.area
          }));
          this.addDebugLog(`获取机器人列表成功，共${this.robotOptions.length}个机器人`,
            this.robotOptions.map(r => ({
              id: r.robotId,
              name: r.robotName,
              status: r.status,
              statusText: r.statusText,
              battery: r.battery
            })),
            'success');
          if (this.robotOptions.length === 0) {
            this.$message.warning('暂无机器人数据');
          }
        } else {
          this.addDebugLog(`获取机器人列表失败: ${response.msg || '未知错误'}`, null, 'error');
          this.$message.error('获取机器人列表失败：' + (response.msg || '未知错误'));
        }
      }).catch(error => {
        this.addDebugLog(`获取机器人列表异常: ${error.message}`, error, 'error');
        console.error('获取机器人列表失败', error);
        this.$message.error('获取机器人列表失败：' + (error.message || '网络错误'));
      });
    },

    getStatusType(status) {
      const statusNum = parseInt(status);
      const map = { 0: 'info', 1: 'success', 2: 'warning' };
      return map[statusNum] || 'info';
    },

    getStatusText(status) {
      const statusNum = parseInt(status);
      const map = { 0: '离线', 1: '在线', 2: '待机中' };
      return map[statusNum] || '未知';
    },

    getBatteryClass(battery) {
      if (battery >= 80) return 'battery-high';
      if (battery >= 30) return 'battery-medium';
      if (battery >= 15) return 'battery-low';
      return 'battery-danger';
    },

    getModeList() {
      listMode({ pageNum: 1, pageSize: 100 }).then(response => {
        this.modeOptions = response.rows;
      }).catch(error => {
        console.error('获取模式列表失败', error);
      });
    },

    handleSelectionModeChange() {
      this.selectedRobotId = null;
      this.selectedRobots = [];
    },

    selectAllRobots() {
      this.selectedRobots = this.robotOptions.map(r => r.robotId);
      this.addDebugLog(`全选机器人: ${this.selectedRobots.length}个`, this.selectedRobots, 'info');
    },

    clearSelection() {
      this.selectedRobots = [];
      this.addDebugLog('清空选择', null, 'info');
    },

    selectOnlineRobots() {
      this.selectedRobots = this.robotOptions
        .filter(r => parseInt(r.status) === 1)
        .map(r => r.robotId);
      this.addDebugLog(`选择在线机器人: ${this.selectedRobots.length}个`, this.selectedRobots, 'info');
    },

    hasSelectedRobot() {
      if (this.selectionMode === 'all') return this.robotOptions.length > 0;
      if (this.selectionMode === 'single') return !!this.selectedRobotId;
      return this.selectedRobots.length > 0;
    },

    getSelectedRobotInfo() {
      if (this.selectionMode === 'all') {
        return `机器人 (共${this.robotOptions.length}个)`;
      } else if (this.selectionMode === 'single' && this.selectedRobotId) {
        const robot = this.robotOptions.find(r => r.robotId === this.selectedRobotId);
        return robot ? robot.robotName : '';
      } else if (this.selectionMode === 'custom' && this.selectedRobots.length > 0) {
        const names = this.robotOptions
          .filter(r => this.selectedRobots.includes(r.robotId))
          .map(r => r.robotName)
          .join('、');
        return `${names} (共${this.selectedRobots.length}个)`;
      }
      return '';
    },

    getTargetRobotIds() {
      if (this.selectionMode === 'all') {
        return this.robotOptions.map(r => r.robotId);
      } else if (this.selectionMode === 'single' && this.selectedRobotId) {
        return [this.selectedRobotId];
      } else if (this.selectionMode === 'custom') {
        return this.selectedRobots;
      }
      return [];
    },

    getTargetRobotNames() {
      const ids = this.getTargetRobotIds();
      return this.robotOptions
        .filter(r => ids.includes(r.robotId))
        .map(r => r.robotName)
        .join('、');
    },

    executeOperation(type) {
      const robotIds = this.getTargetRobotIds();
      const robotNames = this.getTargetRobotNames();
      const count = robotIds.length;

      const operationNames = {
        'emergency_stop': '紧急停止',
        'emergency_evacuation': '紧急撤离',
        'batch_restart': '批量重启',
        'refresh_status': '刷新状态',
        'test_alert': '测试告警',
        'clear_alerts': '清除告警',
        'standby_mode': '待机模式',
        'maintenance_mode': '维护模式',
        'charge_mode': '充电模式'
      };

      const operationName = operationNames[type];
      const needConfirmTypes = ['emergency_stop', 'emergency_evacuation', 'batch_restart', 'standby_mode', 'maintenance_mode', 'charge_mode'];

      if (needConfirmTypes.includes(type)) {
        this.$confirm(`确定要对 ${count} 个机器人执行 ${operationName} 操作吗？`, '操作确认', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.doExecuteOperation(type, robotIds, robotNames, count);
        }).catch(() => {});
      } else {
        this.doExecuteOperation(type, robotIds, robotNames, count);
      }
    },

    doExecuteOperation(type, robotIds, robotNames, count) {
      if (robotIds.length === 0) {
        this.$message.warning('请选择要操作的机器人');
        return;
      }

      this.loading = true;
      this.addDebugLog(`执行操作: ${type}, 机器人数量: ${count}`, { robotIds, robotNames }, 'info');

      const operationNames = {
        'emergency_stop': '紧急停止',
        'emergency_evacuation': '紧急撤离',
        'batch_restart': '批量重启',
        'refresh_status': '刷新状态',
        'test_alert': '测试告警',
        'clear_alerts': '清除告警',
        'standby_mode': '待机模式',
        'maintenance_mode': '维护模式',
        'charge_mode': '充电模式'
      };

      const operationName = operationNames[type];

      const loadingMessage = this.$message({
        message: `正在执行${operationName}操作...`,
        duration: 0,
        showClose: false,
        iconClass: 'el-icon-loading',
        customClass: 'operation-loading-message'
      });

      if (type === 'batch_restart') {
        this.addDebugLog('标记机器人为重启中状态', robotIds, 'warning');
        robotIds.forEach(robotId => {
          const robot = this.robotOptions.find(r => r.robotId === robotId);
          if (robot) {
            robot.status = 3;
          }
        });
      }

      let apiPromise = null;
      switch(type) {
        case 'emergency_stop':
          apiPromise = emergencyStop(robotIds);
          break;
        case 'emergency_evacuation':
          apiPromise = emergencyEvacuation(robotIds);
          break;
        case 'batch_restart':
          apiPromise = batchRestart(robotIds);
          break;
        case 'refresh_status':
          apiPromise = refreshStatus(robotIds);
          break;
        case 'test_alert':
          apiPromise = testAlert(robotIds);
          break;
        case 'clear_alerts':
          apiPromise = clearAlerts(robotIds);
          break;
        case 'standby_mode':
          apiPromise = standbyMode(robotIds);
          break;
        case 'maintenance_mode':
          apiPromise = maintenanceMode(robotIds);
          break;
        case 'charge_mode':
          apiPromise = chargeMode(robotIds);
          apiPromise.then(response => {
            loadingMessage.close();
            this.addDebugLog(`API响应: ${type}`, response, response.code === 200 ? 'success' : 'error');

            if (response.code === 200) {
              // 从 response.data 中获取统计数据
              const successCount = response.data?.successCount || 0;
              const immediateCount = response.data?.immediateCount || 0;
              const waitingCount = response.data?.waitingCount || 0;
              const total = response.data?.total || count;

              let message = '';
              let detail = '';

              // 注意判断顺序：先判断 waitingCount > 0 的情况，再判断立即充电
              if (successCount === 0) {
                message = `充电模式切换失败`;
                detail = '请检查机器人是否在线';
              } else if (waitingCount > 0) {
                // 有等待充电的机器人
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

              // 记录历史
              this.recordHistory(type, robotIds, robotNames, message);

              // 显示结果弹窗
              this.operationResult = {
                title: '操作完成',
                icon: 'el-icon-success',
                color: '#67c23a',
                message: message,
                detail: detail
              };
              this.resultDialogVisible = true;

              // 刷新机器人列表
              setTimeout(() => {
                this.getRobotList();
              }, 1000);

              this.$message.success(message);
            } else {
              this.$message.error(response.msg || '充电模式切换失败');
              this.recordHistory(type, robotIds, robotNames, `充电模式切换失败: ${response.msg || '未知错误'}`, 'fail');
            }
          }).catch(error => {
            loadingMessage.close();
            this.addDebugLog(`API调用失败: ${error.message}`, error, 'error');
            const errorMsg = error.msg || error.message || '网络错误';
            this.$message.error(`充电模式切换失败：${errorMsg}`);
            this.recordHistory(type, robotIds, robotNames, `充电模式切换失败: ${errorMsg}`, 'fail');
          }).finally(() => {
            this.loading = false;
          });
          return;  // 重要：避免继续执行后面的通用处理

        default:
          this.$message.error('未知操作类型');
          this.loading = false;
          loadingMessage.close();
          return;
      }

      apiPromise.then(response => {
        loadingMessage.close();
        this.addDebugLog(`API响应: ${type}`, response, response.code === 200 ? 'success' : 'error');

        if (response.code === 200) {
          let message = '';
          let detail = '';

          switch(type) {
            case 'emergency_stop':
              message = `已对 ${count} 个机器人执行紧急停止操作`;
              detail = '所有选中的机器人已紧急停止';
              break;
            case 'emergency_evacuation':
              message = `已命令 ${count} 个机器人紧急撤离`;
              detail = '机器人正在停止当前任务并返回安全位置';
              break;
            case 'batch_restart':
              const submitted = response.data?.submitted || count;
              message = `已提交 ${submitted} 个机器人的重启任务`;
              detail = '重启过程大约需要5-10秒，请稍后刷新查看状态';
              this.startPollingRestartStatus(robotIds);
              break;
            case 'refresh_status':
              message = `已刷新 ${count} 个机器人的状态`;
              detail = '状态信息已更新';
              break;
            case 'test_alert':
              if (this.testAlertActive) {
                this.$message.warning('测试告警已激活，请先清除告警');
                this.loading = false;
                return;
              }
              this.testAlertActive = true;
              message = `已对 ${count} 个机器人触发测试告警`;
              detail = '机器人将显示告警状态';
              break;
            case 'clear_alerts':
              this.testAlertActive = false;
              message = `已清除 ${count} 个机器人的告警`;
              detail = '所有告警已清除';
              break;
            case 'standby_mode':
              message = `已将 ${count} 个机器人切换为待机模式`;
              detail = '机器人进入待机状态';
              break;
            case 'maintenance_mode':
              message = `已将 ${count} 个机器人切换为维护模式`;
              detail = '机器人进入维护状态，请谨慎操作';
              break;
            case 'charge_mode':
              message = `已将 ${count} 个机器人切换为充电模式`;
              detail = '机器人正在前往充电站充电';
              break;
            default:
              message = `已对 ${count} 个机器人执行${operationName}操作`;
              detail = '操作执行成功';
          }

          this.recordHistory(type, robotIds, robotNames, message);

          this.operationResult = {
            title: '操作完成',
            icon: 'el-icon-success',
            color: '#67c23a',
            message: message,
            detail: detail
          };
          this.resultDialogVisible = true;

          if (type !== 'batch_restart') {
            setTimeout(() => {
              this.getRobotList();
            }, 1000);
          }

          this.$message.success(message);
        } else {
          this.$message.error(response.msg || `${operationName}操作失败`);
          this.recordHistory(type, robotIds, robotNames, `${operationName}操作失败: ${response.msg || '未知错误'}`, 'fail');
          if (type === 'batch_restart') {
            this.getRobotList();
          }
        }
      }).catch(error => {
        loadingMessage.close();
        this.addDebugLog(`API调用失败: ${error.message}`, error, 'error');
        console.error(`${operationName}操作失败`, error);
        const errorMsg = error.msg || error.message || '网络错误';
        this.$message.error(`${operationName}操作失败：${errorMsg}`);
        this.recordHistory(type, robotIds, robotNames, `${operationName}操作失败: ${errorMsg}`, 'fail');
        if (type === 'batch_restart') {
          this.getRobotList();
        }
      }).finally(() => {
        this.loading = false;
      });
    },

    startPollingRestartStatus(robotIds) {
      this.addDebugLog(`开始轮询检查重启状态: robotIds=${JSON.stringify(robotIds)}`, null, 'info');
      let retryCount = 0;
      const maxRetries = 20;

      const checkStatus = () => {
        if (retryCount >= maxRetries) {
          this.addDebugLog('轮询超时，停止检查', null, 'warning');
          this.$message.warning('部分机器人重启状态检查超时，请手动刷新查看');
          if (this.pollingTimer) {
            clearTimeout(this.pollingTimer);
            this.pollingTimer = null;
          }
          return;
        }
        retryCount++;
        this.addDebugLog(`第${retryCount}次轮询检查`, null, 'info');
        this.getRobotList().then(() => {
          const stillRestarting = this.robotOptions.filter(
            r => robotIds.includes(r.robotId) && parseInt(r.status) === 3
          );
          const completedRobots = this.robotOptions.filter(r => robotIds.includes(r.robotId));
          const onlineCount = completedRobots.filter(r => parseInt(r.status) === 1).length;
          const offlineCount = completedRobots.filter(r => parseInt(r.status) === 0).length;
          this.addDebugLog(`轮询结果: 重启中=${stillRestarting.length}, 在线=${onlineCount}, 离线=${offlineCount}`,
            completedRobots.map(r => ({ name: r.robotName, status: r.status, battery: r.battery })),
            'info');
          if (stillRestarting.length > 0) {
            this.addDebugLog(`还有${stillRestarting.length}个机器人正在重启，继续轮询`, null, 'info');
            if (this.pollingTimer) {
              clearTimeout(this.pollingTimer);
            }
            this.pollingTimer = setTimeout(checkStatus, 2000);
          } else {
            this.addDebugLog(`轮询完成: 在线=${onlineCount}, 离线=${offlineCount}`, null, 'success');
            if (offlineCount > 0) {
              this.$message.warning(`重启完成：${onlineCount}个在线，${offlineCount}个离线`);
              this.addDebugLog(`警告: ${offlineCount}个机器人重启后仍为离线状态`,
                completedRobots.filter(r => parseInt(r.status) === 0), 'warning');
            } else if (onlineCount === completedRobots.length && onlineCount > 0) {
              this.$message.success(`所有机器人重启完成，当前全部在线`);
              this.addDebugLog(`重启成功: 所有机器人已恢复在线`, null, 'success');
            } else {
              this.$message.info(`重启完成，当前状态: ${onlineCount}个在线，${offlineCount}个离线`);
            }
            if (this.pollingTimer) {
              clearTimeout(this.pollingTimer);
              this.pollingTimer = null;
            }
          }
        }).catch(error => {
          this.addDebugLog(`轮询检查异常: ${error.message}`, error, 'error');
          if (this.pollingTimer) {
            clearTimeout(this.pollingTimer);
          }
          this.pollingTimer = setTimeout(checkStatus, 3000);
        });
      };
      if (this.pollingTimer) {
        clearTimeout(this.pollingTimer);
      }
      this.pollingTimer = setTimeout(checkStatus, 2000);
    },

    refreshRobotList() {
      this.refreshing = true;
      this.addDebugLog('手动刷新机器人列表', null, 'info');
      this.getRobotList().finally(() => {
        this.refreshing = false;
        this.$message.success('机器人状态已刷新');
      });
    },

    recordHistory(type, robotIds, robotNames, content, status = 'success') {
      const operationTypeMap = {
        'emergency_stop': 'emergency_stop',
        'emergency_evacuation': 'emergency_evacuation',
        'batch_restart': 'batch_restart',
        'refresh_status': 'refresh_status',
        'test_alert': 'test_alert',
        'clear_alerts': 'clear_alerts',
        'standby_mode': 'standby_mode',
        'maintenance_mode': 'maintenance_mode',
        'charge_mode': 'charge_mode'
      };

      const historyData = {
        operationTime: new Date().toISOString(),
        operationType: operationTypeMap[type] || type,
        robotId: robotIds.length === 1 ? robotIds[0] : null,
        robotName: robotNames,
        content: content,
        operator: this.$store.state.user?.name || this.$store.state.user?.nickName || '系统',
        status: status
      };
      addHistory(historyData).catch(error => {
        console.error('保存历史记录失败', error);
      });
    },

    addDebugLog(message, data = null, type = 'info') {
      const log = { time: new Date().toLocaleTimeString(), message: message, data: data, type: type };
      this.debugLogs.unshift(log);
      if (this.debugLogs.length > 50) { this.debugLogs.pop(); }
      console.log(`[调试][${type.toUpperCase()}] ${message}`, data || '');
    },

    clearDebugLogs() {
      this.debugLogs = [];
      this.addDebugLog('日志已清空', null, 'info');
    },

    exportDebugLogs() {
      const logs = JSON.stringify(this.debugLogs, null, 2);
      const blob = new Blob([logs], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `debug_logs_${new Date().toISOString()}.json`;
      a.click();
      URL.revokeObjectURL(url);
      this.$message.success('日志已导出');
    },

    async debugGetRobotStatus() {
      if (!this.debugRobotId) {
        this.$message.warning('请先选择机器人');
        return;
      }
      this.debugLoading = true;
      this.addDebugLog(`开始查询机器人状态: robotId=${this.debugRobotId}`, null, 'info');
      try {
        const response = await request({ url: `/system/robot/debug/${this.debugRobotId}`, method: 'get' });
        if (response.code === 200) {
          this.debugResult = response.data;
          this.addDebugLog(`查询成功`, response.data, 'success');
          this.$message.success('查询成功');
          const robot = response.data;
          this.addDebugLog(`机器人状态: status=${robot.status}, battery=${robot.battery}, updateTime=${robot.updateTime}`, null, 'info');
        } else {
          this.addDebugLog(`查询失败: ${response.msg}`, null, 'error');
          this.$message.error(response.msg);
        }
      } catch (error) {
        this.addDebugLog(`查询异常: ${error.message}`, error, 'error');
        this.$message.error('查询失败');
      } finally {
        this.debugLoading = false;
      }
    },

    async debugForceUpdateOnline() {
      if (!this.debugRobotId) {
        this.$message.warning('请先选择机器人');
        return;
      }
      this.debugLoading = true;
      this.addDebugLog(`开始强制更新机器人状态为在线: robotId=${this.debugRobotId}`, null, 'warning');
      try {
        const response = await request({
          url: `/system/robot/debug/forceUpdate`,
          method: 'put',
          data: { robotId: this.debugRobotId, status: 1, battery: 100 }
        });
        if (response.code === 200) {
          this.addDebugLog(`强制更新成功`, response.data, 'success');
          this.$message.success('强制更新成功');
          await this.debugRefreshList();
        } else {
          this.addDebugLog(`强制更新失败: ${response.msg}`, null, 'error');
          this.$message.error(response.msg);
        }
      } catch (error) {
        this.addDebugLog(`强制更新异常: ${error.message}`, error, 'error');
        this.$message.error('强制更新失败');
      } finally {
        this.debugLoading = false;
      }
    },

    async debugRefreshList() {
      this.debugLoading = true;
      this.addDebugLog('开始刷新机器人列表', null, 'info');
      try {
        await this.getRobotList();
        this.addDebugLog(`刷新成功，共${this.robotOptions.length}个机器人`,
          this.robotOptions.map(r => ({ name: r.robotName, status: r.status, battery: r.battery })),
          'success');
        this.$message.success('刷新成功');
      } catch (error) {
        this.addDebugLog(`刷新失败: ${error.message}`, error, 'error');
        this.$message.error('刷新失败');
      } finally {
        this.debugLoading = false;
      }
    },

    async debugClearCache() {
      this.debugLoading = true;
      this.addDebugLog('开始清除缓存', null, 'warning');
      try {
        const response = await request({ url: `/system/robot/debug/clearCache`, method: 'delete' });
        if (response.code === 200) {
          this.addDebugLog(`清除缓存成功`, null, 'success');
          this.$message.success('缓存已清除');
          await this.debugRefreshList();
        } else {
          this.addDebugLog(`清除缓存失败: ${response.msg}`, null, 'error');
          this.$message.error(response.msg);
        }
      } catch (error) {
        this.addDebugLog(`清除缓存异常: ${error.message}`, error, 'error');
        this.$message.error('清除缓存失败');
      } finally {
        this.debugLoading = false;
      }
    },

    async debugTestRestart() {
      if (!this.debugRobotId) {
        this.$message.warning('请先选择机器人');
        return;
      }
      this.debugLoading = true;
      this.addDebugLog(`开始测试重启: robotId=${this.debugRobotId}`, null, 'warning');
      try {
        const response = await batchRestart([this.debugRobotId]);
        if (response.code === 200) {
          this.addDebugLog(`测试重启已提交`, response.data, 'success');
          this.$message.success('重启命令已发送');
          const robot = this.robotOptions.find(r => r.robotId === this.debugRobotId);
          if (robot) { robot.status = 3; }
          this.startPollingRestartStatus([this.debugRobotId]);
        } else {
          this.addDebugLog(`测试重启失败: ${response.msg}`, null, 'error');
          this.$message.error(response.msg);
        }
      } catch (error) {
        this.addDebugLog(`测试重启异常: ${error.message}`, error, 'error');
        this.$message.error('重启失败');
      } finally {
        this.debugLoading = false;
      }
    },

    copyToClipboard(text) {
      navigator.clipboard.writeText(text).then(() => {
        this.$message.success('已复制到剪贴板');
      }).catch(() => {
        this.$message.error('复制失败');
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

.header-actions {
  display: flex;
  gap: 10px;
}

.card-body {
  padding: 24px 20px;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
  color: #4D4D4D;
  font-weight: 500;
}

.filter-bar i {
  color: #3976E4;
  margin-right: 4px;
}

.selection-section {
  margin-top: 20px;
}

.selection-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.robot-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 10px;
  max-height: 300px;
  overflow-y: auto;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #E5E7EB;
}

.robot-item {
  display: flex;
  align-items: center;
  padding: 8px;
  background-color: white;
  border-radius: 4px;
  border: 1px solid #ebeef5;
  width: 100%;
  margin-right: 0;
}

.robot-info {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.robot-name {
  font-weight: 600;
  font-size: 14px;
  min-width: 70px;
}

.status-tag {
  font-size: 11px;
}

.battery {
  font-size: 12px;
  font-weight: 600;
  margin-left: auto;
}

.battery-high { color: #67c23a; }
.battery-medium { color: #e6a23c; }
.battery-low { color: #f56c6c; }
.battery-danger { color: #f56c6c; font-weight: 700; }

.selected-info {
  margin-top: 15px;
  padding: 10px;
  background-color: #ecf5ff;
  border-left: 4px solid #3976E4;
  border-radius: 4px;
  font-size: 14px;
}

.selected-info i {
  color: #3976E4;
  margin-right: 8px;
}

.operation-section {
  margin-top: 20px;
}

.operation-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  overflow: hidden;
}

.operation-card .el-card__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px !important;
}

.button-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
}

.operation-btn {
  width: 100%;
  justify-content: flex-start;
  text-align: left;
  margin-left: 0 !important;
  padding: 12px 15px;
}

.operation-btn i {
  margin-right: 8px;
  width: 20px;
  text-align: center;
}

.operation-card.emergency .el-card__header {
  background-color: #fef0f0;
  color: #f56c6c;
  border-bottom: 1px solid #fbc4c4;
}

.operation-card.status .el-card__header {
  background-color: #fdf6ec;
  color: #e6a23c;
  border-bottom: 1px solid #f5dab1;
}

.operation-card.system .el-card__header {
  background-color: #ecf5ff;
  color: #3976E4;
  border-bottom: 1px solid #c6e2ff;
}

.operation-btn.emergency-stop {
  background-color: #f56c6c !important;
  border-color: #f56c6c !important;
  color: white !important;
}

.operation-btn.emergency-stop:hover {
  background-color: #f78989 !important;
  border-color: #f78989 !important;
}

.operation-btn.emergency-evacuation {
  background-color: #e6a23c !important;
  border-color: #e6a23c !important;
  color: white !important;
}

.operation-btn.emergency-evacuation:hover {
  background-color: #ebb563 !important;
  border-color: #ebb563 !important;
}

.operation-btn.batch-restart {
  background-color: #909399 !important;
  border-color: #909399 !important;
  color: white !important;
}

.operation-btn.batch-restart:hover {
  background-color: #a6a9ad !important;
  border-color: #a6a9ad !important;
}

.operation-btn.el-button--success {
  background-color: #67c23a !important;
  border-color: #67c23a !important;
  color: white !important;
}

.operation-btn.el-button--success:hover {
  background-color: #85ce61 !important;
  border-color: #85ce61 !important;
}

.operation-btn.el-button--warning {
  background-color: #e6a23c !important;
  border-color: #e6a23c !important;
  color: white !important;
}

.operation-btn.el-button--warning:hover {
  background-color: #ebb563 !important;
  border-color: #ebb563 !important;
}

.operation-btn.el-button--info {
  background-color: #909399 !important;
  border-color: #909399 !important;
  color: white !important;
}

.operation-btn.el-button--info:hover {
  background-color: #a6a9ad !important;
  border-color: #a6a9ad !important;
}

.operation-btn.el-button--primary {
  background-color: #409eff !important;
  border-color: #409eff !important;
  color: white !important;
}

.operation-btn.el-button--primary:hover {
  background-color: #66b1ff !important;
  border-color: #66b1ff !important;
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

.operation-btn.is-disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .operation-section .el-col { margin-bottom: 20px; }
  .operation-section .el-col:last-child { margin-bottom: 0; }
  .card-header { flex-direction: column; align-items: flex-start; gap: 10px; }
}

.custom-debug-panel {
  position: fixed;
  top: 0;
  right: 0;
  width: 600px;
  height: 100vh;
  background: white;
  box-shadow: -2px 0 12px rgba(0, 0, 0, 0.15);
  z-index: 9999;
  display: flex;
  flex-direction: column;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from { transform: translateX(100%); }
  to { transform: translateX(0); }
}

.debug-panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  flex-shrink: 0;
}

.debug-panel-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1e2a3a;
  display: flex;
  align-items: center;
  gap: 8px;
}

.debug-panel-header h3 i {
  color: #3976E4;
}

.debug-panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.debug-section {
  margin-bottom: 24px;
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 20px;
}

.debug-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.debug-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #1e2a3a;
  display: flex;
  align-items: center;
  gap: 8px;
}

.debug-section h4 i {
  color: #3976E4;
}

.debug-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.debug-result {
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #e5e7eb;
  max-height: 300px;
  overflow: auto;
}

.debug-result pre {
  margin: 0;
  padding: 12px;
  font-size: 12px;
  font-family: 'Monaco', 'Menlo', monospace;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.debug-logs {
  background-color: #1e2a3a;
  border-radius: 4px;
  max-height: 400px;
  overflow-y: auto;
  margin-bottom: 10px;
}

.log-list {
  padding: 8px;
}

.log-item {
  padding: 8px 12px;
  border-bottom: 1px solid #2d3a4a;
  font-size: 12px;
  font-family: 'Monaco', 'Menlo', monospace;
}

.log-item:last-child {
  border-bottom: none;
}

.log-info { color: #a0a0a0; }
.log-success { color: #67c23a; }
.log-warning { color: #e6a23c; }
.log-error { color: #f56c6c; }

.log-time {
  color: #909399;
  margin-right: 12px;
  font-size: 11px;
}

.log-message {
  color: #e5e7eb;
  word-break: break-all;
}

.log-data {
  margin-top: 8px;
  padding: 8px;
  background-color: #2d3a4a;
  border-radius: 4px;
  font-size: 11px;
  color: #a0a0a0;
  overflow-x: auto;
  white-space: pre-wrap;
}

.log-empty {
  padding: 20px;
  text-align: center;
  color: #909399;
  font-size: 12px;
}

.debug-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.console-commands {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.command-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background-color: #f8f9fa;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.command-item:hover {
  background-color: #ecf5ff;
}

.command-item code {
  font-size: 12px;
  font-family: 'Monaco', 'Menlo', monospace;
  color: #1e2a3a;
}

.command-item i {
  color: #909399;
  font-size: 14px;
}

.command-item:hover i {
  color: #3976E4;
}
</style>
