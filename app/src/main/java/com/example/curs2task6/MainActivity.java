package com.example.curs2task6;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

// 2) При запуске на Android 6+ - происходит запрос Runtime Permission на запись файлов.
// (WRITE_EXTERNAL_STORAGE)
//
// 3) При нажатии на кнопку 1 должна происходить загрузка файла, url которого указан в EditText.
//
// 4) Сохранение файла должно производиться в папку Downloads устройства.
// 5) Приложение должно качать только изображения.
//    Если ссылка не оканчивается на .jpeg/.png/.bmp или ее вообще нет,
//    то при нажатии на кнопку 1 выводить тост с текстом ошибки
//    (на усмотрение исполнителя).
//
// 6) После окончания загрузки, кнопка 2 становится активной (setEnabled(true))
//    При нажатии на эту кнопку, скачанное изображение загружается в ImageView.
//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
