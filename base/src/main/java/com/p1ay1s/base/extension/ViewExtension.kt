package com.p1ay1s.base.extension

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.p1ay1s.base.appContext
import com.p1ay1s.base.ui.FragmentHost
import com.p1ay1s.base.ui.FragmentHostView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val Activity.TAG
    get() = this::class.simpleName!!
val Fragment.TAG
    get() = this::class.simpleName!!

fun Fragment.findHost(): FragmentHost? {
    var view = view
    var parent = view?.parent

    while (parent != null) {
        if (parent is FragmentHostView) {
            return parent.getActiveHost()
        }
        view = parent as? View // as? 如果转换失败则变为 null
        parent = view?.parent
    }
    return null
}

private const val RADIUS = 25

fun ImageView.loadRadiusImage(
    imgUrl: String,
    radius: Int = RADIUS,
    enableCrossFade: Boolean = true,
) = set(imgUrl) {
    if (enableCrossFade)
        transition(DrawableTransitionOptions.withCrossFade())
    transform(CenterCrop(), RoundedCorners(radius))
}

fun ImageView.loadCircleImage(
    imgUrl: String,
    enableCrossFade: Boolean = false,
) = set(imgUrl) {
    if (enableCrossFade)
        transition(DrawableTransitionOptions.withCrossFade())
    transform(CircleCrop())
}

/**
 * 没有任何偏好
 */
fun ImageView.loadImage(
    imgUrl: String,
    enableCrossFade: Boolean = true,
) = set(imgUrl) {
    if (enableCrossFade)
        transition(DrawableTransitionOptions.withCrossFade())
    transform(CenterCrop())
}

private fun ImageView.set(
    imgUrl: String,
    preferences: RequestBuilder<Drawable>.() -> RequestBuilder<Drawable> = { this }
) {
    setVisible()
    Glide.with(this)
        .load(imgUrl)
        .fitCenter()
        .preferences()
        .into(this)
}

private fun ImageView.setVisible() {
    visibility = View.VISIBLE
}

suspend fun toastSuspended(msg: String, length: Int = Toast.LENGTH_SHORT) =
    withContext(Dispatchers.Main) {
        toast(msg, length)
    }

fun toast(msg: String, length: Int = Toast.LENGTH_SHORT) {
    if (msg.isNotBlank() && appContext != null)
        Toast.makeText(appContext, msg, length).show()
}

fun Any?.toast() {
    val str = this.toString()
    if (str.isNotBlank()) toast(str)
}


/**
 * 检查和获取权限, 最后后调权限情况
 */
fun AppCompatActivity.withPermission(
    name: String = WRITE_EXTERNAL_STORAGE,
    callback: (isGranted: Boolean) -> Unit
) {
    if (isPermissionGranted(name)) {
        callback(true)
    } else {
        requestPermission(name, callback)
    }
}

/**
 * @param name Manifest.permission.XXX
 *
 * startActivityForResult 的简化版
 */
fun AppCompatActivity.requestPermission(
    name: String = WRITE_EXTERNAL_STORAGE,
    callback: (isGranted: Boolean) -> Unit
) = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
    callback(it)
}.launch(name)

fun Activity.isPermissionGranted(name: String): Boolean = ContextCompat.checkSelfPermission(
    this,
    name
) == PackageManager.PERMISSION_GRANTED

fun RecyclerView.setSnapHelper() {
    if (onFlingListener == null)
        PagerSnapHelper().attachToRecyclerView(this)
}

/**
 * 添加原生分割线
 */
fun RecyclerView.addLineDecoration(context: Context, orientation: Int) {
    if (itemDecorationCount == 0 && layoutManager != null)
        addItemDecoration(
            DividerItemDecoration(
                context,
                orientation
            )
        )
}

/**
 * @param cannotScrollOrientation 指定的方向
 *  1: 无法往下
 *
 * -1: 无法往上
 */
fun RecyclerView.addOnLoadMoreListener_V(cannotScrollOrientation: Int, onLoad: () -> Unit) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_IDLE) // 已停止
                if (!canScrollVertically(cannotScrollOrientation)) // 在到达末尾
                    onLoad()
        }
    })
}

/**
 * @param cannotScrollOrientation 指定的方向
 *  1: 无法往右
 *
 * -1: 无法往左
 */
fun RecyclerView.addOnLoadMoreListener_H(cannotScrollOrientation: Int, onLoad: () -> Unit) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_IDLE) // 已停止
                if (!canScrollHorizontally(cannotScrollOrientation)) // 在到达末尾
                    onLoad()
        }
    })
}