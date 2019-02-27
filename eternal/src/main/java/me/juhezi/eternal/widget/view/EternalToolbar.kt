package me.juhezi.eternal.widget.view

import android.content.Context
import android.support.annotation.DrawableRes
import android.support.annotation.LayoutRes
import android.util.ArrayMap
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import me.juhezi.eternal.R
import me.juhezi.eternal.enum.ToolbarStyle

/**
 * 项目中通用的控件
 *
 * mode:
 *
 * Created by Juhezi[juhezix@163.com] on 2018/7/26.
 */
class EternalToolbar @JvmOverloads constructor(context: Context,
                                               attrs: AttributeSet? = null,
                                               defStyleAttr: Int = 0,
                                               defStyleRes: Int = 0)
    : RelativeLayout(context, attrs, defStyleAttr, defStyleRes) {

    var leftStyle = ToolbarStyle.TEXT    // 左边默认为 Text
        set(value) {
            field = value
            updateLeftView()
        }
    var rightStyle = ToolbarStyle.NONE   // 右边默认为 空
        set(value) {
            field = value
            updateRightView()
        }
    var centerStyle = ToolbarStyle.NONE    // 中间也默认为空
        set(value) {
            field = value
            updateCenterView()
        }

    private var mLeftViewMap = ArrayMap<Int, View?>()
    private var mRightViewMap = ArrayMap<Int, View?>()
    private var mCenterViewMap = ArrayMap<Int, View?>()

    @LayoutRes
    private var layoutRes = R.layout.view_eternal_toolbar

    //----Right
    private var mRightTextView: TextView?
    private var mRightImageView: ImageView?

    //----Center


    //----Left
    private var mLeftTextView: TextView?
    private var mLeftImageView: ImageView?
    private var mLeftGroup: ViewGroup?
    private var mLeftGroupTextView: TextView?
    private var mLeftGroupImageView: ImageView?

    var onRightTextClickListener: (() -> Unit)? = null
    var onRightIconClickListener: (() -> Unit)? = null
    //--

    //--
    var onLeftTextClickListener: (() -> Unit)? = null
    var onLeftIconClickListener: (() -> Unit)? = null
    var onLeftGroupTextClickListener: (() -> Unit)? = null
    var onLeftGroupIconClickListener: (() -> Unit)? = null

    init {
        //--- init
        View.inflate(context, layoutRes, this)
        mRightTextView = findViewById(R.id.tv_eternal_right)
        mRightImageView = findViewById(R.id.iv_eternal_right)
        //--

        //--
        mLeftTextView = findViewById(R.id.tv_eternal_left)
        mLeftImageView = findViewById(R.id.iv_eternal_left)
        mLeftGroup = findViewById(R.id.vg_eternal_left_group)
        mLeftGroupTextView = findViewById(R.id.tv_eternal_group_left)
        mLeftGroupImageView = findViewById(R.id.iv_eternal_group_left)
        //--- init View Map

        mRightViewMap[ToolbarStyle.TEXT.key] = mRightTextView
        mRightViewMap[ToolbarStyle.ICON.key] = mRightImageView
        //--

        //--
        mLeftViewMap[ToolbarStyle.TEXT.key] = mLeftTextView
        mLeftViewMap[ToolbarStyle.ICON.key] = mLeftImageView
        mLeftViewMap[ToolbarStyle.ICON_AND_TEXT.key] = mLeftGroup
        //--- init Style Show

        updateLeftView()
        updateCenterView()
        updateRightView()

        //--- event
        mRightTextView?.setOnClickListener {
            onRightTextClickListener?.invoke()
        }
        mRightImageView?.setOnClickListener {
            onRightIconClickListener?.invoke()
        }
        //--

        //--
        mLeftTextView?.setOnClickListener {
            onLeftTextClickListener?.invoke()
        }
        mLeftImageView?.setOnClickListener {
            onLeftIconClickListener?.invoke()
        }
        mLeftGroupTextView?.setOnClickListener {
            onLeftGroupTextClickListener?.invoke()
        }
        mLeftGroupImageView?.setOnClickListener {
            onLeftGroupIconClickListener?.invoke()
        }
    }

    private fun updateLeftView() {
        updateView(leftStyle, mLeftViewMap)
    }

    private fun updateCenterView() {
        updateView(centerStyle, mCenterViewMap)
    }

    private fun updateRightView() {
        updateView(rightStyle, mRightViewMap)
    }

    private fun updateView(style: ToolbarStyle, viewMap: Map<Int, View?>) = hideViewExcept(style, viewMap)


    private fun hideViewExcept(style: ToolbarStyle, viewMap: Map<Int, View?>) {
        viewMap.entries.forEach {
            it.value?.visibility = if (it.key == style.key) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    fun configLeftGroup(text: String = context.getString(R.string.app_name),
                        @DrawableRes iconRes: Int = R.drawable.ic_arrow_back_write) {
        mLeftGroupTextView?.text = text
        mLeftGroupImageView?.setImageDrawable(context.getDrawable(iconRes))
    }

}
