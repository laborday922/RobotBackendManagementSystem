<template>
  <div class="card">
    <div class="card-header">
      <div>
        <div class="card-title">
          <i class="fas fa-comments"></i> 问答管理
        </div>
        <div class="card-desc">统一维护 Dify / OpenAI 问答配置，并为机器人分配唯一的问答入口。</div>
      </div>
      <div class="page-badge">
        <span class="badge"><i class="fas fa-robot"></i> 机器人问答管理台</span>
      </div>
    </div>

    <div class="card-body">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="问答配置" name="chat">
          <div class="sub-card">
            <div class="table-header">
              <span><i class="fas fa-sliders-h"></i> 问答配置列表</span>
              <div class="table-actions">
                <el-button type="primary" size="small" @click="handleAddChat">
                  <i class="fas fa-plus"></i> 新增问答
                </el-button>
              </div>
            </div>

            <el-form ref="chatQueryForm" :model="chatQueryParams" :inline="true" label-width="68px" class="query-form">
              <el-form-item label="名称" prop="chatName">
                <el-input
                  v-model="chatQueryParams.chatName"
                  placeholder="请输入问答名称"
                  clearable
                  @keyup.enter.native="getChatList"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="el-icon-search" size="mini" @click="handleChatQuery">搜索</el-button>
                <el-button icon="el-icon-refresh" size="mini" @click="resetChatQuery">重置</el-button>
              </el-form-item>
            </el-form>

            <div class="table-wrapper">
              <el-table v-loading="chatLoading" :data="chatList" border>
                <el-table-column label="ID" align="center" prop="id" width="80" />
                <el-table-column label="问答名称" align="center" prop="chatName" min-width="160" show-overflow-tooltip />
                <el-table-column label="问答描述" align="center" prop="chatDesc" min-width="220" show-overflow-tooltip />
                <el-table-column label="对话类型" align="center" prop="chatType" width="90">
                  <template slot-scope="scope">
                    <el-tag :type="scope.row.chatType === 'dify' ? '' : 'success'" size="small">
                      {{ scope.row.chatType === 'dify' ? 'Dify' : 'OpenAI' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="API Key" align="center" min-width="220" show-overflow-tooltip>
                  <template slot-scope="scope">
                    <span>{{ scope.row.apiKeyMasked || keyStatusText(scope.row.hasApiKey) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="更新时间" align="center" prop="updateTime" width="180">
                  <template slot-scope="scope">
                    <span>{{ parseTime(scope.row.updateTime) || '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
                  <template slot-scope="scope">
                    <div class="table-actions-buttons">
                      <el-button size="mini" type="text" @click="handleEditChat(scope.row)">
                        <i class="fas fa-edit"></i> 修改
                      </el-button>
                      <el-button size="mini" type="text" class="danger" @click="handleDeleteChat(scope.row)">
                        <i class="fas fa-trash"></i> 删除
                      </el-button>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <div class="pagination-wrap">
              <pagination
                v-show="chatTotal > 0"
                :total="chatTotal"
                :page.sync="chatQueryParams.pageNum"
                :limit.sync="chatQueryParams.pageSize"
                @pagination="getChatList"
              />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="机器人绑定" name="rel">
          <div class="sub-card">
            <div class="table-header">
              <span><i class="fas fa-link"></i> 机器人绑定列表</span>
              <div class="table-actions">
                <el-button type="primary" size="small" @click="handleAddRel">
                  <i class="fas fa-plus"></i> 新增绑定
                </el-button>
              </div>
            </div>

            <el-form ref="relQueryForm" :model="relQueryParams" :inline="true" label-width="68px" class="query-form">
              <el-form-item label="机器人" prop="robotName">
                <el-input
                  v-model="relQueryParams.robotName"
                  placeholder="请输入机器人名称"
                  clearable
                  @keyup.enter.native="getRelList"
                />
              </el-form-item>
              <el-form-item label="问答" prop="chatId">
                <el-select v-model="relQueryParams.chatId" placeholder="请选择问答" clearable filterable>
                  <el-option
                    v-for="item in chatOptions"
                    :key="item.id"
                    :label="item.chatName"
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="el-icon-search" size="mini" @click="handleRelQuery">搜索</el-button>
                <el-button icon="el-icon-refresh" size="mini" @click="resetRelQuery">重置</el-button>
              </el-form-item>
            </el-form>

            <div class="table-wrapper">
              <el-table v-loading="relLoading" :data="relList" border>
                <el-table-column label="机器人ID" align="center" prop="robotId" width="100" />
                <el-table-column label="机器人编号" align="center" prop="robotCode" min-width="140" show-overflow-tooltip />
                <el-table-column label="机器人名称" align="center" prop="robotName" min-width="160" show-overflow-tooltip />
                <el-table-column label="已绑定问答" align="center" prop="chatName" min-width="180" show-overflow-tooltip />
                <el-table-column label="更新时间" align="center" prop="updateTime" width="180">
                  <template slot-scope="scope">
                    <span>{{ parseTime(scope.row.updateTime) || '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
                  <template slot-scope="scope">
                    <div class="table-actions-buttons">
                      <el-button size="mini" type="text" @click="handleEditRel(scope.row)">
                        <i class="fas fa-edit"></i> 修改
                      </el-button>
                      <el-button size="mini" type="text" class="danger" @click="handleDeleteRel(scope.row)">
                        <i class="fas fa-trash"></i> 删除
                      </el-button>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <div class="pagination-wrap">
              <pagination
                v-show="relTotal > 0"
                :total="relTotal"
                :page.sync="relQueryParams.pageNum"
                :limit.sync="relQueryParams.pageSize"
                @pagination="getRelList"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog :title="chatDialogTitle" :visible.sync="chatDialogOpen" width="650px" append-to-body class="global-dialog">
      <el-form ref="chatForm" :model="chatForm" :rules="chatRules" label-width="110px">
        <el-form-item label="问答名称" prop="chatName">
          <el-input v-model="chatForm.chatName" placeholder="请输入问答名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="问答描述" prop="chatDesc">
          <el-input v-model="chatForm.chatDesc" type="textarea" :rows="3" placeholder="请输入问答描述" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="对话类型" prop="chatType">
          <el-select v-model="chatForm.chatType" placeholder="请选择对话类型" style="width: 100%" @change="onChatTypeChange">
            <el-option label="Dify（工作流知识库）" value="dify" />
            <el-option label="OpenAI 体系（DeepSeek / 豆包等）" value="openai" />
          </el-select>
        </el-form-item>
        <el-form-item label="API Key" prop="apiKey">
          <el-input
            v-model="chatForm.apiKey"
            type="textarea"
            :rows="3"
            :placeholder="chatForm.id ? '留空则保持原 Key 不变；如需替换请直接输入新 Key' : '请输入 API Key'"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <template v-if="chatForm.chatType === 'openai'">
          <el-form-item label="接口地址" prop="baseUrl">
            <el-input v-model="chatForm.baseUrl" placeholder="例如 https://api.deepseek.com" maxlength="500" />
          </el-form-item>
          <el-form-item label="模型名称" prop="modelName">
            <el-input v-model="chatForm.modelName" placeholder="例如 deepseek-chat 或 deepseek-v4-pro" maxlength="100" />
          </el-form-item>
        </template>
        <div class="form-tip">
          <template v-if="chatForm.chatType === 'dify'">说明：Dify 模式下，翻译层和 conversationId 由系统自动管理。</template>
          <template v-else-if="chatForm.chatType === 'openai'">说明：OpenAI 体系模式下不经过翻译层，对话历史由服务端内存管理。</template>
          <span v-if="chatForm.id && chatForm.hasApiKey"> 当前后端已保存 Key，本次留空则保持不变。</span>
        </div>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitChatForm">确 定</el-button>
        <el-button @click="chatDialogOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="relDialogTitle" :visible.sync="relDialogOpen" width="520px" append-to-body class="global-dialog">
      <el-form ref="relForm" :model="relForm" :rules="relRules" label-width="110px">
        <el-form-item label="机器人" prop="robotId">
          <el-select v-model="relForm.robotId" placeholder="请选择机器人" filterable style="width: 100%">
            <el-option
              v-for="item in robotOptions"
              :key="item.id"
              :label="robotOptionLabel(item)"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="问答配置" prop="chatId">
          <el-select v-model="relForm.chatId" placeholder="请选择问答配置" filterable style="width: 100%">
            <el-option
              v-for="item in chatOptions"
              :key="item.id"
              :label="item.chatName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <div class="form-tip">规则：一台机器人只能绑定一个问答，重新保存会覆盖旧绑定。</div>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitRelForm">确 定</el-button>
        <el-button @click="relDialogOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listRobots } from '@/api/robots/robots'
import {
  addQaChat,
  addQaRobotChatRel,
  delQaChat,
  delQaRobotChatRel,
  getQaChat,
  getQaRobotChatRel,
  listQaChat,
  listQaChatOptions,
  listQaRobotChatRel,
  updateQaChat,
  updateQaRobotChatRel
} from '@/api/qa/chatManage'

export default {
  name: 'QaChatManage',
  data() {
    return {
      activeTab: 'chat',
      chatLoading: false,
      relLoading: false,
      chatTotal: 0,
      relTotal: 0,
      chatList: [],
      relList: [],
      chatOptions: [],
      robotOptions: [],
      chatDialogOpen: false,
      relDialogOpen: false,
      chatDialogTitle: '',
      relDialogTitle: '',
      chatForm: {
        id: null,
        chatName: '',
        chatDesc: '',
        chatType: 'dify',
        apiKey: '',
        baseUrl: '',
        modelName: '',
        hasApiKey: false
      },
      relForm: {
        robotId: null,
        chatId: null
      },
      chatQueryParams: {
        pageNum: 1,
        pageSize: 10,
        chatName: undefined
      },
      relQueryParams: {
        pageNum: 1,
        pageSize: 10,
        robotName: undefined,
        chatId: undefined
      },
      chatRules: {
        chatName: [{ required: true, message: '问答名称不能为空', trigger: 'blur' }],
        chatType: [{ required: true, message: '对话类型不能为空', trigger: 'change' }]
      },
      relRules: {
        robotId: [{ required: true, message: '机器人不能为空', trigger: 'change' }],
        chatId: [{ required: true, message: '问答配置不能为空', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getChatList()
    this.getRelList()
    this.loadChatOptions()
    this.loadRobotOptions()
  },
  methods: {
    getChatList() {
      this.chatLoading = true
      listQaChat(this.chatQueryParams).then(response => {
        this.chatList = response.rows || []
        this.chatTotal = response.total || 0
      }).finally(() => {
        this.chatLoading = false
      })
    },
    getRelList() {
      this.relLoading = true
      listQaRobotChatRel(this.relQueryParams).then(response => {
        this.relList = response.rows || []
        this.relTotal = response.total || 0
      }).finally(() => {
        this.relLoading = false
      })
    },
    loadChatOptions() {
      listQaChatOptions().then(response => {
        this.chatOptions = response.data || []
      })
    },
    loadRobotOptions() {
      listRobots({ pageNum: 1, pageSize: 1000 }).then(response => {
        this.robotOptions = response.rows || []
      })
    },
    handleChatQuery() {
      this.chatQueryParams.pageNum = 1
      this.getChatList()
    },
    resetChatQuery() {
      this.resetForm('chatQueryForm')
      this.handleChatQuery()
    },
    handleRelQuery() {
      this.relQueryParams.pageNum = 1
      this.getRelList()
    },
    resetRelQuery() {
      this.resetForm('relQueryForm')
      this.handleRelQuery()
    },
    handleAddChat() {
      this.chatDialogTitle = '新增问答配置'
      this.chatDialogOpen = true
      this.chatForm = {
        id: null,
        chatName: '',
        chatDesc: '',
        chatType: 'dify',
        apiKey: '',
        baseUrl: '',
        modelName: '',
        hasApiKey: false
      }
      this.$nextTick(() => this.resetForm('chatForm'))
    },
    handleEditChat(row) {
      getQaChat(row.id).then(response => {
        this.chatForm = Object.assign({
          id: null,
          chatName: '',
          chatDesc: '',
          chatType: 'dify',
          apiKey: '',
          baseUrl: '',
          modelName: '',
          hasApiKey: false
        }, response.data || {})
        this.chatDialogTitle = '修改问答配置'
        this.chatDialogOpen = true
        this.$nextTick(() => this.resetForm('chatForm'))
      })
    },
    onChatTypeChange(val) {
      if (val === 'dify') {
        this.chatForm.baseUrl = ''
        this.chatForm.modelName = ''
      }
    },
    submitChatForm() {
      this.$refs.chatForm.validate(valid => {
        if (!valid) {
          return
        }
        if (!this.chatForm.id && !this.chatForm.apiKey) {
          this.$message.error('新增时 API Key 不能为空')
          return
        }
        if (this.chatForm.chatType === 'openai') {
          if (!this.chatForm.baseUrl) {
            this.$message.error('OpenAI 类型必须填写接口地址')
            return
          }
          if (!this.chatForm.modelName) {
            this.$message.error('OpenAI 类型必须填写模型名称')
            return
          }
        }
        const request = this.chatForm.id ? updateQaChat(this.chatForm) : addQaChat(this.chatForm)
        request.then(() => {
          this.$modal.msgSuccess(this.chatForm.id ? '修改成功' : '新增成功')
          this.chatDialogOpen = false
          this.getChatList()
          this.loadChatOptions()
        })
      })
    },
    handleDeleteChat(row) {
      this.$modal.confirm(`确认删除问答【${row.chatName}】吗？`).then(() => {
        return delQaChat(row.id)
      }).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getChatList()
        this.loadChatOptions()
      })
    },
    handleAddRel() {
      this.relDialogTitle = '新增机器人绑定'
      this.relDialogOpen = true
      this.relForm = {
        robotId: null,
        chatId: null
      }
      this.$nextTick(() => this.resetForm('relForm'))
    },
    handleEditRel(row) {
      getQaRobotChatRel(row.robotId).then(response => {
        this.relForm = Object.assign({
          robotId: null,
          chatId: null
        }, response.data || {})
        this.relDialogTitle = '修改机器人绑定'
        this.relDialogOpen = true
        this.$nextTick(() => this.resetForm('relForm'))
      })
    },
    submitRelForm() {
      this.$refs.relForm.validate(valid => {
        if (!valid) {
          return
        }
        const existed = this.relList.find(item => item.robotId === this.relForm.robotId)
        const request = existed ? updateQaRobotChatRel(this.relForm) : addQaRobotChatRel(this.relForm)
        request.then(() => {
          this.$modal.msgSuccess(existed ? '绑定更新成功' : '绑定成功')
          this.relDialogOpen = false
          this.getRelList()
        })
      })
    },
    handleDeleteRel(row) {
      this.$modal.confirm(`确认解除机器人【${row.robotName || row.robotId}】的问答绑定吗？`).then(() => {
        return delQaRobotChatRel(row.robotId)
      }).then(() => {
        this.$modal.msgSuccess('解除绑定成功')
        this.getRelList()
      })
    },
    robotOptionLabel(item) {
      const name = item.name || ('机器人' + item.id)
      return item.code ? `${name}（${item.code}）` : name
    },
    keyStatusText(hasKey) {
      return hasKey ? '已配置' : '未配置'
    }
  }
}
</script>

<style scoped>
.card {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 10px 30px rgba(57, 118, 228, 0.08);
  overflow: hidden;
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid #eef2f8;
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
  color: var(--primary-blue, #3976E4);
}

.card-desc {
  margin-top: 8px;
  color: #7a869a;
  font-size: 13px;
  line-height: 1.6;
}

.card-body {
  padding: 24px 20px;
}

.page-badge {
  display: flex;
  align-items: center;
}

.badge {
  background: #e6f7ff;
  color: var(--primary-blue, #3976E4);
  padding: 8px 16px;
  border-radius: 30px;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
}

.sub-card {
  background: #ffffff;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid var(--border-light, #E5E7EB);
  margin-bottom: 20px;
}

.query-form {
  margin-bottom: 12px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 15px;
}

.table-header span {
  font-size: 16px;
  font-weight: 600;
  color: #1e2a3a;
}

.table-header span i {
  color: var(--primary-blue, #3976E4);
  margin-right: 8px;
}

.table-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.table-wrapper {
  width: 100%;
  overflow-x: auto;
}

.table-actions-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
  flex-wrap: nowrap;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.form-tip {
  margin-left: 110px;
  margin-top: -6px;
  color: #909399;
  font-size: 12px;
  line-height: 1.6;
}

.el-button.danger {
  color: #f56c6c;
}

.el-button.danger:hover {
  color: #f78989;
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .page-badge {
    width: 100%;
  }

  .badge {
    white-space: normal;
  }

  .table-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .table-actions-buttons {
    flex-wrap: wrap;
  }

  .form-tip {
    margin-left: 0;
  }
}
</style>
