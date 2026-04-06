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

    // 判断图标类型并添加对应的图标
    if (icon) {
      if (icon.startsWith('el-icon')) {
        // Element UI 图标
        vnodes.push(<i class={icon} style="margin-right: 8px;"></i>)
      } else {
        // SVG 图标
        vnodes.push(<svg-icon icon-class={icon}/>)
      }
    }

    // 添加标题
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
