package com.w2sv.autocrop.screenshotlistening

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import kotlin.reflect.KFunction4

typealias PendingIntentRenderer = KFunction4<Context, Int, Intent, Int, PendingIntent>