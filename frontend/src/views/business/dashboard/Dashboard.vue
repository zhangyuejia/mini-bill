<template>
  <div class="page-container">
    <div class="welcome-section">
      <div class="welcome-content">
        <div class="welcome-text">
          <h2>欢迎回来，{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</h2>
          <p v-if="userStore.isAdmin">您拥有系统管理员权限，可在左侧菜单进行系统管理操作</p>
          <p v-else-if="userStore.currentFamily">当前家庭：<strong>{{ userStore.currentFamily.name }}</strong></p>
          <p v-else class="no-family">暂未加入任何家庭，<a @click="goToFamily">创建或加入家庭</a></p>
        </div>
        <div class="welcome-avatar">
          <el-avatar :size="72" class="big-avatar">{{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}</el-avatar>
        </div>
      </div>
    </div>

    <template v-if="userStore.isAdmin">
      <div class="stat-grid">
        <div class="stat-card admin-card">
          <div class="stat-card-icon"><el-icon :size="28"><User /></el-icon></div>
          <div class="stat-card-body">
            <span class="stat-label">系统管理</span>
            <span class="stat-value">管理员</span>
          </div>
          <div class="stat-card-glow"></div>
        </div>
      </div>
      <div class="action-section">
        <h4 class="action-section-title">系统管理</h4>
        <div class="action-grid">
          <router-link to="/user" class="action-card">
            <div class="action-icon" style="background: linear-gradient(135deg, #409eff, #337ecc);"><el-icon :size="22"><User /></el-icon></div>
            <span>用户管理</span>
          </router-link>
          <router-link to="/role" class="action-card">
            <div class="action-icon" style="background: linear-gradient(135deg, #67c23a, #529b2e);"><el-icon :size="22"><Avatar /></el-icon></div>
            <span>角色管理</span>
          </router-link>
          <router-link to="/menu" class="action-card">
            <div class="action-icon" style="background: linear-gradient(135deg, #e6a23c, #d4880f);"><el-icon :size="22"><Menu /></el-icon></div>
            <span>菜单管理</span>
          </router-link>
          <router-link to="/dict" class="action-card">
            <div class="action-icon" style="background: linear-gradient(135deg, #909399, #606266);"><el-icon :size="22"><Collection /></el-icon></div>
            <span>字典管理</span>
          </router-link>
        </div>
      </div>
    </template>

    <template v-else>
      <!-- 摘要统计卡片 -->
      <div class="summary-grid" v-if="summary">
        <div class="sum-card saving">
          <div class="sum-icon"><el-icon :size="22"><Wallet /></el-icon></div>
          <div class="sum-body">
            <span class="sum-label">家庭储蓄</span>
            <span class="sum-value">¥{{ (Number(summary.savingTotal) || 0).toLocaleString() }}</span>
            <span v-if="summary.savingDaysAgoText" class="sum-sub">上次更新：{{ summary.savingDaysAgoText }}前</span>
            <span v-else class="sum-sub">暂无记录</span>
          </div>
        </div>
        <div class="sum-card expense">
          <div class="sum-icon"><el-icon :size="22"><Ticket /></el-icon></div>
          <div class="sum-body">
            <span class="sum-label">家庭维护费用</span>
            <span class="sum-value">¥{{ (Number(summary.totalHistory) || 0).toLocaleString() }}</span>
            <span class="sum-sub">今年：¥{{ (Number(summary.totalThisYear) || 0).toLocaleString() }}，年均：¥{{ (Number(summary.annualAverage) || 0).toLocaleString() }}</span>
          </div>
        </div>
        <div class="sum-card item">
          <div class="sum-icon"><el-icon :size="22"><Goods /></el-icon></div>
          <div class="sum-body">
            <span class="sum-label">家庭物件（{{ summary.itemCount || 0 }}件）</span>
            <span v-if="summary.mostExpensiveItem" class="sum-sub">最贵日均：{{ summary.mostExpensiveItem.name }} ¥{{ Number(summary.mostExpensiveItem.dailyCost).toFixed(2) }}/天</span>
            <span v-if="summary.cheapestItem" class="sum-sub">最省日均：{{ summary.cheapestItem.name }} ¥{{ Number(summary.cheapestItem.dailyCost).toFixed(2) }}/天</span>
          </div>
        </div>
        <div class="sum-card this-year">
          <div class="sum-icon"><el-icon :size="22"><Coin /></el-icon></div>
          <div class="sum-body">
            <span class="sum-label">今年总支出</span>
            <span class="sum-value">¥{{ (Number(summary.totalExpenseThisYear) || 0).toLocaleString() }}</span>
            <span class="sum-sub">房租水电 ¥{{ (Number(summary.billTotalThisYear) || 0).toLocaleString() }} + 维护 ¥{{ (Number(summary.maintenanceTotalThisYear) || 0).toLocaleString() }} + 物件 ¥{{ (Number(summary.itemCostThisYear) || 0).toLocaleString() }}</span>
          </div>
        </div>
      </div>

      <div class="chart-row">
        <div class="chart-card">
          <div class="chart-header">
            <h4>家庭储蓄趋势</h4>
            <el-date-picker v-model="savingYear" type="year" placeholder="选择年份" value-format="YYYY" clearable style="width:130px" @change="loadSavingTrend" />
          </div>
          <div class="chart-body" ref="savingChartRef" style="height:320px;"></div>
        </div>
        <div class="chart-card">
          <div class="chart-header">
            <h4>房租水电同比（各月合计）</h4>
            <el-select v-model="billAddressId" placeholder="全部住址" clearable style="width:150px" @change="loadBillCompare">
              <el-option v-for="a in addresses" :key="a.id" :label="a.name" :value="a.id" />
            </el-select>
          </div>
          <div class="chart-body" ref="billChartRef" style="height:320px;"></div>
        </div>
      </div>
      <div class="chart-card" style="margin-top:20px;">
        <div class="chart-header">
          <h4>物件日均成本</h4>
          <span style="font-size:12px;color:#909399;">（总投入 ÷ 持有天数）</span>
        </div>
        <div class="chart-body" ref="itemCostChartRef" :style="{ height: itemCostChartHeight + 'px' }"></div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { savingApi, addressApi, dashboardApi } from '@/api'
import * as echarts from 'echarts'
import { Ticket, Goods, Wallet, MapLocation, User, Avatar, Menu, Collection, HomeFilled, Coin, UserFilled } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()

const summary = ref(null)
const savingChartRef = ref(null)
const billChartRef = ref(null)
const itemCostChartRef = ref(null)
const itemCostChartHeight = ref(400)
const addresses = ref([])
const savingYear = ref('')
const billAddressId = ref(null)

function goToFamily() { router.push('/family') }

async function loadAddresses() {
  if (!userStore.currentFamily) return
  try { const res = await addressApi.listByFamily(userStore.currentFamily.id); addresses.value = res.data || [] } catch(e) {}
}

function renderSavingChart(data) {
  if (!savingChartRef.value) return
  const chart = echarts.init(savingChartRef.value)
  if (!data || data.length === 0) {
    chart.setOption({
      title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#c0c4cc', fontSize: 14 } }
    })
    return
  }
  chart.setOption({
    tooltip: { trigger: 'axis', formatter: p => `${p[0].name}<br/>合计：¥${p[0].value.toLocaleString()}` },
    grid: { left: 80, right: 40, top: 40, bottom: 50 },
    xAxis: { type: 'category', data: data.map(d => d.date), axisLabel: { rotate: 30 }, name: '日期' },
    yAxis: { type: 'value', name: '金额(元)', axisLabel: { formatter: v => (v/10000).toFixed(1) + '万' } },
    series: [{
      type: 'line', data: data.map(d => Number(d.total) || 0), smooth: true,
      lineStyle: { color: '#409eff', width: 2 },
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: 'rgba(64,158,255,0.3)' }, { offset: 1, color: 'rgba(64,158,255,0.02)' }
      ])},
      symbol: 'circle', symbolSize: 6,
      itemStyle: { color: '#409eff' }
    }]
  })
  return chart
}

function renderBillChart(data) {
  if (!billChartRef.value) return
  const chart = echarts.init(billChartRef.value)
  if (!data || data.length === 0) {
    chart.setOption({
      title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#c0c4cc', fontSize: 14 } }
    })
    return
  }
  const years = Object.keys(data[0]).filter(k => k !== 'month')
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399']
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: years, top: 0 },
    grid: { left: 80, right: 40, top: 50, bottom: 50 },
    xAxis: { type: 'category', data: data.map(d => d.month), name: '月份' },
    yAxis: { type: 'value', name: '金额(元)', axisLabel: { formatter: v => (v/1000).toFixed(0) + 'k' } },
    series: years.map((y, i) => ({
      name: y + '年', type: 'line', data: data.map(d => { const v = d[y]; return v != null ? Number(v) : null }),
      smooth: true, lineStyle: { width: 2 }, symbol: 'circle', symbolSize: 6,
      itemStyle: { color: colors[i % colors.length] }
    }))
  })
  return chart
}

function renderItemCostChart(data) {
  if (!itemCostChartRef.value) return
  const chart = echarts.init(itemCostChartRef.value)
  if (!data || data.length === 0) {
    chart.setOption({
      title: { text: '暂无物件数据', left: 'center', top: 'center', textStyle: { color: '#c0c4cc', fontSize: 14 } }
    })
    return
  }
  const names = data.map(d => d.name)
  const dailyValues = data.map(d => Number(d.dailyCost) || 0)
  // 动态高度：每条最少 24px，最小 400px
  const rowHeight = 24
  const minHeight = 400
  const dynamicHeight = Math.max(minHeight, data.length * rowHeight + 60)
  itemCostChartHeight.value = dynamicHeight
  // 自适应柱宽：高度够用则固定 20，否则缩窄
  const barW = data.length > 18 ? Math.max(10, Math.floor((dynamicHeight - 100) / data.length)) : 20
  chart.setOption({
    tooltip: {
      trigger: 'axis', axisPointer: { type: 'shadow' },
      formatter: p => {
        const d = data[p[0].dataIndex]
        return `<strong>${d.name}</strong><br/>
                购买金额：¥${Number(d.purchaseAmount).toLocaleString()}<br/>
                维护费用：¥${Number(d.maintenanceCost).toLocaleString()}<br/>
                残值回收：¥${Number(d.residualValue || 0).toLocaleString()}<br/>
                总投入：¥${Number(d.totalCost).toLocaleString()}<br/>
                持有天数：${d.daysOwnedText}<br/>
                日均成本：<strong>¥${Number(d.dailyCost).toFixed(2)}</strong>`
      }
    },
    grid: { left: 120, right: 60, top: 20, bottom: 40 },
    xAxis: { type: 'value', name: '日均成本(元)', axisLabel: { formatter: v => '¥' + v.toFixed(1) } },
    yAxis: { type: 'category', data: names, axisLabel: { fontSize: 12, interval: 0 } },
    series: [{
      type: 'bar', data: dailyValues,
      barWidth: barW,
      itemStyle: {
        borderRadius: [0, 4, 4, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#409eff' }, { offset: 1, color: '#79bbff' }
        ])
      },
      label: { show: true, position: 'right', formatter: p => '¥' + Number(p.value).toFixed(2), fontSize: 11, color: '#606266' }
    }]
  })
  return chart
}

let savingChartInstance = null
let billChartInstance = null
let itemCostChartInstance = null

async function loadSavingTrend() {
  if (!userStore.currentFamily) return
  try {
    const res = await dashboardApi.savingTrend(userStore.currentFamily.id)
    const data = res.data || []
    if (savingChartInstance) savingChartInstance.dispose()
    savingChartInstance = renderSavingChart(data)
  } catch(e) {}
}

async function loadBillCompare() {
  if (!userStore.currentFamily) return
  try {
    const res = await dashboardApi.billCompare(userStore.currentFamily.id, billAddressId.value || undefined)
    const data = res.data || []
    console.log('bill compare data:', data)
    await nextTick()
    if (billChartInstance) billChartInstance.dispose()
    billChartInstance = renderBillChart(data)
  } catch(e) {}
}

async function loadItemDailyCost() {
  if (!userStore.currentFamily) return
  try {
    const res = await dashboardApi.itemDailyCost(userStore.currentFamily.id)
    const data = res.data || []
    if (itemCostChartInstance) itemCostChartInstance.dispose()
    itemCostChartInstance = renderItemCostChart(data)
  } catch(e) {}
}

async function loadSummary() {
  if (!userStore.currentFamily) return
  try {
    const res = await dashboardApi.summary(userStore.currentFamily.id)
    summary.value = res.data || null
  } catch(e) {}
}

watch(() => userStore.currentFamilyId, () => {
  if (!userStore.isAdmin) {
    loadAddresses()
    loadSummary()
    loadSavingTrend()
    loadBillCompare()
    loadItemDailyCost()
  }
})

onMounted(() => {
  if (!userStore.isAdmin && userStore.currentFamily) {
    loadAddresses()
    loadSummary()
    loadSavingTrend()
    loadBillCompare()
    loadItemDailyCost()
  }
})
</script>

<style lang="scss" scoped>
.welcome-section {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  border-radius: 16px;
  padding: 32px 36px;
  margin-bottom: 28px;
  position: relative;
  overflow: hidden;
  &::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -20%;
    width: 400px; height: 400px;
    background: radial-gradient(circle, rgba(64,158,255,0.15), transparent);
    border-radius: 50%;
  }
  .welcome-content { display: flex; justify-content: space-between; align-items: center; position: relative; z-index: 1; }
  .welcome-text {
    h2 { color: #fff; font-size: 22px; font-weight: 600; margin: 0 0 8px; }
    p { color: rgba(255,255,255,0.7); margin: 0; font-size: 14px;
      strong { color: rgba(255,255,255,0.95); }
      a { color: #409eff; cursor: pointer; text-decoration: underline; }
    }
  }
  .big-avatar { background: linear-gradient(135deg, #409eff, #337ecc); font-size: 28px; border: 3px solid rgba(255,255,255,0.2); box-shadow: 0 8px 24px rgba(0,0,0,0.2); }
}

.stat-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 20px; margin-bottom: 28px; }
.stat-card { background: #fff; border-radius: 14px; padding: 24px; display: flex; align-items: center; gap: 16px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); transition: all 0.3s; position: relative; overflow: hidden;
  &:hover { transform: translateY(-3px); box-shadow: 0 8px 24px rgba(0,0,0,0.1); }
  &.admin-card .stat-card-glow { position: absolute; top: -50%; right: -30%; width: 180px; height: 180px; background: radial-gradient(circle, rgba(64,158,255,0.08), transparent); border-radius: 50%; }
  .stat-card-icon { width: 54px; height: 54px; border-radius: 14px; display: flex; align-items: center; justify-content: center; color: #fff; flex-shrink: 0; }
  .stat-card-body { display: flex; flex-direction: column; gap: 4px; .stat-label { font-size: 13px; color: #909399; } .stat-value { font-size: 22px; font-weight: 700; color: #303133; } }
}
.action-section { margin-bottom: 24px; }
.action-section-title { font-size: 15px; font-weight: 600; color: #303133; margin: 0 0 16px; }
.action-grid { display: flex; flex-wrap: wrap; gap: 14px; }
.action-card { display: flex; align-items: center; gap: 12px; padding: 16px 22px; border-radius: 12px; background: #fff; color: #606266; text-decoration: none; font-size: 14px; transition: all 0.25s; box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  &:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0,0,0,0.08); color: #409eff; }
  .action-icon { width: 42px; height: 42px; border-radius: 10px; display: flex; align-items: center; justify-content: center; color: #fff; flex-shrink: 0; }
  span { font-weight: 500; }
}

.summary-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
.sum-card { background: #fff; border-radius: 14px; padding: 20px; display: flex; align-items: flex-start; gap: 14px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); transition: all 0.3s;
  &:hover { transform: translateY(-3px); box-shadow: 0 8px 24px rgba(0,0,0,0.1); }
  .sum-icon { width: 46px; height: 46px; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: #fff; flex-shrink: 0; }
  .sum-body { display: flex; flex-direction: column; gap: 4px; min-width: 0;
    .sum-label { font-size: 13px; color: #909399; white-space: nowrap; }
    .sum-value { font-size: 20px; font-weight: 700; color: #303133; }
    .sum-sub { font-size: 11px; color: #b0b3bb; line-height: 1.4; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  }
  &.saving .sum-icon { background: linear-gradient(135deg, #67c23a, #529b2e); }
  &.expense .sum-icon { background: linear-gradient(135deg, #e6a23c, #d4880f); }
  &.item .sum-icon { background: linear-gradient(135deg, #409eff, #337ecc); }
  &.this-year .sum-icon { background: linear-gradient(135deg, #f56c6c, #d94343); }
}
.chart-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.chart-card { background: #fff; border-radius: 14px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
.chart-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;
  h4 { margin: 0; font-size: 15px; font-weight: 600; color: #303133; }
}
.chart-body { width: 100%; }

@media (max-width: 768px) {
  .welcome-section { padding: 24px 20px; .welcome-text h2 { font-size: 18px; } }
  .stat-grid { grid-template-columns: 1fr; }
  .summary-grid { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .sum-card { padding: 14px; gap: 10px;
    .sum-icon { width: 38px; height: 38px; border-radius: 10px; }
    .sum-body {
      .sum-value { font-size: 17px; }
      .sum-sub { white-space: normal; }
    }
  }
  .action-grid { flex-direction: column; }
  .action-card { width: 100%; }
  .chart-row { grid-template-columns: 1fr; }
}
</style>
