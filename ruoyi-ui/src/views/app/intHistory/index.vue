<template>
  <div class="app-container">
    <!-- 新增：数据统计卡片区域 -->
    <div class="stats-card">
      <div class="stats-item">
        <span class="stats-label">总交互次数</span>
        <span class="stats-value">{{ statsData.totalTimes }}</span>
      </div>
      <div class="stats-item">
        <span class="stats-label">好评数</span>
        <span class="stats-value good">{{ statsData.goodTimes }}</span>
      </div>
      <div class="stats-item">
        <span class="stats-label">中评数</span>
        <span class="stats-value mid">{{ statsData.midTimes }}</span>
      </div>
      <div class="stats-item">
        <span class="stats-label">差评数</span>
        <span class="stats-value bad">{{ statsData.badTimes }}</span>
      </div>
      <div class="stats-item">
        <span class="stats-label">平均分</span>
        <span class="stats-value avg">{{ statsData.averageRating }}</span>
      </div>
    </div>

    <!-- 原有：搜索区域美化 -->
    <div class="search-card">
      <el-form 
        :model="queryParams" 
        ref="queryForm" 
        size="small" 
        :inline="true" 
        label-width="70px"
        class="search-form"
      >
        <el-form-item label="任务ID" prop="taskId">
          <el-input
            v-model="queryParams.taskId"
            placeholder="请输入任务ID"
            clearable
            @keyup.enter.native="handleQuery"
            class="search-input"
          />
        </el-form-item>
        <el-form-item label="机器人ID" prop="robotId">
          <el-input
            v-model="queryParams.robotId"
            placeholder="请输入机器人ID"
            clearable
            @keyup.enter.native="handleQuery"
            class="search-input"
          />
        </el-form-item>
        <el-form-item label="用户ID" prop="userId">
          <el-input
            v-model="queryParams.userId"
            placeholder="请输入用户ID"
            clearable
            @keyup.enter.native="handleQuery"
            class="search-input"
          />
        </el-form-item>
        <el-form-item label="操作" class="search-btn-group">
          <el-button 
            type="primary" 
            icon="el-icon-search" 
            size="mini" 
            @click="handleQuery"
            class="search-btn"
          >
            查询
          </el-button>
          <el-button 
            icon="el-icon-refresh" 
            size="mini" 
            @click="resetQuery"
            class="reset-btn"
          >
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 原有：操作按钮区域 -->
    <el-row :gutter="15" class="operate-bar mb10">
      <el-col :span="24">
        <el-button
          type="primary"
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['app:intHistory:add']"
          class="add-btn"
        >
          新增交互记录
        </el-button>
        <el-button
          type="warning"
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['app:intHistory:export']"
          class="export-btn"
        >
          导出数据
        </el-button>
        <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" class="right-toolbar" />
      </el-col>
    </el-row>

    <!-- 原有：表格区域 -->
    <el-table 
      v-loading="loading" 
      :data="intHistoryList" 
      border 
      stripe 
      highlight-current-row
      class="data-table"
      :header-cell-style="{background: '#f5f7fa', color: '#303133', fontWeight: '500'}"
    >
      <el-table-column label="任务ID" align="center" prop="taskId" width="120" />
      <el-table-column label="机器人ID" align="center" prop="robotId" width="120" />
      <el-table-column label="用户ID" align="center" prop="userId" width="100" />
      <el-table-column label="用户名称" align="center" prop="userName" width="120" />
      <el-table-column label="交互类型" align="center" prop="interactionType" width="100">
        <template slot-scope="scope">
          <el-tag :type="getTypeTagType(scope.row.interactionType)" size="mini">
            {{ getInteractionTypeName(scope.row.interactionType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="交互内容" align="center" prop="interactionContent" min-width="200" show-overflow-tooltip />
      <el-table-column label="交互时间" align="center" prop="interactionTime" width="180" />
      <el-table-column label="交互耗时(分)" align="center" prop="duration" width="100" />
      <el-table-column label="交互评分" align="center" prop="rating" width="100">
        <template slot-scope="scope">
          <el-tag :type="getRatingTagType(scope.row.rating)" size="mini">
            {{ getRatingName(scope.row.rating) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="评价文本" align="center" prop="evaluationText" min-width="150" show-overflow-tooltip />
      <el-table-column label="交互状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag :type="getStatusTagType(scope.row.status)" size="mini">
            {{ getStatusName(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="扩展信息" align="center" prop="extInfo" min-width="150" show-overflow-tooltip />
    </el-table>
    
    <!-- 原有：分页区域 -->
    <div class="pagination-container">
      <pagination
        v-show="total>0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getList"
        background
      />
    </div>

    <!-- 原有：新增对话框 - 美化版 -->
    <el-dialog 
      :title="title" 
      :visible.sync="open" 
      width="800px" 
      append-to-body
      :close-on-click-modal="false"
      class="add-dialog"
      @close="cancel"
    >
      <el-form 
        ref="form" 
        :model="form" 
        :rules="rules" 
        label-width="110px"
        class="dialog-form"
        size="small"
      >
        <!-- 基础信息分组 -->
        <div class="form-section">
          <div class="section-title">
            <i class="el-icon-info"></i> 基础信息
          </div>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="任务ID" prop="taskId">
                <el-input v-model="form.taskId" placeholder="请输入任务ID" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="交互标识" prop="interactionId">
                <el-input v-model="form.interactionId" placeholder="唯一标识" clearable />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="机器人ID" prop="robotId">
                <el-input v-model="form.robotId" placeholder="请输入机器人ID" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="用户ID" prop="userId">
                <el-input v-model="form.userId" placeholder="请输入用户ID" clearable />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="用户名称" prop="userName">
                <el-input v-model="form.userName" placeholder="请输入用户名称" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="交互类型" prop="interactionType">
                <el-select v-model="form.interactionType" placeholder="请选择交互类型">
                  <el-option label="配送" value="0" />
                  <el-option label="清洁" value="1" />
                  <el-option label="巡检" value="2" />
                  <el-option label="维保" value="3" />
                  <el-option label="安防" value="4" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 交互详情分组 -->
        <div class="form-section">
          <div class="section-title">
            <i class="el-icon-data-analysis"></i> 交互详情
          </div>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="交互时间" prop="interactionTime">
                <el-date-picker clearable
                  v-model="form.interactionTime"
                  type="datetime"
                  value-format="yyyy-MM-dd HH:mm:ss"
                  placeholder="请选择交互发生时间"
                  style="width: 100%;"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="耗时(分钟)" prop="duration">
                <el-input v-model="form.duration" placeholder="数字" type="number" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="交互评分" prop="rating">
                <el-select v-model="form.rating" placeholder="请选择交互评分">
                  <el-option label="差" value="0" />
                  <el-option label="中等" value="1" />
                  <el-option label="良好" value="2" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="交互状态" prop="status">
                <el-select v-model="form.status" placeholder="请选择交互状态">
                  <el-option label="成功" value="0" />
                  <el-option label="失败" value="1" />
                  <el-option label="超时" value="2" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="交互内容" prop="interactionContent">
            <el-input 
              v-model="form.interactionContent" 
              type="textarea" 
              :rows="3" 
              placeholder="请输入交互内容（请求/响应数据）"
              clearable
              maxlength="1000"
              show-word-limit
            />
          </el-form-item>
        </div>

        <!-- 评价反馈分组 -->
        <div class="form-section">
          <div class="section-title">
            <i class="el-icon-comments-solid"></i> 评价反馈
          </div>
          <el-form-item label="评价文本" prop="evaluationText">
            <el-input 
              v-model="form.evaluationText" 
              type="textarea" 
              :rows="2" 
              placeholder="请输入用户的评价文本"
              clearable
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          <el-form-item label="扩展信息" prop="extInfo">
            <el-input 
              v-model="form.extInfo" 
              type="textarea" 
              :rows="2" 
              placeholder="请输入扩展信息（JSON格式）"
              clearable
              maxlength="2000"
              show-word-limit
            />
          </el-form-item>
        </div>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listIntHistory, addIntHistory } from "@/api/app/intHistory"
import { getSumOfIntHistory } from "@/api/app/intHistory"

export default {
  name: "IntHistory",
  data() {
    return {
      loading: true,
      submitLoading: false,
      showSearch: true,
      total: 0,
      intHistoryList: [],
      title: "",
      open: false,
      // 新增：统计数据初始化
      statsData: {
        totalTimes: 0,
        goodTimes: 0,
        midTimes: 0,
        badTimes: 0,
        averageRating: 0.0
      },
      queryParams: {
        pageNum: 1,
        pageSize: 15,
        taskId: null,
        robotId: null,
        userId: null
      },
      form: {},
      rules: {
        taskId: [{ required: true, message: "任务ID不能为空", trigger: "blur" }],
        interactionId: [{ required: true, message: "单次交互唯一标识不能为空", trigger: "blur" }],
        robotId: [{ required: true, message: "机器人ID不能为空", trigger: "blur" }],
        userId: [{ required: true, message: "用户ID不能为空", trigger: "blur" }],
        userName: [{ required: true, message: "用户名称不能为空", trigger: "blur" }],
        interactionType: [{ required: true, message: "交互类型不能为空", trigger: "change" }],
        interactionTime: [{ required: true, message: "交互时间不能为空", trigger: "blur" }],
        status: [{ required: true, message: "交互状态不能为空", trigger: "change" }],
        rating: [{ required: true, message: "交互评分不能为空", trigger: "change" }]
      },
      interactionTypeMap: { 0: "配送", 1: "清洁", 2: "巡检", 3: "维保", 4: "安防" },
      ratingMap: { 0: "差", 1: "中等", 2: "良好" },
      statusMap: { 0: "成功", 1: "失败", 2: "超时" },
      typeTagMap: { 0: "primary", 1: "success", 2: "info", 3: "warning", 4: "danger" },
      ratingTagMap: { 0: "danger", 1: "warning", 2: "success" },
      statusTagMap: { 0: "success", 1: "danger", 2: "warning" }
    }
  },
  created() {
    this.getList()
    // 新增：页面加载时获取统计数据
    this.getStatsData()
  },
  methods: {
    // 新增：获取统计数据的方法
    getStatsData() {
      getSumOfIntHistory().then(response => {
        // 接口返回的data字段赋值给statsData
        this.statsData = response.data
      }).catch(error => {
        console.error("获取统计数据失败：", error)
      })
    },
    getInteractionTypeName(type) {
      return this.interactionTypeMap[Number(type)] || "未知"
    },
    getTypeTagType(type) {
      return this.typeTagMap[Number(type)] || "default"
    },
    getRatingName(rating) {
      return this.ratingMap[Number(rating)] || "未知"
    },
    getRatingTagType(rating) {
      return this.ratingTagMap[Number(rating)] || "default"
    },
    getStatusName(status) {
      return this.statusMap[Number(status)] || "未知"
    },
    getStatusTagType(status) {
      return this.statusTagMap[Number(status)] || "default"
    },
    getList() {
      this.loading = true
      listIntHistory(this.queryParams).then(response => {
        this.intHistoryList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        id: null, taskId: null, interactionId: null, robotId: null, userId: null, userName: null,
        interactionType: null, interactionContent: null, interactionTime: null, duration: null,
        rating: null, evaluationText: null, status: null, extInfo: null
      }
      this.resetForm("form")
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
      // 新增：查询表格数据时同步刷新统计数据
      this.getStatsData()
    },
    resetQuery() {
      this.queryParams = {
        pageNum: 1,
        pageSize: 15,
        taskId: null,
        robotId: null,
        userId: null
      }
      this.resetForm("queryForm")
      this.getList()
      // 新增：重置查询时同步刷新统计数据
      this.getStatsData()
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "新增交互历史记录"
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          addIntHistory(this.form).then(() => {
            this.$modal.msgSuccess("新增成功")
            this.open = false
            this.getList()
            // 新增：新增数据后同步刷新统计数据
            this.getStatsData()
          })
        }
      })
    },
    handleExport() {
      this.download('app/intHistory/export', { ...this.queryParams }, `intHistory_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

<style scoped>
/* 新增：统计卡片样式 */
.stats-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  margin-bottom: 15px;
  display: flex;
  justify-content: space-around;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}
.stats-item {
  text-align: center;
  flex: 1;
  min-width: 100px;
}
.stats-label {
  display: block;
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}
.stats-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}
.stats-value.good {
  color: #67c23a; /* 好评绿色 */
}
.stats-value.mid {
  color: #e6a23c; /* 中评黄色 */
}
.stats-value.bad {
  color: #f56c6c; /* 差评红色 */
}
.stats-value.avg {
  color: #409eff; /* 平均分蓝色 */
}

/* 原有样式 */
.mb10 {
  margin-bottom: 10px;
}
.search-card {
  background: #fff;
  padding: 15px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  margin-bottom: 15px;
}
.search-form {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}
.search-input {
  width: 180px;
}
.search-btn-group {
  margin-left: 10px;
}
.operate-bar {
  padding: 10px 0;
}
.add-btn {
  margin-right: 10px;
}
.data-table {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}
.pagination-container {
  margin-top: 20px;
  text-align: center;
}
.add-dialog /deep/ .el-dialog__header {
  border-bottom: 1px solid #e4e7ed;
  padding-bottom: 10px;
}
.dialog-footer {
  text-align: right;
  padding-top: 10px;
  border-top: 1px solid #e4e7ed;
}

/* 新增：form-section 分组样式 */
.form-section {
  margin-bottom: 22px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}
.form-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}
.section-title {
  display: flex;
  align-items: center;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #409eff;
}
.section-title i {
  margin-right: 8px;
  color: #409eff;
  font-size: 16px;
}

.dialog-form /deep/ .el-form-item {
  margin-bottom: 16px;
}
</style>