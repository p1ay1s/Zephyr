package com.zephyr.vbclass.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zephyr.vbclass.ViewBindingInterface

/**
 * @param VB databinding
 * @param D dataclass
 * @param itemCallback 自行实现 YourCallback: DiffUtil.ItemCallback<YourBean>()
 *
 * @see ViewBindingInterface 注意事项
 */
abstract class ViewBindingListAdapter<VB : ViewDataBinding, D>(
    itemCallback: DiffUtil.ItemCallback<D>
) : ListAdapter<D, ViewBindingListAdapter<VB, D>.ViewHolder>(itemCallback),
    ViewBindingInterface<VB> {

    inner class ViewHolder(val binding: VB) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return with(getViewBinding(LayoutInflater.from(parent.context), parent)) {
            ViewHolder(this)
        }
    }

    /**
     * 取代了 "onBindViewHolder"
     *
     * 不再需要写 "executePendingBindings()"
     */
    abstract fun VB.onBindViewHolder(data: D?, position: Int)

    /**
     * 不需要再重写
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            val item = try {
                getItem(position)
            } catch (_: Exception) {
                null
            }
            onBindViewHolder(item, position)
            executePendingBindings()
        }
    }
}