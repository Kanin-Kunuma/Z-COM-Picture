<template>
  <div class="space-rank-analyze">
    <a-card title="空间使用排行分析">
      <v-chart ref="chartRef" :option="options" style="height: 320px; max-width: 100%;" :loading="loading" />
    </a-card>
  </div>
</template>

<script setup lang="ts">
import VChart from 'vue-echarts'
import 'echarts'
import { computed, onMounted, onUnmounted, ref, watchEffect } from 'vue'
import { getSpaceRankAnalyzeUsingPost } from '@/api/spaceAnalyzeController.ts'
import { message } from 'ant-design-vue'

interface Props {
  queryAll?: boolean
  queryPublic?: boolean
  spaceId?: number
}

const props = withDefaults(defineProps<Props>(), {
  queryAll: false,
  queryPublic: false,
})

// 图表数据
const dataList = ref<API.Space[]>([])
// 加载状态
const loading = ref(true)

// 获取数据
const fetchData = async () => {
  loading.value = true
  // 转换搜索参数
  const res = await getSpaceRankAnalyzeUsingPost({
    queryAll: props.queryAll,
    queryPublic: props.queryPublic,
    spaceId: props.spaceId,
    topN: 10, // 后端默认是 10
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data ?? []
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}

/**
 * 监听变量，参数改变时触发数据的重新加载
 */
watchEffect(() => {
  fetchData()
})

// 图表选项
const options = computed(() => {
  const spaceNames = dataList.value.map((item) => item.spaceName)
  const usageData = dataList.value.map((item) => (item.totalSize / (1024 * 1024)).toFixed(2)) // 转为 MB

  return {
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: spaceNames,
    },
    yAxis: {
      type: 'value',
      name: '空间使用量 (MB)',
    },
    series: [
      {
        name: '空间使用量 (MB)',
        type: 'bar',
        data: usageData,
        itemStyle: {
          color: '#5470C6', // 自定义柱状图颜色
        },
      },
    ],
  }
})


// 补充：图表自适应窗口
const chartRef = ref()

// 处理图表
const handleResize = () => {
  if (chartRef.value) {
    chartRef.value.chart.resize()
  }
}

// 防抖函数
function useDebounce(fn: () => void, delay: number) {
  let timer: NodeJS.Timeout | null = null;
  return function (this: any, ...args: any[]) {
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => {
      fn.apply(this, args);
    }, delay);
  }; // 补上闭合
}

// 创建防抖后的 resize 处理函数
const debouncedResize = useDebounce(handleResize, 300)

// 组件挂载时获取数据和添加 resize 监听
onMounted(() => {
  fetchData()
  window.addEventListener('resize', debouncedResize)
});


// 组件卸载时移除 resize 监听
onUnmounted(() => {
  window.removeEventListener('resize', debouncedResize)
});


</script>

<style scoped></style>
