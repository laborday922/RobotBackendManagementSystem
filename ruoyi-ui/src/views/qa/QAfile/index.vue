<template>
  <div class="card">
    <div class="card-header">
      <div>
        <div class="card-title">
          <i class="fas fa-database"></i> 知识库与文件管理
        </div>
        <div class="card-desc">统一管理知识库分组、Dify 配置、外部 API 配置，以及该知识库下的文件。</div>
      </div>
      <div class="page-badge">
        <span class="badge"><i class="fas fa-folder-open"></i> QA 知识库工作台</span>
      </div>
    </div>

    <div class="card-body">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="知识库管理" name="knowledgeBase">
          <div class="sub-card">
            <div class="table-header">
              <span><i class="fas fa-book"></i> 知识库列表</span>
              <div class="table-actions">
                <el-button type="primary" size="small" @click="handleAddKnowledgeBase">
                  <i class="fas fa-plus"></i> 新增知识库
                </el-button>
              </div>
            </div>

            <el-form ref="knowledgeBaseQueryForm" :model="knowledgeBaseQueryParams" :inline="true" label-width="68px" class="query-form">
              <el-form-item label="名称" prop="kbName">
                <el-input
                  v-model="knowledgeBaseQueryParams.kbName"
                  placeholder="请输入知识库名称"
                  clearable
                  @keyup.enter.native="handleKnowledgeBaseQuery"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="el-icon-search" size="mini" @click="handleKnowledgeBaseQuery">搜索</el-button>
                <el-button icon="el-icon-refresh" size="mini" @click="resetKnowledgeBaseQuery">重置</el-button>
              </el-form-item>
            </el-form>

            <div class="table-wrapper">
              <el-table v-loading="knowledgeBaseLoading" :data="knowledgeBaseList" border>
                <el-table-column label="ID" align="center" prop="id" width="80" />
                <el-table-column label="知识库名称" align="center" prop="kbName" min-width="160" show-overflow-tooltip />
                <el-table-column label="描述" align="center" prop="kbDesc" min-width="220" show-overflow-tooltip>
                  <template slot-scope="scope">
                    <span>{{ scope.row.kbDesc || '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="Dify配置" align="center" min-width="220">
                  <template slot-scope="scope">
                    <div class="config-cell">
                      <el-tag size="mini" :type="scope.row.difyEnabled ? 'success' : 'info'">
                        {{ scope.row.difyEnabled ? '已启用' : '未启用' }}
                      </el-tag>
                      <div v-if="scope.row.difyEnabled" class="config-lines">
                        <div>Dataset ID：{{ scope.row.difyDatasetId || '-' }}</div>
                        <div>模式：{{ difyDocFormText(scope.row.difyDocForm) }}</div>
                        <div>索引：{{ scope.row.difyIndexingTechnique || '-' }}</div>
                        <div>分段：{{ formatSegmentation(scope.row) }}</div>
                        <div>API Key：{{ scope.row.difyDatasetApiKeyMasked || secretStatusText(scope.row.hasDifyDatasetApiKey) }}</div>
                      </div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="API配置" align="center" min-width="240">
                  <template slot-scope="scope">
                    <div class="config-cell">
                      <el-tag size="mini" :type="scope.row.apiEnabled ? 'warning' : 'info'">
                        {{ scope.row.apiEnabled ? '已启用' : '未启用' }}
                      </el-tag>
                      <div v-if="scope.row.apiEnabled" class="config-lines">
                        <div>地址：{{ scope.row.apiBaseUrl || '-' }}</div>
                        <div>Token：{{ scope.row.apiAuthTokenMasked || secretStatusText(scope.row.hasApiAuthToken) }}</div>
                      </div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="下属文件数" align="center" prop="fileCount" width="110" />
                <el-table-column label="更新时间" align="center" prop="updateTime" width="180">
                  <template slot-scope="scope">
                    <span>{{ parseTime(scope.row.updateTime) || '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
                  <template slot-scope="scope">
                    <div class="table-actions-buttons">
                      <el-button size="mini" type="text" @click="handleEditKnowledgeBase(scope.row)">
                        <i class="fas fa-edit"></i> 修改
                      </el-button>
                      <el-button size="mini" type="text" class="danger" @click="handleDeleteKnowledgeBase(scope.row)">
                        <i class="fas fa-trash"></i> 删除
                      </el-button>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <div class="pagination-wrap">
              <pagination
                v-show="knowledgeBaseTotal > 0"
                :total="knowledgeBaseTotal"
                :page.sync="knowledgeBaseQueryParams.pageNum"
                :limit.sync="knowledgeBaseQueryParams.pageSize"
                @pagination="getKnowledgeBaseList"
              />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="文件管理" name="file">
          <div class="sub-card">
            <div class="table-header">
              <span><i class="fas fa-file-alt"></i> 文件列表</span>
              <div class="table-actions">
                <el-button type="primary" size="small" @click="handleAddFile">
                  <i class="fas fa-plus"></i> 上传文件
                </el-button>
              </div>
            </div>

            <el-form ref="fileQueryForm" :model="queryParams" :inline="true" label-width="80px" class="query-form">
              <el-form-item label="知识库" prop="knowledgeBaseId">
                <el-select v-model="queryParams.knowledgeBaseId" placeholder="请选择知识库" clearable filterable style="width: 180px">
                  <el-option
                    v-for="item in knowledgeBaseOptions"
                    :key="item.id"
                    :label="item.kbName"
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="文件名" prop="fileName">
                <el-input
                  v-model="queryParams.fileName"
                  placeholder="请输入文件名"
                  clearable
                  @keyup.enter.native="handleFileQuery"
                />
              </el-form-item>
              <el-form-item label="状态" prop="status">
                <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 160px">
                  <el-option label="正常" :value="0"></el-option>
                  <el-option label="上传失败" :value="1"></el-option>
                  <el-option label="Dify知识库上传失败" :value="2"></el-option>
                  <el-option label="API上传失败" :value="3"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="el-icon-search" size="mini" @click="handleFileQuery">搜索</el-button>
                <el-button icon="el-icon-refresh" size="mini" @click="resetFileQuery">重置</el-button>
              </el-form-item>
            </el-form>

            <div class="table-wrapper">
              <el-table v-loading="fileLoading" :data="QAfileList" border>
                <el-table-column label="ID" prop="id" width="80" align="center" />
                <el-table-column label="所属知识库" prop="knowledgeBaseName" min-width="160" show-overflow-tooltip>
                  <template slot-scope="scope">
                    <span>{{ scope.row.knowledgeBaseName || '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="文件名" prop="fileName" min-width="220" show-overflow-tooltip />
                <el-table-column label="文件大小(字节)" prop="fileSize" width="140" align="center" />
                <el-table-column label="文件类型" prop="fileType" width="120" align="center" />
                <el-table-column label="处理状态" prop="status" width="140" align="center">
                  <template slot-scope="scope">
                    <span>{{ statusLabel(scope.row.status) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
                  <template slot-scope="scope">
                    <div class="table-actions-buttons">
                      <el-button
                        size="mini"
                        type="text"
                        :disabled="scope.row.status === 0 || scope.row.status === null || scope.row.status === undefined"
                        @click="handleRetryProcess(scope.row)"
                      >
                        <i class="fas fa-redo-alt"></i> 重试处理
                      </el-button>
                      <el-button size="mini" type="text" class="danger" @click="handleDeleteFile(scope.row)">
                        <i class="fas fa-trash"></i> 删除
                      </el-button>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <div class="pagination-wrap">
              <pagination
                v-show="total > 0"
                :total="total"
                :page.sync="queryParams.pageNum"
                :limit.sync="queryParams.pageSize"
                @pagination="getFileList"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog :title="knowledgeBaseDialogTitle" :visible.sync="knowledgeBaseDialogOpen" width="680px" append-to-body class="global-dialog">
      <el-form ref="knowledgeBaseForm" :model="knowledgeBaseForm" :rules="knowledgeBaseRules" label-width="120px">
        <el-form-item label="知识库名称" prop="kbName">
          <el-input v-model="knowledgeBaseForm.kbName" placeholder="请输入知识库名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="知识库描述" prop="kbDesc">
          <el-input v-model="knowledgeBaseForm.kbDesc" type="textarea" :rows="3" placeholder="请输入知识库描述" maxlength="500" show-word-limit />
        </el-form-item>

        <el-divider content-position="left">Dify 配置</el-divider>
        <el-form-item label="启用 Dify">
          <el-switch v-model="knowledgeBaseForm.difyEnabled" :disabled="knowledgeBaseEditMode" @change="handleDifyToggle" />
        </el-form-item>
        <el-form-item label="Dataset ID">
          <el-input
            v-if="!knowledgeBaseEditMode"
            v-model="knowledgeBaseForm.difyDatasetId"
            :disabled="!knowledgeBaseForm.difyEnabled"
            placeholder="启用后必填"
          />
          <el-input
            v-else
            :value="knowledgeBaseForm.difyDatasetId || '-'"
            disabled
          />
        </el-form-item>
        <el-form-item label="Dataset API Key">
          <el-input
            v-if="!knowledgeBaseEditMode"
            v-model="knowledgeBaseForm.difyDatasetApiKey"
            :disabled="!knowledgeBaseForm.difyEnabled"
            placeholder="启用后必填"
            show-password
          />
          <el-input
            v-else
            :value="secretStatusText(knowledgeBaseForm.hasDifyDatasetApiKey)"
            disabled
          />
        </el-form-item>
        <el-form-item label="索引方式">
          <el-select
            v-if="!knowledgeBaseEditMode"
            v-model="knowledgeBaseForm.difyIndexingTechnique"
            :disabled="!knowledgeBaseForm.difyEnabled"
            placeholder="请选择索引方式"
            style="width: 100%"
          >
            <el-option label="高质量 high_quality" value="high_quality" />
            <el-option label="经济 economy" value="economy" />
          </el-select>
          <el-input v-else :value="knowledgeBaseForm.difyIndexingTechnique || '-'" disabled />
        </el-form-item>
        <el-form-item label="文档模式">
          <el-select
            v-if="!knowledgeBaseEditMode"
            v-model="knowledgeBaseForm.difyDocForm"
            :disabled="!knowledgeBaseForm.difyEnabled"
            placeholder="请选择文档模式"
            style="width: 100%"
          >
            <el-option label="QA 模式 qa_model" value="qa_model" />
            <el-option label="文本模式 text_model" value="text_model" />
          </el-select>
          <el-input v-else :value="difyDocFormText(knowledgeBaseForm.difyDocForm)" disabled />
        </el-form-item>
        <el-form-item label="文档语言">
          <el-input
            v-if="!knowledgeBaseEditMode"
            v-model="knowledgeBaseForm.difyDocLanguage"
            :disabled="!knowledgeBaseForm.difyEnabled"
            placeholder="例如：Chinese Simplified"
          />
          <el-input v-else :value="knowledgeBaseForm.difyDocLanguage || '-'" disabled />
        </el-form-item>
        <el-form-item label="处理模式">
          <el-select
            v-if="!knowledgeBaseEditMode"
            v-model="knowledgeBaseForm.difyProcessRuleMode"
            :disabled="!knowledgeBaseForm.difyEnabled"
            placeholder="请选择处理模式"
            style="width: 100%"
          >
            <el-option label="自定义 custom" value="custom" />
            <el-option label="自动 automatic" value="automatic" />
          </el-select>
          <el-input v-else :value="knowledgeBaseForm.difyProcessRuleMode || '-'" disabled />
        </el-form-item>
        <el-form-item label="分段分隔符">
          <el-input
            v-if="!knowledgeBaseEditMode"
            v-model="knowledgeBaseForm.difyRuleSeparator"
            :disabled="!knowledgeBaseForm.difyEnabled"
            placeholder="例如：\n\n"
          />
          <el-input v-else :value="knowledgeBaseForm.difyRuleSeparator || '-'" disabled />
        </el-form-item>
        <el-form-item label="最大分段长度">
          <el-input-number
            v-if="!knowledgeBaseEditMode"
            v-model="knowledgeBaseForm.difyRuleMaxTokens"
            :disabled="!knowledgeBaseForm.difyEnabled"
            :min="1"
            :max="20000"
            controls-position="right"
            style="width: 100%"
          />
          <el-input v-else :value="knowledgeBaseForm.difyRuleMaxTokens" disabled />
        </el-form-item>
        <el-form-item label="分段重叠长度">
          <el-input-number
            v-if="!knowledgeBaseEditMode"
            v-model="knowledgeBaseForm.difyRuleChunkOverlap"
            :disabled="!knowledgeBaseForm.difyEnabled"
            :min="0"
            :max="5000"
            controls-position="right"
            style="width: 100%"
          />
          <el-input v-else :value="knowledgeBaseForm.difyRuleChunkOverlap" disabled />
        </el-form-item>
        <el-form-item label="预处理规则">
          <el-checkbox
            v-if="!knowledgeBaseEditMode"
            v-model="knowledgeBaseForm.difyRemoveExtraSpaces"
            :disabled="!knowledgeBaseForm.difyEnabled"
          >去除多余空格</el-checkbox>
          <el-checkbox
            v-if="!knowledgeBaseEditMode"
            v-model="knowledgeBaseForm.difyRemoveUrlsEmails"
            :disabled="!knowledgeBaseForm.difyEnabled"
          >去除 URL / 邮箱</el-checkbox>
          <el-input
            v-else
            :value="formatPreprocessing(knowledgeBaseForm)"
            disabled
          />
        </el-form-item>

        <el-alert
          v-if="knowledgeBaseForm.difyEnabled || knowledgeBaseEditMode"
          class="protocol-alert"
          title="Dify 参数会固化到该知识库"
          type="warning"
          :closable="false"
          show-icon
        >
          <div slot="default">
            Dify dataset 一旦已有文档，分段方式、文档模式、索引方式通常要与已有文档保持一致。
            建议在首次创建知识库时就确定好这组参数；当前默认值已按 QA 模式预填。
          </div>
        </el-alert>

        <el-divider content-position="left">外部 API 配置</el-divider>
        <el-form-item label="启用 API">
          <el-switch v-model="knowledgeBaseForm.apiEnabled" :disabled="knowledgeBaseEditMode" @change="handleApiToggle" />
        </el-form-item>
        <el-form-item label="API 地址">
          <el-input
            v-if="!knowledgeBaseEditMode"
            v-model="knowledgeBaseForm.apiBaseUrl"
            :disabled="!knowledgeBaseForm.apiEnabled"
            placeholder="例如：http://127.0.0.1:8000"
          />
          <el-input v-else :value="knowledgeBaseForm.apiBaseUrl || '-'" disabled />
        </el-form-item>
        <el-form-item label="API Token">
          <el-input
            v-if="!knowledgeBaseEditMode"
            v-model="knowledgeBaseForm.apiAuthToken"
            :disabled="!knowledgeBaseForm.apiEnabled"
            placeholder="可选"
            show-password
          />
          <el-input v-else :value="secretStatusText(knowledgeBaseForm.hasApiAuthToken)" disabled />
        </el-form-item>

        <el-alert
          v-if="knowledgeBaseForm.apiEnabled || knowledgeBaseEditMode"
          class="protocol-alert"
          title="外部 API 为固定协议"
          type="info"
          :closable="false"
          show-icon
        >
          <div slot="default">
            上传文件固定调用 <code>POST {API地址}/files/upsert</code>；删除文件固定调用 <code>DELETE {API地址}/files/{fileId}</code>。
            上传参数固定为 <code>fileId</code>、<code>fileName</code>、<code>content</code>、<code>metadata</code>，其中 <code>metadata</code> 包含文件类型、文件大小、所属知识库 ID 和名称。
          </div>
        </el-alert>

        <div class="form-tip">
          说明：知识库的 Dify / API 配置只允许在创建时设置；创建后只能修改知识库名称和描述。
        </div>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitKnowledgeBaseForm">确 定</el-button>
        <el-button @click="knowledgeBaseDialogOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="上传 QA 文件" :visible.sync="fileDialogOpen" width="520px" append-to-body class="global-dialog">
      <el-form :model="fileUploadForm" label-width="90px">
        <el-form-item label="所属知识库" required>
          <el-select v-model="fileUploadForm.knowledgeBaseId" placeholder="请选择知识库" filterable style="width: 100%">
            <el-option
              v-for="item in knowledgeBaseOptions"
              :key="item.id"
              :label="item.kbName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择文件">
          <el-upload
            ref="uploader"
            name="file"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :data="{ knowledgeBaseId: fileUploadForm.knowledgeBaseId }"
            :auto-upload="true"
            :limit="1"
            :multiple="false"
            :show-file-list="true"
            accept=".txt,.doc,.docx,.pdf"
            :before-upload="beforeUpload"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :on-exceed="handleExceed"
          >
            <el-button type="primary">选择文件</el-button>
            <div slot="tip" class="el-upload__tip">支持 txt/doc/docx/pdf，选择文件后自动上传</div>
          </el-upload>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelUpload">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { delQAfile, listQAfile, retryProcess } from '@/api/qa/QAfile'
import {
  addQaKnowledgeBase,
  delQaKnowledgeBase,
  getQaKnowledgeBase,
  listQaKnowledgeBase,
  listQaKnowledgeBaseOptions,
  updateQaKnowledgeBase
} from '@/api/qa/knowledgeBase'
import { getToken } from '@/utils/auth'

function createKnowledgeBaseForm() {
  return {
    id: null,
    kbName: '',
    kbDesc: '',
    difyEnabled: false,
    difyDatasetId: '',
    difyDatasetApiKey: '',
      difyIndexingTechnique: 'high_quality',
      difyDocForm: 'qa_model',
      difyDocLanguage: 'Chinese Simplified',
      difyProcessRuleMode: 'custom',
      difyRuleSeparator: '\n\n',
      difyRuleMaxTokens: 500,
      difyRuleChunkOverlap: 50,
      difyRemoveExtraSpaces: true,
      difyRemoveUrlsEmails: false,
    hasDifyDatasetApiKey: false,
    apiEnabled: false,
    apiBaseUrl: '',
    apiAuthToken: '',
    hasApiAuthToken: false,
    fileCount: 0
  }
}

function createFileUploadForm() {
  return {
    knowledgeBaseId: null
  }
}

export default {
  name: 'QAfile',
  data() {
    return {
      activeTab: 'knowledgeBase',
      knowledgeBaseLoading: false,
      fileLoading: false,
      knowledgeBaseTotal: 0,
      total: 0,
      knowledgeBaseList: [],
      QAfileList: [],
      knowledgeBaseOptions: [],
      knowledgeBaseDialogOpen: false,
      knowledgeBaseDialogTitle: '',
      knowledgeBaseEditMode: false,
      fileDialogOpen: false,
      uploadUrl: process.env.VUE_APP_BASE_API + '/qa/QAfile/upload',
      uploadHeaders: {
        Authorization: 'Bearer ' + getToken()
      },
      knowledgeBaseForm: createKnowledgeBaseForm(),
      fileUploadForm: createFileUploadForm(),
      knowledgeBaseQueryParams: {
        pageNum: 1,
        pageSize: 10,
        kbName: undefined
      },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        knowledgeBaseId: undefined,
        fileName: undefined,
        status: undefined
      },
      knowledgeBaseRules: {
        kbName: [{ required: true, message: '知识库名称不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getKnowledgeBaseList()
    this.getFileList()
    this.loadKnowledgeBaseOptions()
  },
  methods: {
    getKnowledgeBaseList() {
      this.knowledgeBaseLoading = true
      listQaKnowledgeBase(this.knowledgeBaseQueryParams).then(response => {
        this.knowledgeBaseList = response.rows || []
        this.knowledgeBaseTotal = response.total || 0
      }).finally(() => {
        this.knowledgeBaseLoading = false
      })
    },
    getFileList() {
      this.fileLoading = true
      listQAfile(this.queryParams).then(response => {
        this.QAfileList = response.rows || []
        this.total = response.total || 0
      }).finally(() => {
        this.fileLoading = false
      })
    },
    loadKnowledgeBaseOptions() {
      listQaKnowledgeBaseOptions().then(response => {
        this.knowledgeBaseOptions = response.data || []
      })
    },
    handleKnowledgeBaseQuery() {
      this.knowledgeBaseQueryParams.pageNum = 1
      this.getKnowledgeBaseList()
    },
    resetKnowledgeBaseQuery() {
      this.resetForm('knowledgeBaseQueryForm')
      this.handleKnowledgeBaseQuery()
    },
    handleFileQuery() {
      this.queryParams.pageNum = 1
      this.getFileList()
    },
    resetFileQuery() {
      this.resetForm('fileQueryForm')
      this.handleFileQuery()
    },
    handleAddKnowledgeBase() {
      this.knowledgeBaseEditMode = false
      this.knowledgeBaseDialogTitle = '新增知识库'
      this.knowledgeBaseForm = createKnowledgeBaseForm()
      this.knowledgeBaseDialogOpen = true
      this.$nextTick(() => this.resetForm('knowledgeBaseForm'))
    },
    handleEditKnowledgeBase(row) {
      getQaKnowledgeBase(row.id).then(response => {
        this.knowledgeBaseEditMode = true
        this.knowledgeBaseDialogTitle = '修改知识库'
        this.knowledgeBaseForm = Object.assign(createKnowledgeBaseForm(), response.data || {})
        this.knowledgeBaseDialogOpen = true
        this.$nextTick(() => this.resetForm('knowledgeBaseForm'))
      })
    },
    submitKnowledgeBaseForm() {
      this.$refs.knowledgeBaseForm.validate(valid => {
        if (!valid) {
          return
        }
        if (!this.knowledgeBaseEditMode) {
          if (this.knowledgeBaseForm.difyEnabled) {
            if (!this.knowledgeBaseForm.difyDatasetId) {
              this.$message.error('启用 Dify 时，Dataset ID 不能为空')
              return
            }
            if (!this.knowledgeBaseForm.difyDatasetApiKey) {
              this.$message.error('启用 Dify 时，Dataset API Key 不能为空')
              return
            }
            if (!this.knowledgeBaseForm.difyIndexingTechnique || !this.knowledgeBaseForm.difyDocForm || !this.knowledgeBaseForm.difyDocLanguage || !this.knowledgeBaseForm.difyProcessRuleMode) {
              this.$message.error('启用 Dify 时，索引方式、文档模式、语言、处理模式不能为空')
              return
            }
          }
          if (this.knowledgeBaseForm.apiEnabled) {
            if (!this.knowledgeBaseForm.apiBaseUrl) {
              this.$message.error('启用 API 时，API 地址不能为空')
              return
            }
          }
        }
        const request = this.knowledgeBaseEditMode
          ? updateQaKnowledgeBase(this.knowledgeBaseForm)
          : addQaKnowledgeBase(this.knowledgeBaseForm)
        request.then(() => {
          this.$modal.msgSuccess(this.knowledgeBaseEditMode ? '修改成功' : '新增成功')
          this.knowledgeBaseDialogOpen = false
          this.getKnowledgeBaseList()
          this.loadKnowledgeBaseOptions()
        })
      })
    },
    handleDeleteKnowledgeBase(row) {
      const fileCount = row.fileCount || 0
      this.$modal.confirm(`确认删除知识库【${row.kbName}】吗？删除后不可撤回，并会连同下属 ${fileCount} 个文件一并删除。`)
        .then(() => delQaKnowledgeBase(row.id))
        .then(() => {
          this.$modal.msgSuccess('删除成功')
          this.getKnowledgeBaseList()
          this.getFileList()
          this.loadKnowledgeBaseOptions()
        })
    },
    handleDifyToggle(enabled) {
      if (!enabled && !this.knowledgeBaseEditMode) {
        this.knowledgeBaseForm.difyDatasetId = ''
        this.knowledgeBaseForm.difyDatasetApiKey = ''
        this.knowledgeBaseForm.difyIndexingTechnique = 'high_quality'
        this.knowledgeBaseForm.difyDocForm = 'qa_model'
        this.knowledgeBaseForm.difyDocLanguage = 'Chinese Simplified'
        this.knowledgeBaseForm.difyProcessRuleMode = 'custom'
        this.knowledgeBaseForm.difyRuleSeparator = '\n\n'
        this.knowledgeBaseForm.difyRuleMaxTokens = 500
        this.knowledgeBaseForm.difyRuleChunkOverlap = 50
        this.knowledgeBaseForm.difyRemoveExtraSpaces = true
        this.knowledgeBaseForm.difyRemoveUrlsEmails = false
      }
    },
    handleApiToggle(enabled) {
      if (!enabled && !this.knowledgeBaseEditMode) {
        this.knowledgeBaseForm.apiBaseUrl = ''
        this.knowledgeBaseForm.apiAuthToken = ''
      }
    },
    handleAddFile() {
      if (!this.knowledgeBaseOptions.length) {
        this.$message.warning('请先创建知识库，再上传文件')
        this.activeTab = 'knowledgeBase'
        return
      }
      this.fileUploadForm = createFileUploadForm()
      this.fileUploadForm.knowledgeBaseId = this.queryParams.knowledgeBaseId || null
      this.fileDialogOpen = true
      this.$nextTick(() => this.clearUpload())
    },
    cancelUpload() {
      this.fileDialogOpen = false
      this.clearUpload()
    },
    handleDeleteFile(row) {
      const id = row && row.id ? row.id : null
      if (!id) {
        this.$message.warning('请选择要删除的文件')
        return
      }
      this.$modal.confirm(`是否确认删除文件【${row.fileName || id}】？`)
        .then(() => delQAfile(id))
        .then(() => {
          this.$modal.msgSuccess('删除成功')
          this.getFileList()
          this.getKnowledgeBaseList()
        })
    },
    handleRetryProcess(row) {
      const id = row && row.id ? row.id : null
      if (!id) {
        return
      }
      this.$modal.confirm('是否确认重新处理该文件？')
        .then(() => retryProcess(id))
        .then(() => {
          this.$modal.msgSuccess('已触发重试')
          this.getFileList()
        })
    },
    beforeUpload(file) {
      if (!this.fileUploadForm.knowledgeBaseId) {
        this.$message.error('请先选择所属知识库')
        return false
      }
      const fileName = file && file.name ? file.name : ''
      const ext = fileName.includes('.') ? fileName.split('.').pop().toLowerCase() : ''
      const allowed = ['txt', 'doc', 'docx', 'pdf']
      if (!allowed.includes(ext)) {
        this.$message.error('仅支持上传 txt/doc/docx/pdf')
        return false
      }
      return true
    },
    handleUploadSuccess(response) {
      if (response && response.code === 200) {
        this.$modal.msgSuccess('上传成功')
        this.fileDialogOpen = false
        this.getFileList()
        this.getKnowledgeBaseList()
        this.clearUpload()
        return
      }
      const msg = response && response.msg ? response.msg : '上传失败'
      this.$message.error(msg)
    },
    handleUploadError() {
      this.$message.error('上传失败')
    },
    handleExceed() {
      this.$message.warning('一次只能上传一个文件')
    },
    clearUpload() {
      const uploader = this.$refs.uploader
      if (uploader && uploader.clearFiles) {
        uploader.clearFiles()
      }
    },
    statusLabel(status) {
      if (status === 0) return '正常'
      if (status === 1) return '上传失败'
      if (status === 2) return 'Dify知识库上传失败'
      if (status === 3) return 'API上传失败'
      return '-'
    },
    difyDocFormText(docForm) {
      if (docForm === 'qa_model') return 'QA 模式'
      if (docForm === 'text_model') return '文本模式'
      return docForm || '-'
    },
    formatSegmentation(row) {
      const separator = row && row.difyRuleSeparator !== undefined && row.difyRuleSeparator !== null ? row.difyRuleSeparator : '-'
      const maxTokens = row && row.difyRuleMaxTokens !== undefined && row.difyRuleMaxTokens !== null ? row.difyRuleMaxTokens : '-'
      const overlap = row && row.difyRuleChunkOverlap !== undefined && row.difyRuleChunkOverlap !== null ? row.difyRuleChunkOverlap : '-'
      return `${separator} / ${maxTokens} / ${overlap}`
    },
    formatPreprocessing(row) {
      const values = []
      if (row && row.difyRemoveExtraSpaces) {
        values.push('去除多余空格')
      }
      if (row && row.difyRemoveUrlsEmails) {
        values.push('去除 URL / 邮箱')
      }
      return values.length ? values.join('，') : '未启用'
    },
    secretStatusText(hasValue) {
      return hasValue ? '已配置' : '未配置'
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

.config-cell {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}

.config-lines {
  text-align: left;
  color: #606266;
  line-height: 1.6;
}

.form-tip {
  margin-left: 120px;
  margin-top: -6px;
  color: #909399;
  font-size: 12px;
  line-height: 1.6;
}

.protocol-alert {
  margin: 0 0 16px 120px;
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

  .protocol-alert {
    margin-left: 0;
  }
}
</style>
