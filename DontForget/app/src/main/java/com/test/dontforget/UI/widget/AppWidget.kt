package com.test.dontforget.UI.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.test.dontforget.MainActivity
import com.test.dontforget.R
import com.test.dontforget.Repository.TodoRepository

/**
 * Implementation of App Widget functionality.
 */
class AppWidget : AppWidgetProvider() {

    // 위젯 메타 데이터를 구성 할 때 updatePeriodMillis 라는 업데이트 주기 값을 설정하게 되며, 이 주기에 따라 호출 됩니다.
    // 또한 앱 위젯이 추가 될 떄에도 호출 되므로 Service 와의 상호작용 등의 초기 설정이 필요 할 경우에도 이 메소드를 통해 구현합니다
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them

        for (appWidgetId in appWidgetIds) {
            // 위젯 클릭시 MainAcitivty 실행

            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    // 앱 위젯은 여러개가 등록 될 수 있는데, 최초의 앱 위젯이 등록 될 때 호출 됩니다. (각 앱 위젯 인스턴스가 등록 될때마다 호출 되는 것이 아님)
    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    // onEnabled() 와는 반대로 마지막의 최종 앱 위젯 인스턴스가 삭제 될 때 호출 됩니다
    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java).let { intent ->
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }

    val views: RemoteViews = RemoteViews(context.packageName, R.layout.app_widget).apply {
        setOnClickPendingIntent(R.id.appwidget_text, pendingIntent)

        // 이미지뷰
        setInt(R.id.appwidget_imageview,"setColorFilter",ContextCompat.getColor(context, R.color.white))
    }

    // 텍스트뷰
    val widgetText = "할일작성"
    views.setTextViewText(R.id.appwidget_text, widgetText)
    views.setImageViewResource(R.id.appwidget_imageview, R.drawable.ic_create)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}