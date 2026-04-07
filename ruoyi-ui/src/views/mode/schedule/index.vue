<template>
  <div class="card">
    <div class="card-header">
      <div class="card-title">
        <i class="fas fa-calendar-alt"></i> 模式排程
      </div>
      <div class="header-actions">
        <el-button type="primary" size="small" @click="handleAdd">
          <i class="fas fa-plus"></i> 新建排程
        </el-button>
        <el-button
          type="success"
          size="small"
          :class="{ active: viewMode === 'calendar' }"
          @click="switchToCalendarView"
        >
          <i class="fas fa-calendar-alt"></i> 日历视图
        </el-button>
        <el-button
          type="primary"
          size="small"
          :class="{ active: viewMode === 'table' }"
          @click="viewMode = 'table'"
        >
          <i class="fas fa-table"></i> 表格视图
        </el-button>
      </div>
    </div>

    <div class="card-body">
      <!-- 搜索栏 -->
      <div class="filter-bar">
        <el-input
          v-model="queryParams.scheduleName"
          placeholder="搜索排程名称"
          style="width:200px;"
          size="small"
          clearable
          @keyup.enter.native="handleQuery"
        >
          <template #prefix><i class="fas fa-search"></i></template>
        </el-input>

        <el-select v-model="queryParams.modeId" placeholder="模式" clearable size="small" style="width:150px;">
          <el-option
            v-for="mode in modeOptions"
            :key="mode.modeId"
            :label="mode.modeName"
            :value="mode.modeId"
          />
        </el-select>

        <el-select v-model="queryParams.status" placeholder="状态" clearable size="small" style="width:150px;">
          <el-option label="进行中" value="running" />
          <el-option label="已暂停" value="paused" />
          <el-option label="待执行" value="pending" />
          <el-option label="已完成" value="completed" />
          <el-option label="失败" value="failed" />
        </el-select>

        <el-button type="primary" size="small" @click="handleQuery">
          <i class="fas fa-search"></i> 查询
        </el-button>
        <el-button size="small" @click="resetQuery">
          <i class="fas fa-undo"></i> 重置
        </el-button>

        <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" style="margin-left: auto;"></right-toolbar>
      </div>

      <!-- 调度统计卡片 -->
      <div class="stats-cards">
        <div class="stat-card total" @click="showScheduleDetail('total')">
          <div class="stat-icon"><i class="fas fa-database"></i></div>
          <div class="stat-info">
            <div class="stat-value">{{ totalSchedules }}</div>
            <div class="stat-label">总调度数</div>
          </div>
        </div>
        <div class="stat-card running" @click="showScheduleDetail('running')">
          <div class="stat-icon"><i class="fas fa-play-circle"></i></div>
          <div class="stat-info">
            <div class="stat-value">{{ runningSchedules }}</div>
            <div class="stat-label">进行中</div>
          </div>
        </div>
        <div class="stat-card paused" @click="showScheduleDetail('paused')">
          <div class="stat-icon"><i class="fas fa-pause-circle"></i></div>
          <div class="stat-info">
            <div class="stat-value">{{ pausedSchedules }}</div>
            <div class="stat-label">已暂停</div>
          </div>
        </div>
        <div class="stat-card pending" @click="showScheduleDetail('pending')">
          <div class="stat-icon"><i class="fas fa-clock"></i></div>
          <div class="stat-info">
            <div class="stat-value">{{ pendingSchedules }}</div>
            <div class="stat-label">待执行</div>
          </div>
        </div>
      </div>

      <!-- 日历视图区域 -->
      <div v-if="viewMode === 'calendar'" class="calendar-view">
        <!-- 日历统计信息卡片 -->
        <div class="stats-cards small">
          <div class="stat-item success">
            <div class="stat-value">{{ calendarStats.success }}</div>
            <div class="stat-label">已完成</div>
          </div>
          <div class="stat-item partial">
            <div class="stat-value">{{ calendarStats.partial }}</div>
            <div class="stat-label">部分完成</div>
          </div>
          <div class="stat-item failed">
            <div class="stat-value">{{ calendarStats.failed }}</div>
            <div class="stat-label">失败</div>
          </div>
          <div class="stat-item pending">
            <div class="stat-value">{{ calendarStats.pending }}</div>
            <div class="stat-label">待执行</div>
          </div>
          <div class="stat-item running">
            <div class="stat-value">{{ calendarStats.running }}</div>
            <div class="stat-label">进行中</div>
          </div>
          <div class="stat-item paused">
            <div class="stat-value">{{ calendarStats.paused }}</div>
            <div class="stat-label">已暂停</div>
          </div>
        </div>

        <!-- 日历主体 -->
        <div class="calendar-container">
          <!-- 月份导航 -->
          <div class="month-nav">
            <el-button type="text" @click="prevMonth" :disabled="currentMonth === 0">
              <i class="fas fa-chevron-left"></i> 上个月
            </el-button>
            <span class="current-month">{{ currentMonthName }} {{ calendarYear }}年</span>
            <el-button type="text" @click="nextMonth" :disabled="currentMonth === 11">
              下个月 <i class="fas fa-chevron-right"></i>
            </el-button>
          </div>

          <!-- 星期标题 -->
          <div class="weekdays">
            <div v-for="day in weekdays" :key="day" class="weekday">{{ day }}</div>
          </div>

          <!-- 日历格子 -->
          <div class="calendar-grid">
            <!-- 空白格子（上月剩余） -->
            <div
              v-for="n in startDayOfMonth"
              :key="'empty-' + n"
              class="calendar-cell empty"
            ></div>

            <!-- 本月日期 -->
            <div
              v-for="day in daysInMonth"
              :key="day"
              class="calendar-cell"
              :class="getCellClass(day)"
              @click="showDayDetail(day)"
            >
              <div class="day-number">{{ day }}</div>
              <div class="task-info" v-if="getDayTasks(day).length > 0">
                <el-tooltip
                  placement="top"
                  :content="getTaskTooltip(day)"
                  effect="light"
                >
                  <div class="task-count">
                    <span class="total-count">{{ getDayTasks(day).length }}</span>
                  </div>
                </el-tooltip>
              </div>
              <!-- 今日标记 -->
              <div v-if="isToday(day)" class="today-mark">今日</div>
            </div>
          </div>
        </div>

        <!-- 图例 -->
        <div class="legend">
          <span><i class="success-dot"></i> 已完成</span>
          <span><i class="partial-dot"></i> 部分完成</span>
          <span><i class="failed-dot"></i> 失败</span>
          <span><i class="pending-dot"></i> 待执行</span>
          <span><i class="running-dot"></i> 进行中</span>
          <span><i class="paused-dot"></i> 已暂停</span>
          <span><i class="today-dot"></i> 今日</span>
          <span><i class="default-dot"></i> 无任务</span>
        </div>
      </div>

      <!-- 调度表格视图 -->
      <div v-else>
        <el-table v-loading="loading" :data="scheduleList" @selection-change="handleSelectionChange" border>
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="排程名称" align="center" prop="scheduleName" width="150" />
          <el-table-column label="模式" align="center" prop="modeName" width="100" />

          <!-- 目标机器人列 - 显示关联的真实机器人 -->
          <el-table-column label="目标机器人" align="center" width="250">
            <template slot-scope="scope">
              <div v-if="scope.row.robots && scope.row.robots.length > 0">
                <el-tag
                  v-for="robot in scope.row.robots"
                  :key="robot.robotId"
                  size="small"
                  :type="getRobotTagType(robot.status)"
                  style="margin-right: 5px; margin-bottom: 5px;"
                >
                  {{ robot.name || `机器人${robot.robotId}` }}
                </el-tag>
              </div>
              <div v-else-if="scope.row.robotIds && scope.row.robotIds.length > 0">
                <!-- 如果只有robotIds，从robotOptions中查找 -->
                <el-tag
                  v-for="robotId in scope.row.robotIds.slice(0, 3)"
                  :key="robotId"
                  size="small"
                  style="margin-right: 5px; margin-bottom: 5px;"
                >
                  {{ getRobotNameById(robotId) }}
                </el-tag>
                <el-tag
                  v-if="scope.row.robotIds.length > 3"
                  size="small"
                  type="info"
                  style="margin-bottom: 5px;"
                >
                  +{{ scope.row.robotIds.length - 3 }}
                </el-tag>
              </div>
              <span v-else class="no-data">未分配机器人</span>
            </template>
          </el-table-column>

          <el-table-column label="执行时间" align="center" prop="executeTime" width="180" />
          <el-table-column label="重复" align="center" prop="repeatType" width="100">
            <template slot-scope="scope">
              {{ repeatTypeText(scope.row.repeatType) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" align="center" width="100">
            <template slot-scope="scope">
              <span :class="'status-tag status-' + scope.row.status">
                {{ scheduleStatusText(scope.row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="上次执行" align="center" width="160">
            <template slot-scope="scope">
              <div v-if="scope.row.lastExecuteTime">
                {{ scope.row.lastExecuteTime }}
                <span :class="'status-tag status-' + (scope.row.lastExecuteStatus === 'success' ? 'success' : 'failed')" style="margin-left: 5px;">
                  {{ scope.row.lastExecuteStatus === 'success' ? '成功' : '失败' }}
                </span>
              </div>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" fixed="right" width="220">
            <template slot-scope="scope">
              <el-button size="mini" type="text" @click="handleUpdate(scope.row)">
                <i class="fas fa-edit"></i> 编辑
              </el-button>
              <el-button
                v-if="scope.row.status === 'running' || scope.row.status === 'paused'"
                size="mini"
                type="text"
                @click="handleToggleStatus(scope.row)"
              >
                <i :class="scope.row.status === 'running' ? 'fas fa-pause' : 'fas fa-play'"></i>
                {{ scope.row.status === 'running' ? '暂停' : '恢复' }}
              </el-button>
              <el-button
                size="mini"
                type="text"
                class="danger"
                @click="handleDelete(scope.row)"
              >
                <i class="fas fa-trash"></i> 删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrap">
          <pagination
            v-show="total>0"
            :total="total"
            :page.sync="queryParams.pageNum"
            :limit.sync="queryParams.pageSize"
            @pagination="getList"
          />
        </div>
      </div>
    </div>

    <!-- 添加或修改排程对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="排程名称" prop="scheduleName">
              <el-input v-model="form.scheduleName" placeholder="请输入排程名称" maxlength="100" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="模式" prop="modeId">
              <el-select v-model="form.modeId" placeholder="请选择模式" style="width:100%">
                <el-option
                  v-for="mode in modeOptions"
                  :key="mode.modeId"
                  :label="mode.modeName"
                  :value="mode.modeId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="重复类型" prop="repeatType">
              <el-select v-model="form.repeatType" placeholder="请选择重复类型" style="width:100%">
                <el-option label="单次" value="once" />
                <el-option label="每天" value="daily" />
                <el-option label="每周" value="weekly" />
                <el-option label="每月" value="monthly" />
                <el-option label="工作日" value="weekdays" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker
                v-model="form.startDate"
                type="date"
                placeholder="选择开始日期"
                style="width:100%"
                :picker-options="datePickerOptions"
                value-format="yyyy-MM-dd"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-time-picker
                v-model="form.startTime"
                placeholder="选择开始时间"
                style="width:100%"
                value-format="HH:mm"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="持续时间(小时)" prop="duration">
              <el-input-number v-model="form.duration" :min="0.5" :max="24" :step="0.5" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态" style="width:100%">
                <el-option label="待执行" value="pending" />
                <el-option label="进行中" value="running" />
                <el-option label="已暂停" value="paused" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="目标机器人" prop="robotIds">
              <el-select
                v-model="form.robotIds"
                multiple
                filterable
                placeholder="请选择机器人"
                style="width:100%"
              >
                <el-option
                  v-for="robot in robotOptions"
                  :key="robot.robotId"
                  :label="`${robot.robotName} (${robot.robotCode})`"
                  :value="robot.robotId"
                >
                  <span style="float: left">{{ robot.robotName }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">
                    {{ robot.status === 'online' ? '在线' : robot.status === 'low_battery' ? '低电量' : '离线' }}
                  </span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 调度详情对话框 -->
    <el-dialog :title="detailTitle" :visible.sync="detailOpen" width="800px" append-to-body>
      <el-table :data="detailSchedules" border>
        <el-table-column label="排程名称" prop="scheduleName" />
        <el-table-column label="模式" prop="modeName" />
        <el-table-column label="执行时间" prop="executeTime" />
        <el-table-column label="机器人" width="200">
          <template slot-scope="scope">
            <div v-if="scope.row.robots && scope.row.robots.length > 0">
              <el-tag
                v-for="robot in scope.row.robots.slice(0, 3)"
                :key="robot.robotId"
                size="small"
                style="margin-right: 5px;"
              >
                {{ robot.robotName }}
              </el-tag>
            </div>
            <span v-else-if="scope.row.robotIds && scope.row.robotIds.length > 0">
              {{ scope.row.robotIds.length }}个机器人
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center">
          <template slot-scope="scope">
            <span :class="'status-tag status-' + scope.row.status">
              {{ scheduleStatusText(scope.row.status) }}
            </span>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>

    <!-- 任务详情对话框 -->
    <el-dialog :title="selectedDate + ' 任务详情'" :visible.sync="taskDetailVisible" width="700px">
      <el-table :data="selectedDayTasks" border>
        <el-table-column label="排程名称" prop="scheduleName" />
        <el-table-column label="模式" prop="modeName" />
        <el-table-column label="机器人" width="200">
          <template slot-scope="scope">
            <div v-if="scope.row.robots && scope.row.robots.length > 0">
              <el-tag
                v-for="robot in scope.row.robots.slice(0, 3)"
                :key="robot.robotId"
                size="small"
                style="margin-right: 5px;"
              >
                {{ robot.robotName }}
              </el-tag>
              <span v-if="scope.row.robots.length > 3">+{{ scope.row.robots.length - 3 }}</span>
            </div>
            <span v-else-if="scope.row.robotIds && scope.row.robotIds.length > 0">
              {{ scope.row.robotIds.length }}个机器人
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center">
          <template slot-scope="scope">
            <span :class="'status-tag status-' + scope.row.displayStatus">
              {{ scheduleStatusText(scope.row.displayStatus) }}
            </span>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="taskDetailVisible = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listSchedule, getSchedule, delSchedule, addSchedule, updateSchedule, toggleScheduleStatus } from "@/api/mode/schedule";
import { listMode } from "@/api/mode/mode";
import { listRobot } from "@/api/mode/robot";

export default {
  name: "Schedule",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      scheduleList: [],
      modeOptions: [],
      robotOptions: [],
      robotMap: {}, // 添加机器人ID到机器人对象的映射
      title: "",
      open: false,
      detailOpen: false,
      detailTitle: "",
      detailType: "",
      detailSchedules: [],
      totalSchedules: 0,
      runningSchedules: 0,
      pausedSchedules: 0,
      pendingSchedules: 0,
      completedSchedules: 0,
      failedSchedules: 0,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        scheduleName: null,
        modeId: null,
        status: null
      },
      form: {
        scheduleId: null,
        scheduleName: null,
        modeId: null,
        robotIds: [],
        startDate: null,
        startTime: null,
        repeatType: 'daily',
        duration: 2,
        status: 'pending'
      },
      rules: {
        scheduleName: [
          { required: true, message: "排程名称不能为空", trigger: "blur" }
        ],
        modeId: [
          { required: true, message: "请选择调度模式", trigger: "change" }
        ],
        robotIds: [
          { required: true, message: "请选择至少一个机器人", trigger: "change" }
        ],
        startDate: [
          { required: true, message: "请选择开始日期", trigger: "change" }
        ],
        startTime: [
          { required: true, message: "请选择开始时间", trigger: "change" }
        ]
      },

      // 视图模式相关
      viewMode: 'table',
      calendarYear: new Date().getFullYear(),
      currentMonth: new Date().getMonth(),

      // 日历数据
      calendarData: [],
      weekdays: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],

      // 任务详情相关
      taskDetailVisible: false,
      selectedDate: '',
      selectedDayTasks: [],

      // 日历统计
      calendarStats: {
        success: 0,
        partial: 0,
        failed: 0,
        pending: 0,
        running: 0,
        paused: 0,
        total: 0
      },

      // 日期选择器选项
      datePickerOptions: {
        disabledDate(time) {
          return time.getTime() < new Date('2024-01-01').getTime();
        }
      },

      // 今日日期
      todayDate: ''
    };
  },
  computed: {
    startDayOfMonth() {
      const date = new Date(parseInt(this.calendarYear), this.currentMonth, 1);
      let day = date.getDay();
      return day === 0 ? 6 : day - 1;
    },
    daysInMonth() {
      return new Date(parseInt(this.calendarYear), this.currentMonth + 1, 0).getDate();
    },
    currentMonthName() {
      const months = ['一月', '二月', '三月', '四月', '五月', '六月',
        '七月', '八月', '九月', '十月', '十一月', '十二月'];
      return months[this.currentMonth];
    }
  },
  created() {
    this.getList();
    this.getModeOptions();
    this.getRobotOptions();
    this.setTodayDate();
  },
  methods: {
    // 获取机器人标签类型（根据状态）
    getRobotTagType(status) {
      const typeMap = {
        'online': 'success',
        'low_battery': 'warning',
        'offline': 'info'
      };
      return typeMap[status] || 'info';
    },

    // 根据机器人ID获取机器人名称
    getRobotNameById(robotId) {
      const robot = this.robotMap[robotId];
      if (robot) {
        return robot.robotName;
      }
      // 从robotOptions中查找
      const found = this.robotOptions.find(r => r.robotId === robotId);
      if (found) {
        return found.robotName;
      }
      return `机器人${robotId}`;
    },

    // 获取重复类型文本
    repeatTypeText(type) {
      const map = {
        'once': '单次',
        'daily': '每天',
        'weekly': '每周',
        'monthly': '每月',
        'weekdays': '工作日'
      };
      return map[type] || type;
    },

    getList() {
      this.loading = true;
      listSchedule(this.queryParams).then(response => {
        this.scheduleList = response.rows || [];
        this.total = response.total || 0;
        this.loading = false;

        // 调试：打印第一条数据的robots字段
        if (this.scheduleList.length > 0) {
          console.log('排程数据示例:', this.scheduleList[0]);
          console.log('robots字段:', this.scheduleList[0].robots);
          console.log('robotIds字段:', this.scheduleList[0].robotIds);
        }

        this.calculateStats();
        if (this.viewMode === 'calendar') {
          this.buildCalendarData();
        }
      }).catch(error => {
        console.error('获取排程列表失败:', error);
        this.loading = false;
      });
    },

    getModeOptions() {
      listMode({ pageNum: 1, pageSize: 100 }).then(response => {
        this.modeOptions = response.rows || [];
      }).catch(error => {
        console.error('获取模式列表失败:', error);
      });
    },

    getRobotOptions() {
      listRobot({ pageNum: 1, pageSize: 100 }).then(response => {
        this.robotOptions = response.rows || [];
        // 构建机器人ID到机器人对象的映射
        this.robotMap = {};
        this.robotOptions.forEach(robot => {
          this.robotMap[robot.robotId] = robot;
        });
        console.log('机器人列表加载完成，共', this.robotOptions.length, '个机器人');
        console.log('机器人映射:', this.robotMap);
      }).catch(error => {
        console.error('获取机器人列表失败:', error);
      });
    },

    calculateStats() {
      this.totalSchedules = this.scheduleList.length;
      this.runningSchedules = this.scheduleList.filter(s => s.status === 'running').length;
      this.pausedSchedules = this.scheduleList.filter(s => s.status === 'paused').length;
      this.pendingSchedules = this.scheduleList.filter(s => s.status === 'pending').length;
      this.completedSchedules = this.scheduleList.filter(s => s.status === 'completed').length;
      this.failedSchedules = this.scheduleList.filter(s => s.status === 'failed').length;
    },

    setTodayDate() {
      const today = new Date();
      this.todayDate = `${today.getFullYear()}年${today.getMonth() + 1}月${today.getDate()}日`;
    },

    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },

    resetQuery() {
      this.queryParams.scheduleName = null;
      this.queryParams.modeId = null;
      this.queryParams.status = null;
      this.handleQuery();
    },

    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.scheduleId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },

    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "新建排程";
    },

    handleUpdate(row) {
      this.reset();
      const scheduleId = row.scheduleId || this.ids;
      getSchedule(scheduleId).then(response => {
        this.form = response.data;
        if (this.form.robots) {
          this.form.robotIds = this.form.robots.map(r => r.robotId);
        } else if (this.form.robotIds) {
          // 保持原有的robotIds
        }
        this.open = true;
        this.title = "修改排程";
      }).catch(error => {
        console.error('获取排程详情失败:', error);
      });
    },

    handleToggleStatus(row) {
      const text = row.status === 'running' ? '暂停' : '恢复';
      this.$modal.confirm('确认要' + text + ' "' + row.scheduleName + '" 吗？').then(() => {
        return toggleScheduleStatus(row.scheduleId);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess(text + "成功");
      }).catch(() => {});
    },

    handleDelete(row) {
      const scheduleIds = row.scheduleId || this.ids;
      this.$modal.confirm('是否确认删除排程"' + row.scheduleName + '"？').then(() => {
        return delSchedule(scheduleIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },

    showScheduleDetail(type) {
      this.detailType = type;
      const titleMap = {
        'total': '总调度详情',
        'running': '进行中调度详情',
        'paused': '已暂停调度详情',
        'pending': '待执行调度详情'
      };
      this.detailTitle = titleMap[type] || '调度详情';

      if (type === 'total') {
        this.detailSchedules = this.scheduleList;
      } else {
        this.detailSchedules = this.scheduleList.filter(s => s.status === type);
      }
      this.detailOpen = true;
    },

    scheduleStatusText(status) {
      const map = {
        'running': '进行中',
        'paused': '已暂停',
        'pending': '待执行',
        'completed': '已完成',
        'success': '成功',
        'partial': '部分完成',
        'failed': '失败'
      };
      return map[status] || status;
    },

    reset() {
      const today = new Date();
      const tomorrow = new Date(today);
      tomorrow.setDate(tomorrow.getDate() + 1);
      const tomorrowStr = tomorrow.toISOString().split('T')[0];

      this.form = {
        scheduleId: null,
        scheduleName: null,
        modeId: null,
        robotIds: [],
        startDate: tomorrowStr,
        startTime: '08:00',
        repeatType: 'daily',
        duration: 2,
        status: 'pending'
      };
      if (this.$refs.form) {
        this.$refs.form.clearValidate();
      }
    },

    cancel() {
      this.open = false;
      this.reset();
    },

    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.scheduleId != null) {
            updateSchedule(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            }).catch(error => {
              console.error('修改排程失败:', error);
            });
          } else {
            addSchedule(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            }).catch(error => {
              console.error('新增排程失败:', error);
            });
          }
        }
      });
    },

    switchToCalendarView() {
      this.viewMode = 'calendar';
      this.buildCalendarData();
    },

    buildCalendarData() {
      this.calendarData = [];

      this.scheduleList.forEach(schedule => {
        let taskDate = null;

        // 从 executeTime 或 startDate 获取日期
        if (schedule.executeTime) {
          const dateMatch = schedule.executeTime.match(/(\d{4}-\d{2}-\d{2})/);
          if (dateMatch) {
            taskDate = dateMatch[1];
          }
        }
        if (!taskDate && schedule.startDate) {
          taskDate = schedule.startDate;
        }

        if (taskDate) {
          let dayData = this.calendarData.find(d => d.date === taskDate);
          if (!dayData) {
            dayData = { date: taskDate, tasks: [] };
            this.calendarData.push(dayData);
          }

          dayData.tasks.push({
            ...schedule,
            displayStatus: this.getTaskStatusForDate(schedule, taskDate)
          });
        }
      });

      this.calendarData.sort((a, b) => a.date.localeCompare(b.date));
      this.calculateCalendarStats();
    },

    getTaskStatusForDate(schedule, dateStr) {
      const taskDate = new Date(dateStr);
      const today = new Date();
      today.setHours(0, 0, 0, 0);

      if (taskDate > today) {
        return schedule.status || 'pending';
      }

      if (schedule.lastExecuteStatus) {
        return schedule.lastExecuteStatus;
      }

      return schedule.status || 'pending';
    },

    getDayTasks(day) {
      const dateStr = `${this.calendarYear}-${String(this.currentMonth + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
      const dayData = this.calendarData.find(d => d.date === dateStr);
      return dayData ? dayData.tasks : [];
    },

    isToday(day) {
      const today = new Date();
      return parseInt(this.calendarYear) === today.getFullYear() &&
        this.currentMonth === today.getMonth() &&
        day === today.getDate();
    },

    getCellClass(day) {
      const tasks = this.getDayTasks(day);
      if (tasks.length === 0) return '';

      const today = new Date();
      const currentDate = new Date(parseInt(this.calendarYear), this.currentMonth, day);
      const isPast = currentDate < today;

      if (!isPast) {
        return 'future-cell';
      }

      const statusCounts = {
        success: tasks.filter(t => t.lastExecuteStatus === 'success' || t.status === 'completed' || t.status === 'success').length,
        partial: tasks.filter(t => t.lastExecuteStatus === 'partial' || t.status === 'partial').length,
        failed: tasks.filter(t => t.lastExecuteStatus === 'failed' || t.status === 'failed').length,
        running: tasks.filter(t => t.status === 'running').length,
        paused: tasks.filter(t => t.status === 'paused').length,
        pending: tasks.filter(t => t.status === 'pending').length
      };

      if (statusCounts.failed > 0) return 'failed-cell';
      if (statusCounts.partial > 0) return 'partial-cell';
      if (statusCounts.success > 0 && statusCounts.success === tasks.length) return 'success-cell';
      if (statusCounts.running > 0) return 'running-cell';
      if (statusCounts.paused > 0) return 'paused-cell';
      if (statusCounts.pending > 0) return 'pending-cell';

      return 'mixed-cell';
    },

    getTaskTooltip(day) {
      const tasks = this.getDayTasks(day);

      const success = tasks.filter(t => t.lastExecuteStatus === 'success' || t.status === 'completed' || t.status === 'success').length;
      const partial = tasks.filter(t => t.lastExecuteStatus === 'partial' || t.status === 'partial').length;
      const failed = tasks.filter(t => t.lastExecuteStatus === 'failed' || t.status === 'failed').length;
      const running = tasks.filter(t => t.status === 'running').length;
      const paused = tasks.filter(t => t.status === 'paused').length;
      const pending = tasks.filter(t => t.status === 'pending').length;

      let tooltip = `共 ${tasks.length} 个任务\n`;
      if (success > 0) tooltip += `✅ 成功: ${success}\n`;
      if (partial > 0) tooltip += `🟡 部分完成: ${partial}\n`;
      if (failed > 0) tooltip += `❌ 失败: ${failed}\n`;
      if (running > 0) tooltip += `▶️ 进行中: ${running}\n`;
      if (paused > 0) tooltip += `⏸️ 已暂停: ${paused}\n`;
      if (pending > 0) tooltip += `⏳ 待执行: ${pending}`;

      return tooltip;
    },

    prevMonth() {
      if (this.currentMonth > 0) {
        this.currentMonth--;
      } else {
        this.currentMonth = 11;
        this.calendarYear = parseInt(this.calendarYear) - 1;
      }
    },

    nextMonth() {
      if (this.currentMonth < 11) {
        this.currentMonth++;
      } else {
        this.currentMonth = 0;
        this.calendarYear = parseInt(this.calendarYear) + 1;
      }
    },

    showDayDetail(day) {
      const tasks = this.getDayTasks(day);
      if (tasks.length === 0) {
        this.$message.info('这一天没有排程任务');
        return;
      }

      this.selectedDate = `${this.calendarYear}年${this.currentMonth + 1}月${day}日`;
      this.selectedDayTasks = tasks;
      this.taskDetailVisible = true;
    },

    calculateCalendarStats() {
      let success = 0, partial = 0, failed = 0, pending = 0, running = 0, paused = 0;

      this.calendarData.forEach(day => {
        day.tasks.forEach(task => {
          const status = task.displayStatus || task.lastExecuteStatus || task.status;
          if (status === 'success' || status === 'completed') success++;
          else if (status === 'partial') partial++;
          else if (status === 'failed') failed++;
          else if (status === 'running') running++;
          else if (status === 'paused') paused++;
          else if (status === 'pending') pending++;
        });
      });

      const total = success + partial + failed + pending + running + paused;

      this.calendarStats = {
        success,
        partial,
        failed,
        pending,
        running,
        paused,
        total
      };
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

.header-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* 筛选栏样式 */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
  background-color: #f8f9fa;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid #E5E7EB;
}

.filter-bar i {
  color: #3976E4;
  margin-right: 4px;
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stats-cards.small {
  grid-template-columns: repeat(6, 1fr);
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0px 2px 2px rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s;
  border-left: 4px solid transparent;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-card.total {
  border-left-color: #3976E4;
}

.stat-card.running {
  border-left-color: #67c23a;
}

.stat-card.paused {
  border-left-color: #e6a23c;
}

.stat-card.pending {
  border-left-color: #909399;
}

.stat-icon {
  font-size: 28px;
  margin-right: 8px;
  min-width: 40px;
  text-align: center;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
  color: #4D4D4D;
}

.stat-label {
  font-size: 14px;
  color: #808080;
  margin-top: 4px;
}

/* 统计项（用于日历统计） */
.stat-item {
  background: white;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0px 2px 2px rgba(0, 0, 0, 0.5);
  text-align: center;
  border-left: 4px solid transparent;
}

.stat-item.success {
  border-left-color: #67c23a;
}
.stat-item.partial {
  border-left-color: #e6a23c;
}
.stat-item.failed {
  border-left-color: #f56c6c;
}
.stat-item.pending {
  border-left-color: #909399;
}
.stat-item.running {
  border-left-color: #409eff;
}
.stat-item.paused {
  border-left-color: #e6a23c;
}

.stat-item .stat-value {
  font-size: 20px;
  margin-bottom: 4px;
}

.stat-item .stat-label {
  font-size: 12px;
}

/* 日历视图 */
.calendar-view {
  margin-top: 20px;
}

.calendar-container {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #E5E7EB;
  margin-bottom: 16px;
}

.month-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.current-month {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  font-weight: 600;
  color: #909399;
  margin-bottom: 10px;
}

.weekday {
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
  background-color: #ebeef5;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}

.calendar-cell {
  background-color: #fff;
  min-height: 80px;
  padding: 8px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.calendar-cell:hover {
  background-color: #ecf5ff;
}

.calendar-cell.empty {
  background-color: #f8f9fa;
  cursor: default;
}

.calendar-cell.empty:hover {
  background-color: #f8f9fa;
}

.day-number {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
  font-weight: 600;
}

.today-mark {
  position: absolute;
  top: 2px;
  right: 2px;
  font-size: 10px;
  color: #409eff;
  font-weight: bold;
  background: #ecf5ff;
  padding: 2px 4px;
  border-radius: 4px;
}

.task-info {
  margin-top: 5px;
}

.task-count {
  display: flex;
  gap: 2px;
  font-size: 11px;
  flex-wrap: wrap;
  justify-content: center;
}

.total-count {
  padding: 2px 6px;
  border-radius: 10px;
  color: #606266;
  background-color: #f0f0f0;
  font-weight: bold;
  font-size: 12px;
}

/* 单元格颜色 */
.success-cell {
  background-color: #f0f9eb;
}

.partial-cell {
  background-color: #fdf6ec;
}

.failed-cell {
  background-color: #fef0f0;
}

.pending-cell {
  background-color: #f4f4f5;
}

.running-cell {
  background-color: #ecf5ff;
}

.paused-cell {
  background-color: #fdf6ec;
}

.future-cell {
  background-color: #e8f4fe;
  border: 1px dashed #409eff;
}

.mixed-cell {
  background: linear-gradient(135deg, #f0f9eb 0%, #fdf6ec 50%, #fef0f0 100%);
}

/* 图例 */
.legend {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  flex-wrap: wrap;
  margin-top: 16px;
}

.legend span {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #909399;
}

.success-dot, .partial-dot, .failed-dot, .pending-dot, .running-dot, .paused-dot, .today-dot, .default-dot {
  display: inline-block;
  width: 16px;
  height: 16px;
  border-radius: 4px;
}

.success-dot {
  background-color: #f0f9eb;
  border: 2px solid #67c23a;
}

.partial-dot {
  background-color: #fdf6ec;
  border: 2px solid #e6a23c;
}

.failed-dot {
  background-color: #fef0f0;
  border: 2px solid #f56c6c;
}

.pending-dot {
  background-color: #f4f4f5;
  border: 2px solid #909399;
}

.running-dot {
  background-color: #ecf5ff;
  border: 2px solid #409eff;
}

.paused-dot {
  background-color: #fdf6ec;
  border: 2px solid #e6a23c;
}

.today-dot {
  background-color: #fff;
  border: 2px solid #409eff;
  position: relative;
}

.today-dot::after {
  content: '今';
  position: absolute;
  font-size: 10px;
  color: #409eff;
  top: -2px;
  left: 3px;
}

.default-dot {
  background-color: #fff;
  border: 1px solid #dcdfe6;
}

/* 分页包装 */
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 按钮激活状态 */
.el-button.active {
  background-color: #1890ff !important;
  color: white !important;
  border-color: #1890ff !important;
}

/* 表格按钮样式 */
.el-button.danger {
  color: #f56c6c;
}

.el-button.danger:hover {
  color: #f78989;
}

.no-data {
  color: #909399;
  font-size: 12px;
  font-style: italic;
}

/* 状态标签样式 */
.status-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-running {
  background-color: #ecf5ff;
  color: #409eff;
}

.status-paused {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.status-pending {
  background-color: #f4f4f5;
  color: #909399;
}

.status-completed, .status-success {
  background-color: #f0f9eb;
  color: #67c23a;
}

.status-failed {
  background-color: #fef0f0;
  color: #f56c6c;
}

/* 响应式调整 */
@media (max-width: 992px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .stats-cards.small {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    justify-content: space-between;
  }

  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-bar .el-select,
  .filter-bar .el-input {
    width: 100% !important;
  }

  .stats-cards {
    grid-template-columns: 1fr;
  }

  .stats-cards.small {
    grid-template-columns: repeat(2, 1fr);
  }

  .calendar-cell {
    min-height: 60px;
    padding: 4px;
  }

  .legend {
    gap: 8px;
  }
}

@media (max-width: 480px) {
  .stats-cards.small {
    grid-template-columns: 1fr;
  }

  .weekday {
    padding: 5px;
    font-size: 12px;
  }
}
</style>
