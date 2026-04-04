<script>
export default {
  name: 'MenuItem',
  functional: true,
  props: {
    icon: {
      type: String,
      default: ''
    },
    title: {
      type: String,
      default: ''
    }
  },
  render(h, context) {
    const { icon, title } = context.props
    const vnodes = []

    if (icon) {
      // 判断是否为 Element UI 图标（以 el-icon 开头）
      if (icon.indexOf('el-icon') === 0) {
        // 使用 Element UI 图标
        vnodes.push(<i class={icon} style="margin-right: 8px;"></i>)
      } else {
        // 使用自定义 SVG 图标
        vnodes.push(<svg-icon icon-class={icon}/>)
      }
    }

    if (title) {
      if (title.length > 5) {
        vnodes.push(<span slot='title' title={(title)}>{(title)}</span>)
      } else {
        vnodes.push(<span slot='title'>{(title)}</span>)
      }
    }
    return vnodes
  }
}
</script>
