package com.example.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment

class RulesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rules, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val webView = view.findViewById<WebView>(R.id.webViewRules)

        // Важные настройки WebView
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true

        // Устанавливаем WebViewClient чтобы страница открывалась внутри WebView
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                url: String?
            ): Boolean {
                // Загружаем все ссылки внутри WebView, не открывая браузер
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                // Страница загружена
                super.onPageFinished(view, url)
            }
        }

        // HTML контент с правилами игры
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <style>
                    body {
                        font-family: 'Arial', sans-serif;
                        padding: 20px;
                        background-color: #f5f5f5;
                        color: #333;
                        line-height: 1.6;
                    }
                    h1 {
                        color: #2c3e50;
                        text-align: center;
                        border-bottom: 2px solid #3498db;
                        padding-bottom: 10px;
                        margin-bottom: 20px;
                    }
                    h2 {
                        color: #2980b9;
                        margin-top: 25px;
                        margin-bottom: 10px;
                    }
                    .section {
                        background: white;
                        padding: 15px;
                        margin: 15px 0;
                        border-radius: 8px;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                    }
                    .important {
                        background-color: #fff3cd;
                        border-left: 4px solid #ffc107;
                        padding: 12px;
                        margin: 15px 0;
                    }
                    ul {
                        padding-left: 20px;
                    }
                    li {
                        margin-bottom: 8px;
                        padding-left: 5px;
                    }
                    .bonus {
                        color: #27ae60;
                        font-weight: bold;
                    }
                    .penalty {
                        color: #e74c3c;
                        font-weight: bold;
                    }
                </style>
            </head>
            <body>
                <h1>🐜 Жуки 🐜</h1>
                
                <div class="section">
                    <h2>🎯 Цель игры</h2>
                    <p>Убивать!!!.</p>
                </div>
                
                <div class="section">
                    <h2>🎮 Управление</h2>
                    <ul>
                        <li><strong>Касание экрана</strong> - Удар</li>
                        <li><strong>Свайп влево/вправо</strong> - передвижение по карте</li>
                        <li><strong>Зажатие </strong> - активация супер-способности</li>
                    </ul>
                </div>
                
                <div class="section">
                    <h2>⭐ Бонусы</h2>
                    <ul>
                        <li><span class="bonus">⚡ Доп заряд</span> - временно увеличивает скорость набора способности</li>
                        <li><span class="bonus">🛡️ Снижение Защиты</span> - делает тараканов уязвимыми на 10 сек</li>
                        <li><span class="bonus">🌀 Телепорт</span> - дает разовый заряд телепорта на определенное растояние</li>
                        <li><span class="bonus">💰 Монеты</span> - увеличивают счет и открывают новые скины</li>
                    </ul>
                </div>
                
                <div class="section">
                     <h2>🐞 Типы жуков</h2>
                          <ul>
                                <li><span class="emoji">🐞</span><strong>Обычный жук</strong> - 1 удар для уничтожения</li>
                                <li><span class="emoji">🪲</span><strong>Танк-жук</strong> - требует 3 удара</li>
                                <li><span class="emoji">🪰</span><strong>Летучий жук</strong> - быстрый, сложная цель</li>
                                <li><span class="emoji">👑</span><strong>Королева</strong> - босс, вызывает подкрепление</li>
                                <li><span class="emoji">💣</span><strong>Взрывной жук</strong> - взрывается при смерти</li>
                          </ul>
                </div>
                
            </body>
            </html>
        """.trimIndent()


        webView.loadDataWithBaseURL(
            null,
            htmlContent,
            "text/html",
            "UTF-8",
            null
        )
    }
}