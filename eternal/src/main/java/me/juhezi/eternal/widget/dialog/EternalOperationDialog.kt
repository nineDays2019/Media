package me.juhezi.eternal.widget.dialog

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.annotation.LayoutRes
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import me.juhezi.eternal.R
import me.juhezi.eternal.base.BaseDialog
import me.juhezi.eternal.extension.dip2px

class EternalOperationDialog(context: Context?, theme: Int = R.style.full_screen_dialog) :
        BaseDialog(context, theme) {

    companion object {
        @JvmStatic
        val CANCEL_ID = "cancel"    // cancel 按钮的固定 id

        @JvmStatic
        val SINGLE_TEXT_TYPE = 0x001
        @JvmStatic
        val ICON_AND_TEXT_TYPE = 0x002

        @JvmStatic
        val ICON_AND_TEXT_LAYOUT_RES = R.layout.item_icon_and_text_operation    // icon + text 布局文件

        @JvmStatic
        val DEFAULT_TEXT_SIZE = 20f     // 字体大小默认为 20sp

        @ColorInt
        @JvmStatic
        val DEFAULT_TEXT_COLOR = Color.parseColor("#FF272727") // 默认字体颜色
    }


    @LayoutRes
    private val mLayoutRes = R.layout.dialog_operation
    private val mContainer: LinearLayout
    private val mOperationMap: LinkedHashMap<String, Operation> = LinkedHashMap()
    var onCreateView: ((Operation) -> View)? = null
    var onClickListener: ((String, Int) -> Unit)? = null

    init {
        setContentView(mLayoutRes)
        mContainer = findViewById(R.id.vg_operation_dialog_container)
    }

    @TargetApi(Build.VERSION_CODES.N)
    fun apply() {
        mContainer.removeAllViews()
        val size = mOperationMap.size
        var index = 0
        mOperationMap.forEach { _, operation ->
            val view = createView(operation)
            view.setOnClickListener {
                onClickListener?.invoke(operation.id, operation.type)   // 点击事件
            }
            mContainer.addView(view)
            if (index != size - 1) {    // 添加分割线
                mContainer.addView(createDividerView())
            }
            index++
        }
    }

    override fun show() {
        show(0, 0)
    }

    fun show(x: Int, y: Int) {
        windowDeploy(x, y)
        super.show()
    }

    fun clear() = mContainer.removeAllViews()

    private fun windowDeploy(x: Int, y: Int) {
        window.setWindowAnimations(R.style.OperationDialogAnim) // 设置窗口弹出动画
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        val layoutParams = window.attributes
        // 根据 x，y坐标设置窗口需要显示的位置
        layoutParams.x = x
        layoutParams.y = y
        layoutParams.gravity = Gravity.BOTTOM   //位于底部
        // update window layout
        onWindowAttributesChanged(layoutParams)
    }

    /**
     * 生成分割线 View
     */
    private fun createDividerView(): View {
        val view = View(context)
        val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, context.dip2px(0.5f))
        view.layoutParams = layoutParams
        view.setBackgroundColor(context.resources.getColor(R.color.black_alpha20))
        return view
    }

    /**
     * 根据对应的 type 生成固定的 View
     * 内置只有两种
     * * 单文本
     * * Icon + 单文本
     */
    private fun createView(operation: Operation): View {
        val type = operation.type
        val wrapper = RelativeLayout(context)
        val wrapperLayoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        context.dip2px(56f))    // 高度固定为 56 dp
        wrapper.layoutParams = wrapperLayoutParams
        val contentLayoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        contentLayoutParams.setMargins(context.dip2px(2f),
                context.dip2px(2f),
                context.dip2px(2f),
                context.dip2px(2f)) // Margin 设置为 2dp
        val contentView: View = when (type) {
            SINGLE_TEXT_TYPE -> {
                // 直接创建一个 textView
                val textView = TextView(context)
                textView.gravity = Gravity.CENTER   // 内容在中间
                if (operation is TextOperation) {
                    operation.closure?.invoke(textView)
                }
                textView
            }
            ICON_AND_TEXT_TYPE -> {
                val view = LayoutInflater.from(context).inflate(ICON_AND_TEXT_LAYOUT_RES, null)
                val icon = view.findViewById<ImageView>(R.id.iv_icon_text_operation_icon)
                val text = view.findViewById<TextView>(R.id.tv_icon_text_operation_text)
                if (operation is GeneralOperation) {
                    operation.iconClosure?.invoke(icon)
                    operation.textClosure?.invoke(text)
                }
                view
            }
            else -> onCreateView?.invoke(operation) ?: View(context)
        }
        contentView.layoutParams = contentLayoutParams
        wrapper.addView(contentView)
        wrapper.setBackgroundResource(R.drawable.selector_operation_item)
        return wrapper
    }

    fun addOperation(operationItem: Operation): Boolean {
        if (CANCEL_ID == operationItem.id || mOperationMap.containsKey(operationItem.id)) return false   // id 重复，返回 false
        mOperationMap[operationItem.id] = operationItem
        return true
    }

    fun removeOperation(id: String) = mOperationMap[id]

    fun hasOperation(id: String) = mOperationMap.containsKey(id)

    fun addTextOperation(id: String, content: String = "", closure: (TextView.() -> Unit)? = {
        textSize = DEFAULT_TEXT_SIZE
        setTextColor(DEFAULT_TEXT_COLOR)
    }) = addOperation(TextOperation(id) {
        text = content
        closure?.invoke(this)
    })

    fun addGeneralOperation(id: String,
                            @DrawableRes drawableRes: Int = R.drawable.ic_error_outline_black,
                            content: String = "",
                            iconClosure: (ImageView.() -> Unit)? = null,
                            textClosure: (TextView.() -> Unit)? = {
                                textSize = DEFAULT_TEXT_SIZE
                                setTextColor(DEFAULT_TEXT_COLOR)
                            }) = addOperation(
            GeneralOperation(id, {
                setImageDrawable(context.getDrawable(drawableRes))
                iconClosure?.invoke(this)
            }, {
                text = content
                textClosure?.invoke(this)
            }))


    /**
     * 添加取消按钮
     */
    fun addCancelOperation(closure: (TextView.() -> Unit)? = {
        text = context.getText(R.string.cancel)     // 文本为取消
        textSize = DEFAULT_TEXT_SIZE
        setTextColor(DEFAULT_TEXT_COLOR)
    }): Boolean {
        if (hasOperation(CANCEL_ID)) return false
        val cancelOperation = TextOperation(CANCEL_ID, closure)
        mOperationMap[CANCEL_ID] = cancelOperation
        return true
    }

    open inner class Operation(open val id: String, open val type: Int)

    /**
     * 0x001
     * 只有一个文本的 Item
     */
    inner class TextOperation(override val id: String,
                              var closure: (TextView.() -> Unit)? = null) : Operation(id, SINGLE_TEXT_TYPE)

    /**
     * 0x002
     * 一个 icon 和一个文本的 Item
     */
    inner class GeneralOperation(override val id: String,
                                 var iconClosure: (ImageView.() -> Unit)? = null,
                                 var textClosure: (TextView.() -> Unit)? = null) : Operation(id, ICON_AND_TEXT_TYPE)


}
