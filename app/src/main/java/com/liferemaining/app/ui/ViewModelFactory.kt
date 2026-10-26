package com.liferemaining.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** 轻量工厂：项目只有一个容器，不需要反射注入。 */
inline fun <reified VM : ViewModel> viewModelFactory(
    crossinline create: () -> VM
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = create() as T
}
