package com.example.register.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.register.R
import com.example.register.data.GoldRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GoldRateWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_gold_rate)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = GoldRepository()
                val goldRate = repository.getGoldRate()

                views.setTextViewText(R.id.tvGoldRate, "Золото: ${goldRate} руб/г")
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                views.setTextViewText(R.id.tvGoldRate, "Ошибка загрузки")
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}