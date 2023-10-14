package com.test.dontforget.UI.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.android.volley.Response
import com.test.dontforget.MainActivity
import com.test.dontforget.MyApplication
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
        Log.d("!@#$%^&","앱 등록됐다!!!")
    }

    // onEnabled() 와는 반대로 마지막의 최종 앱 위젯 인스턴스가 삭제 될 때 호출 됩니다
    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    val views: RemoteViews = RemoteViews(context.packageName, R.layout.app_widget).apply {
        // 이미지뷰
        setImageViewResource(R.id.appwidget_imageview, R.drawable.ic_pencil)
        setOnClickPendingIntent(R.id.ll_appwidget, getIntent(context))
    }

    appWidgetManager.updateAppWidget(appWidgetId, views)
}
fun getIntent(context: Context) : PendingIntent{
    val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java).let { intent ->
//        MyApplication.isStartedWithWidget = true
//        Log.d("!@#$%^&","글쓰기로 가기!")
        PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }
    return pendingIntent
}