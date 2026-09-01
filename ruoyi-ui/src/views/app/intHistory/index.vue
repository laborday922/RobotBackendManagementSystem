<template>
  <div class="app-container">
    <el-card class="stats-cards" shadow="never">
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
    </el-card>

    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="70px">
        <el-form-item label="机器人ID" prop="robotId">
          <el-input v-model="queryParams.robotId" placeholder="请输入机器人ID" clearable @keyup.enter.native="handleQuery" style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 原有：操作按钮区域
    <el-row :gutter="15" class="operate-bar mb10">
      <el-col :span="24">
        <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" class="right-toolbar" />
      </el-col>
    </el-row> -->

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>交互历史</span>
        </div>
      </template>

      <el-table v-loading="loading" :data="intHistoryList" border style="width: 100%">
        <el-table-column label="机器人ID" align="center" prop="robotId" width="120" />
        <el-table-column label="来源类型" align="center" prop="sourceType" width="100">
          <template slot-scope="scope">
            <el-tag :type="getSourceTypeTagType(scope.row.sourceType)" size="mini">
              {{ getSourceTypeName(scope.row.sourceType) }}
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

      <pagination
        v-show="total>0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>
  </div>
</template>

<script>
import { listIntHistory, getSumOfIntHistory } from "@/api/taskmgt/intHistory"

export default {
  name: "IntHistory",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      intHistoryList: [],
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
        pageSize: 10,
        robotId: null
      },
      sourceTypeMap: { 1: "任务", 2: "问答" },
      sourceTypeTagMap: { 1: "primary", 2: "success" },
      ratingMap: { 0: "差", 1: "中等", 2: "良好" },
      statusMap: { 0: "成功", 1: "失败", 2: "超时" },
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
    getSourceTypeName(type) {
      return this.sourceTypeMap[Number(type)] || "未知"
    },
    getSourceTypeTagType(type) {
      return this.sourceTypeTagMap[Number(type)] || "default"
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
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
      // 新增：查询表格数据时同步刷新统计数据
      this.getStatsData()
    },
    resetQuery() {
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        robotId: null
      }
      this.resetForm("queryForm")
      this.getList()
      // 新增：重置查询时同步刷新统计数据
      this.getStatsData()
    }
  }
}
</script>

<style scoped>
.stats-cards { margin-bottom: 20px; }
.search-card { margin-bottom: 20px; }
.table-card { margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }

.stats-card {
  padding: 10px 0;
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
