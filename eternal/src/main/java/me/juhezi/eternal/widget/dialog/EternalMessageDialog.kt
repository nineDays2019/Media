package me.juhezi.eternal.widget.dialog

import android.content.Context
import android.support.annotation.LayoutRes
import android.view.View
import android.widget.Button
import android.widget.TextView
import me.juhezi.eternal.R
import me.juhezi.eternal.base.BaseDialog
import me.juhezi.eternal.extension.isEmpty

/**
 * Created by Juhezi[juhezix@163.com] on 2018/8/23.
 */
class EternalMessageDialog(context: Context?, theme: Int = R.style.full_screen_dialog) :
        BaseDialog(context, theme) {

    @LayoutRes
    private val mLayoutRes = R.layout.dialog_message
    private val mButton: Button
    private val mTvTitle: TextView
    private val mTvMessage: TextView

    var onConfirmClickListener: (() -> Unit)? = null

    init {
        setContentView(mLayoutRes)
        mButton = findViewById(R.id.btn_message_dialog_confirm)
        mTvTitle = findViewById(R.id.tv_message_dialog_title)
        mTvMessage = findViewById(R.id.tv_message_dialog_message)
        mButton.setOnClickListener {
            dismiss()
            onConfirmClickListener?.invoke()
        }
    }

    fun configConfirmButton(closure: (Button.() -> Unit)?) {
        closure?.invoke(mButton)
    }

    fun configTitleTextView(closure: (TextView.() -> Unit)?) {
        closure?.invoke(mTvTitle)
    }

    fun configMessageTextView(closure: (TextView.() -> Unit)?) {
        closure?.invoke(mTvMessage)
    }

    fun setDisplayContent(title: String? = null,
                          message: String? = null,
                          buttonContent: String? = null) {
        mTvTitle.text = title ?: context.getString(R.string.notice)
        mTvMessage.text = message
        mButton.text = buttonContent ?: context.getString(R.string.confirm)
    }

    override fun show() {
        if (isEmpty(mTvMessage.text.toString())) {
            mTvMessage.visibility = View.GONE
        } else {
            mTvMessage.visibility = View.VISIBLE
        }
        super.show()
    }

}
