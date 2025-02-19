package com.zephyr.vbclass

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import com.zephyr.global_values.TAG
import com.zephyr.log.logI

/**
 * @see ViewBindingInterface 注意事项
 */
abstract class ViewBindingActivity<VB : ViewDataBinding> : AppCompatActivity(),
    ViewBindingInterface<VB> {

    /**
     * 函数内可直接引用控件id
     */
    abstract fun VB.initBinding()

    private var _binding: VB? = null
    protected val binding: VB
        get() = _binding!!

    /**
     * 子类的 super 方法包含了 initBinding, 可以据此安排代码
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        logI(TAG, "$TAG: onCreate")
        super.onCreate(savedInstanceState)
        _binding = getViewBinding(layoutInflater)
        setContentView(binding.root)
        binding.initBinding()
    }

    override fun onStart() {
        logI(TAG, "$TAG: onStart")
        super.onStart()
    }

    override fun onResume() {
        logI(TAG, "$TAG: onResume")
        super.onResume()
    }

    override fun onPause() {
        logI(TAG, "$TAG: onPause")
        super.onPause()
    }

    override fun onStop() {
        logI(TAG, "$TAG: onStop")
        super.onStop()
    }

    /**
     * 防止内存泄露
     */
    override fun onDestroy() {
        logI(TAG, "$TAG: onDestroy")
        super.onDestroy()
        _binding?.unbind()
        _binding = null
    }
}