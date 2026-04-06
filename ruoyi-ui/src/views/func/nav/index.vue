<template>
  <div class="card">
    <!-- 隐藏的文件上传输入框 -->
    <input
      type="file"
      ref="fileInput"
      style="display: none;"
      accept="image/*"
      @change="handleMapUpload"
    >

    <div class="card-header">
      <div class="card-title">
        <i class="fas fa-map-signs"></i> 导航指引
      </div>
      <div class="header-actions">
        <el-button type="success" size="small" @click="showAddPointDialog = true">
          <i class="fas fa-plus"></i> 添加点位
        </el-button>
        <el-button type="primary" size="small" @click="triggerFileUpload">
          <i class="fas fa-upload"></i> 上传新地图
        </el-button>
        <el-button
          type="danger"
          size="small"
          plain
          :disabled="!navConfig.mapId"
          @click="deleteCurrentMap"
        >
          <i class="fas fa-trash-alt"></i> 删除地图
        </el-button>
      </div>
    </div>

    <div class="card-body">
      <el-row :gutter="20">
        <!-- 左侧：地图和点位 -->
        <el-col :span="16">
          <div class="sub-card">
            <div class="form-row">
              <div class="form-group" style="flex: 1;">
                <label>当前地图</label>
                <el-select v-model="navConfig.mapId" placeholder="请选择地图" @change="changeMap">
                  <el-option v-for="map in mapList" :key="map.mapId" :label="map.mapName" :value="map.mapId">
                    <span>{{ map.mapName }}</span>
                    <span style="float: right; color: #8492a6;">{{ map.pointCount || 0 }}个点位</span>
                  </el-option>
                </el-select>
              </div>
            </div>

            <!-- 地图预览 -->
            <div class="map-preview" @click="triggerFileUpload">
              <img
                v-if="mapImageUrl"
                :src="mapImageUrl"
                class="map-image"
                alt="地图预览"
                @error="handleImageError"
              />
              <div v-else class="map-placeholder">
                <i class="fas fa-cloud-upload-alt"></i>
                <span>点击上传实际地图</span>
                <span class="upload-hint">支持 jpg、png 格式，不超过10MB</span>
              </div>
              <div v-if="uploadLoading" class="upload-loading">
                <i class="fas fa-spinner fa-spin"></i>
                <span>上传中...</span>
              </div>
            </div>

            <!-- 点位列表区域 -->
            <div class="point-section">
              <div class="point-header">
                <div class="point-header-left">
                  <i class="fas fa-map-pin"></i>
                  <strong>点位列表</strong>
                  <span class="point-count">(共 {{ pointList.length }} 个点位)</span>
                </div>
                <el-button type="text" size="small" @click="showAddPointDialog = true" class="add-point-btn">
                  <i class="fas fa-plus-circle"></i> 添加点位
                </el-button>
              </div>
              <div class="point-list-container">
                <div
                  v-for="point in pointList"
                  :key="point.pointId"
                  class="point-item"
                  :class="{ 'point-item-active': selectedPoint === point.pointName }"
                  @click="selectPoint(point)"
                >
                  <div class="point-icon">
                    <i :class="getPointIcon(point.pointType)"></i>
                  </div>
                  <div class="point-info">
                    <div class="point-name">{{ point.pointName }}</div>
                    <div class="point-code" v-if="point.pointCode">{{ point.pointCode }}</div>
                  </div>
                  <div class="point-status" v-if="point.status === '0'">
                    <el-tag type="info" size="mini" effect="plain">禁用</el-tag>
                  </div>
                  <div class="point-actions">
                    <el-button type="text" size="small" @click.stop="editPoint(point)">
                      <i class="fas fa-edit"></i>
                    </el-button>
                    <el-button type="text" size="small" @click.stop="deletePoint(point)">
                      <i class="fas fa-trash-alt"></i>
                    </el-button>
                  </div>
                  <div class="point-nav-icon" v-if="selectedPoint === point.pointName">
                    <i class="fas fa-arrow-right"></i>
                  </div>
                </div>
                <div v-if="pointList.length === 0" class="empty-points">
                  <i class="fas fa-map-marked-alt"></i>
                  <span>暂无点位，请点击上方"添加点位"按钮添加</span>
                </div>
              </div>
            </div>
          </div>
        </el-col>

        <!-- 右侧：导航设置 -->
        <el-col :span="8">
          <div class="sub-card">
            <div class="form-group">
              <label>播报方式</label>
              <el-radio-group v-model="navConfig.voiceType">
                <el-radio label="default">默认播报语</el-radio>
                <el-radio label="custom">自定义播报</el-radio>
                <el-radio label="none">无播报</el-radio>
              </el-radio-group>
            </div>

            <div v-if="navConfig.voiceType === 'custom'" class="custom-voice">
              <div class="form-group">
                <label>出发前播报</label>
                <el-input v-model="navConfig.beforeMsg" placeholder="现在带您去社保窗口" />
              </div>
              <div class="form-group">
                <label>导航中播报</label>
                <el-input v-model="navConfig.duringMsg" placeholder="请跟随我" />
              </div>
              <div class="form-group">
                <label>到达后播报</label>
                <el-input v-model="navConfig.afterMsg" placeholder="已到达" />
              </div>
            </div>

            <el-divider></el-divider>

            <!-- 当前导航任务 -->
            <div class="current-task">
              <i class="fas fa-flag-checkered" style="color: #409eff;"></i>
              <span><strong>当前导航任务：</strong> {{ currentTask || '无' }}</span>
              <el-button
                size="small"
                type="danger"
                plain
                :disabled="!currentTask"
                @click="emergencyStop"
                style="margin-left: auto;"
              >
                <i class="fas fa-stop-circle"></i> 急停
              </el-button>
            </div>

            <el-button type="primary" style="width: 100%; margin-top: 20px;" @click="saveNavConfig">
              <i class="fas fa-save"></i> 保存导航设置
            </el-button>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 添加/编辑点位对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="showAddPointDialog"
      width="500px"
      @close="resetPointForm"
    >
      <el-form :model="pointForm" :rules="pointRules" ref="pointForm" label-width="100px">
        <el-form-item label="点位名称" prop="pointName">
          <el-input v-model="pointForm.pointName" placeholder="请输入点位名称" />
        </el-form-item>
        <el-form-item label="点位编码" prop="pointCode">
          <el-input v-model="pointForm.pointCode" placeholder="请输入点位编码" />
        </el-form-item>
        <el-form-item label="点位类型" prop="pointType">
          <el-select v-model="pointForm.pointType" placeholder="请选择点位类型" style="width: 100%">
            <el-option label="普通点位" value="normal">
              <i class="fas fa-location-dot"></i> 普通点位
            </el-option>
            <el-option label="VIP点位" value="vip">
              <i class="fas fa-crown"></i> VIP点位
            </el-option>
            <el-option label="服务点位" value="service">
              <i class="fas fa-concierge-bell"></i> 服务点位
            </el-option>
            <el-option label="出口点位" value="exit">
              <i class="fas fa-door-open"></i> 出口点位
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="X坐标" prop="coordinateX">
          <el-input-number v-model="pointForm.coordinateX" :precision="2" :step="0.1" placeholder="请输入X坐标" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Y坐标" prop="coordinateY">
          <el-input-number v-model="pointForm.coordinateY" :precision="2" :step="0.1" placeholder="请输入Y坐标" style="width: 100%" />
        </el-form-item>
        <el-form-item label="显示顺序" prop="orderNum">
          <el-input-number v-model="pointForm.orderNum" :min="0" :max="999" placeholder="请输入显示顺序" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="pointForm.status">
            <el-radio label="1">启用</el-radio>
            <el-radio label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="pointForm.remark" type="textarea" :rows="2" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showAddPointDialog = false">取消</el-button>
        <el-button type="primary" @click="submitPointForm" :loading="pointSubmitting">确定</el-button>
      </span>
    </el-dialog>

    <!-- 地图名称编辑对话框 -->
    <el-dialog title="编辑地图名称" :visible.sync="showMapNameDialog" width="400px">
      <el-form :model="mapForm" label-width="80px">
        <el-form-item label="地图名称">
          <el-input v-model="mapForm.mapName" placeholder="请输入地图名称" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showMapNameDialog = false">取消</el-button>
        <el-button type="primary" @click="saveMapName">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getNavConfig, saveNavConfig, startNavigation, emergencyStop as stopNavigation } from "@/api/func/nav";
import { getMapList, getPointListByMap, uploadMap, delMap, updateMap, getMapImage } from "@/api/func/map";
import { addPoint, updatePoint, deletePoint } from "@/api/func/point";

export default {
  name: "Nav",
  data() {
    return {
      navConfig: {
        mapId: null,
        voiceType: 'default',
        beforeMsg: '',
        duringMsg: '',
        afterMsg: ''
      },
      mapList: [],
      pointList: [],
      selectedPoint: '',
      currentTask: '',
      mapImageUrl: '',
      uploadLoading: false,
      showAddPointDialog: false,
      showMapNameDialog: false,
      mapForm: {
        mapId: null,
        mapName: ''
      },
      pointForm: {
        pointId: null,
        pointName: '',
        pointCode: '',
        pointType: 'normal',
        coordinateX: null,
        coordinateY: null,
        orderNum: 0,
        status: '1',
        remark: ''
      },
      pointSubmitting: false,
      pointRules: {
        pointName: [
          { required: true, message: '请输入点位名称', trigger: 'blur' },
          { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
        ],
        pointType: [
          { required: true, message: '请选择点位类型', trigger: 'change' }
        ]
      },
      // 存储blob URL，用于释放内存
      currentImageUrl: null
    };
  },
  computed: {
    dialogTitle() {
      return this.pointForm.pointId ? '编辑点位' : '添加点位';
    }
  },
  beforeDestroy() {
    // 组件销毁前释放blob URL
    if (this.currentImageUrl) {
      URL.revokeObjectURL(this.currentImageUrl);
    }
  },
  created() {
    this.loadNavConfig();
    this.loadMaps();
  },
  watch: {
    'navConfig.mapId': function(newVal, oldVal) {
      if (newVal && newVal !== oldVal) {
        this.loadPoints();
        this.loadMapImage();
        if (this.navConfig.mapId) {
          this.saveNavConfig();
        }
      }
    }
  },
  methods: {
    getPointIcon(pointType) {
      const icons = {
        'normal': 'fas fa-location-dot',
        'vip': 'fas fa-crown',
        'service': 'fas fa-concierge-bell',
        'exit': 'fas fa-door-open',
        'default': 'fas fa-map-pin'
      };
      return icons[pointType] || icons.default;
    },

    triggerFileUpload() {
      console.log('触发文件上传');
      if (this.$refs.fileInput) {
        this.$refs.fileInput.click();
      } else {
        console.error('fileInput ref 不存在');
        const input = document.createElement('input');
        input.type = 'file';
        input.accept = 'image/*';
        input.style.display = 'none';
        input.onchange = this.handleMapUpload.bind(this);
        document.body.appendChild(input);
        input.click();
      }
    },

    loadNavConfig() {
      getNavConfig().then(response => {
        if (response.data) {
          this.navConfig = response.data;
          if (this.navConfig.mapId) {
            this.loadMapImage();
          }
        }
      }).catch(error => {
        console.error('加载导航配置失败:', error);
      });
    },

    loadMaps() {
      getMapList({ pageNum: 1, pageSize: 100 }).then(response => {
        let maps = [];
        if (response.rows) {
          maps = response.rows;
        } else if (response.data) {
          maps = Array.isArray(response.data) ? response.data : [];
        } else if (Array.isArray(response)) {
          maps = response;
        }

        this.mapList = maps;
        console.log('加载地图列表:', this.mapList);

        if (this.mapList.length > 0) {
          if (!this.navConfig.mapId && this.mapList.length > 0) {
            this.navConfig.mapId = this.mapList[0].mapId;
            this.loadPoints();
            this.loadMapImage();
            this.saveNavConfig();
          } else if (this.navConfig.mapId) {
            const exists = this.mapList.some(m => m.mapId === this.navConfig.mapId);
            if (!exists && this.mapList.length > 0) {
              this.navConfig.mapId = this.mapList[0].mapId;
              this.saveNavConfig();
            }
            this.loadPoints();
            this.loadMapImage();
          }
        } else {
          this.pointList = [];
          this.mapImageUrl = '';
        }
      }).catch(error => {
        console.error('加载地图列表失败:', error);
        this.$message.error('加载地图列表失败');
      });
    },

    changeMap() {
      this.loadPoints();
      this.loadMapImage();
      this.saveNavConfig();
    },

    loadPoints() {
      if (!this.navConfig.mapId) {
        this.pointList = [];
        return;
      }
      getPointListByMap(this.navConfig.mapId).then(response => {
        const points = response.data || response.rows || [];
        this.pointList = points;
        console.log('加载点位列表:', this.pointList);

        const currentMap = this.mapList.find(m => m.mapId === this.navConfig.mapId);
        if (currentMap) {
          currentMap.pointCount = points.length;
        }
      }).catch(error => {
        console.error('加载点位列表失败:', error);
        this.pointList = [];
      });
    },

    loadMapImage() {
      if (!this.navConfig.mapId) {
        this.mapImageUrl = '';
        return;
      }

      // 释放之前的blob URL
      if (this.currentImageUrl) {
        URL.revokeObjectURL(this.currentImageUrl);
        this.currentImageUrl = null;
      }

      // 通过接口获取图片
      getMapImage(this.navConfig.mapId).then(response => {
        // 将blob转换为对象URL
        const url = URL.createObjectURL(response);
        this.mapImageUrl = url;
        this.currentImageUrl = url;
        console.log('地图图片加载成功');
      }).catch(error => {
        console.error('加载地图图片失败:', error);
        this.mapImageUrl = '';
        this.$message.warning('地图图片加载失败，请重新上传');
      });
    },

    handleMapUpload(event) {
      console.log('文件上传事件触发', event);
      const file = event.target.files[0];
      if (!file) {
        console.log('没有选择文件');
        return;
      }

      console.log('选择的文件:', file.name, file.size);

      if (!file.type.startsWith('image/')) {
        this.$message.error('请上传图片文件（jpg、png、jpeg等格式）');
        event.target.value = '';
        return;
      }

      if (file.size > 10 * 1024 * 1024) {
        this.$message.error('文件大小不能超过10MB');
        event.target.value = '';
        return;
      }

      this.uploadLoading = true;

      const formData = new FormData();
      formData.append('file', file);

      if (this.navConfig.mapId) {
        formData.append('mapId', this.navConfig.mapId);
      }

      uploadMap(formData).then(response => {
        this.uploadLoading = false;

        console.log('上传响应:', response);

        let mapData = null;
        if (response.data) {
          mapData = response.data;
        } else if (response.code === 200 && response.data) {
          mapData = response.data;
        }

        if (mapData) {
          if (!this.navConfig.mapId || !this.mapList.some(m => m.mapId === mapData.mapId)) {
            this.mapList.unshift(mapData);
          } else {
            const index = this.mapList.findIndex(m => m.mapId === mapData.mapId);
            if (index !== -1) {
              this.mapList[index] = mapData;
            }
          }

          this.navConfig.mapId = mapData.mapId;

          // 重新加载图片（通过接口）
          this.loadMapImage();

          this.mapForm.mapId = mapData.mapId;
          this.mapForm.mapName = mapData.mapName;
          this.showMapNameDialog = true;

          this.loadPoints();
          this.saveNavConfig();
          this.$message.success('地图上传成功');
        } else {
          this.$message.error('上传失败：返回数据格式错误');
        }
      }).catch(error => {
        this.uploadLoading = false;
        console.error('地图上传失败:', error);
        this.$message.error('地图上传失败：' + (error.response?.data?.msg || error.message || '未知错误'));
      });

      event.target.value = '';
    },

    saveMapName() {
      if (!this.mapForm.mapName.trim()) {
        this.$message.warning('请输入地图名称');
        return;
      }

      updateMap({
        mapId: this.mapForm.mapId,
        mapName: this.mapForm.mapName
      }).then(() => {
        this.$message.success('地图名称保存成功');
        this.showMapNameDialog = false;
        const map = this.mapList.find(m => m.mapId === this.mapForm.mapId);
        if (map) {
          map.mapName = this.mapForm.mapName;
        }
        this.loadMaps();
      }).catch(error => {
        console.error('保存地图名称失败:', error);
        this.$message.error('保存地图名称失败');
      });
    },

    deleteCurrentMap() {
      if (!this.navConfig.mapId) {
        this.$message.warning('请先选择要删除的地图');
        return;
      }

      const currentMap = this.mapList.find(m => m.mapId === this.navConfig.mapId);
      if (!currentMap) return;

      this.$confirm(`确定要删除地图"${currentMap.mapName}"吗？删除后该地图下的所有点位也将被删除！`, '警告', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delMap(this.navConfig.mapId).then(() => {
          this.$message.success('地图删除成功');
          this.loadMaps();
          this.navConfig.mapId = null;
          this.pointList = [];
          this.mapImageUrl = '';
          this.selectedPoint = '';
          this.currentTask = '';
          this.saveNavConfig();
        }).catch(error => {
          console.error('删除地图失败:', error);
          this.$message.error('删除地图失败');
        });
      }).catch(() => {});
    },

    handleImageError() {
      console.warn('图片加载失败:', this.mapImageUrl);
      this.mapImageUrl = '';
    },

    selectPoint(point) {
      this.selectedPoint = point.pointName;
      this.currentTask = point.pointName;

      startNavigation(point.pointName).then(() => {
        this.$message.success(`开始导航：带我去 ${point.pointName}`);
      }).catch(error => {
        console.error('开始导航失败:', error);
        this.$message.error('开始导航失败');
      });
    },

    emergencyStop() {
      stopNavigation().then(() => {
        this.$message.warning(`已紧急停止：${this.currentTask}`);
        this.currentTask = '';
        this.selectedPoint = '';
      }).catch(error => {
        console.error('紧急停止失败:', error);
        this.$message.error('紧急停止失败');
      });
    },

    saveNavConfig() {
      saveNavConfig(this.navConfig).then(() => {
        console.log('导航配置已保存');
      }).catch(error => {
        console.error('保存导航配置失败:', error);
      });
    },

    editPoint(point) {
      this.pointForm = {
        pointId: point.pointId,
        pointName: point.pointName,
        pointCode: point.pointCode || '',
        pointType: point.pointType || 'normal',
        coordinateX: point.coordinateX,
        coordinateY: point.coordinateY,
        orderNum: point.orderNum || 0,
        status: point.status || '1',
        remark: point.remark || ''
      };
      this.showAddPointDialog = true;
    },

    deletePoint(point) {
      this.$confirm(`确定要删除点位"${point.pointName}"吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deletePoint(point.pointId).then(() => {
          this.$message.success('删除成功');
          this.loadPoints();
        }).catch(error => {
          console.error('删除点位失败:', error);
          this.$message.error('删除失败');
        });
      }).catch(() => {});
    },

    resetPointForm() {
      this.pointForm = {
        pointId: null,
        pointName: '',
        pointCode: '',
        pointType: 'normal',
        coordinateX: null,
        coordinateY: null,
        orderNum: 0,
        status: '1',
        remark: ''
      };
      if (this.$refs.pointForm) {
        this.$refs.pointForm.resetFields();
      }
    },

    submitPointForm() {
      this.$refs.pointForm.validate((valid) => {
        if (!valid) return;

        this.pointSubmitting = true;
        const formData = {
          ...this.pointForm,
          mapId: this.navConfig.mapId
        };

        const apiCall = formData.pointId ? updatePoint(formData) : addPoint(formData);

        apiCall.then(() => {
          this.$message.success(this.pointForm.pointId ? '更新成功' : '添加成功');
          this.showAddPointDialog = false;
          this.loadPoints();

          const currentMap = this.mapList.find(m => m.mapId === this.navConfig.mapId);
          if (currentMap) {
            currentMap.pointCount = this.pointList.length;
          }
        }).catch(error => {
          console.error('保存点位失败:', error);
          this.$message.error('保存失败');
        }).finally(() => {
          this.pointSubmitting = false;
        });
      });
    }
  }
};
</script>

<style scoped>
/* 样式保持不变，使用之前的样式 */
.card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;
  overflow: hidden;
  transition: box-shadow 0.3s ease;
}

.card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fafafa;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2f3d;
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-title i {
  font-size: 18px;
  color: #409eff;
}

.card-body {
  padding: 24px;
}

.sub-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e9ecef;
  transition: all 0.3s ease;
}

.sub-card:hover {
  border-color: #d0d7de;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 10px;
  font-weight: 500;
  color: #2c3e50;
  font-size: 14px;
}

.map-preview {
  background: #f8f9fa;
  height: 280px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px dashed #dee2e6;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 24px;
  overflow: hidden;
  position: relative;
}

.map-preview:hover {
  background: #f1f3f5;
  border-color: #409eff;
}

.map-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: #f5f5f5;
}

.map-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.map-placeholder i {
  font-size: 56px;
  color: #adb5bd;
  margin-bottom: 12px;
}

.map-placeholder span {
  font-size: 14px;
  color: #6c757d;
}

.upload-hint {
  font-size: 12px;
  color: #adb5bd;
  margin-top: 8px;
}

.upload-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  z-index: 10;
  backdrop-filter: blur(4px);
}

.upload-loading i {
  font-size: 36px;
  margin-bottom: 12px;
}

.point-section {
  margin-top: 8px;
}

.point-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.point-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.point-header-left i {
  color: #409eff;
  font-size: 16px;
}

.point-header-left strong {
  color: #2c3e50;
  font-size: 15px;
}

.point-count {
  color: #909399;
  font-size: 12px;
  font-weight: normal;
}

.add-point-btn {
  padding: 0;
  font-size: 12px;
}

.point-list-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 500px;
  overflow-y: auto;
  padding: 4px;
}

.point-list-container::-webkit-scrollbar {
  width: 6px;
}

.point-list-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.point-list-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.point-list-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.point-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #ffffff;
  border: 1px solid #e9ecef;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s ease;
  position: relative;
  overflow: hidden;
}

.point-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: #409eff;
  transform: scaleY(0);
  transition: transform 0.25s ease;
}

.point-item:hover {
  transform: translateX(4px);
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
  background: #f8fcff;
}

.point-item:hover::before {
  transform: scaleY(1);
}

.point-item-active {
  background: linear-gradient(135deg, #f0f7ff 0%, #e8f0fe 100%);
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.point-item-active::before {
  transform: scaleY(1);
}

.point-item-active .point-icon i {
  color: #409eff;
}

.point-item-active .point-name {
  color: #409eff;
  font-weight: 600;
}

.point-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  border-radius: 8px;
  margin-right: 12px;
  transition: all 0.25s ease;
}

.point-icon i {
  font-size: 18px;
  color: #6c757d;
  transition: color 0.25s ease;
}

.point-item:hover .point-icon {
  background: #e9ecef;
}

.point-info {
  flex: 1;
  min-width: 0;
}

.point-name {
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
  transition: color 0.25s ease;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.point-code {
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.point-status {
  margin-left: 8px;
}

.point-actions {
  display: flex;
  gap: 4px;
  margin-left: 8px;
  opacity: 0;
  transition: opacity 0.25s ease;
}

.point-item:hover .point-actions {
  opacity: 1;
}

.point-actions .el-button {
  padding: 4px 6px;
  color: #909399;
}

.point-actions .el-button:hover {
  color: #409eff;
}

.point-nav-icon {
  margin-left: 8px;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #409eff;
  border-radius: 50%;
  animation: slideIn 0.3s ease;
}

.point-nav-icon i {
  font-size: 12px;
  color: white;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.empty-points {
  text-align: center;
  padding: 48px 20px;
  color: #909399;
}

.empty-points i {
  font-size: 48px;
  margin-bottom: 12px;
  color: #dcdfe6;
}

.empty-points span {
  display: block;
  font-size: 14px;
}

.custom-voice {
  margin-top: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 3px solid #409eff;
}

.current-task {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px;
  background: #ecf5ff;
  border-radius: 8px;
  margin-top: 10px;
  border-left: 3px solid #409eff;
}

.mb-2 {
  margin-bottom: 10px;
}

@media (max-width: 768px) {
  .el-col {
    width: 100% !important;
    margin-bottom: 20px;
  }

  .form-row {
    flex-direction: column;
    gap: 0;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .point-item {
    padding: 10px 12px;
  }

  .point-icon {
    width: 32px;
    height: 32px;
  }

  .point-name {
    font-size: 13px;
  }

  .point-actions {
    opacity: 1;
  }
}
</style>
