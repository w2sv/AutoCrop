package com.autocrop.utils.kotlin

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import kotlin.reflect.KFunction4

typealias BlankFun = () -> Unit
typealias PendingIntentRenderer = KFunction4<Context, Int, Intent, Int, PendingIntent>