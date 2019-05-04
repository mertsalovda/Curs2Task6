package com.example.curs2task6;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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

    EditText etURI;
    Button btnDownload;
    ImageView ivImage;
    Button btnSetImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etURI = findViewById(R.id.et_uriImage);
        btnDownload = findViewById(R.id.btn_download);
        ivImage = findViewById(R.id.iv_image);
        btnSetImage = findViewById(R.id.btn_set_image);

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Начать загрузку
                String textURI = etURI.getText().toString();
                startDownload(textURI);
            }
        });

        btnSetImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Поменять картинку в ivImage

            }
        });

        if (Build.VERSION.SDK_INT >= 23) {
            //запросить разрешение записи во внешнюю память, если sdk_int >= 23
        }

    }

    private void startDownload(String textURI) {
        //Проверка текста на соответствие условиям
        if (isValidText(textURI)) {
            Toast.makeText(this, "download", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidText(String text) {

        if (TextUtils.isEmpty(text)){
            showMessage("Text is empty");
            return false;
        }
        if (!URLUtil.isValidUrl(text)){
            showMessage("Text is not URL");
            return false;
        }
        //Если не содержит на конце формат изображения
        if (!text.matches("(.*).jpeg")
                && !text.matches("(.*).png")
                && !text.matches("(.*).bmp")) {
            showMessage("Is not URL on Image");
            return false;
        }

        return true;
    }

    private void showMessage(String textMessage){
        Toast.makeText(this, textMessage, Toast.LENGTH_SHORT).show();
    }
}
