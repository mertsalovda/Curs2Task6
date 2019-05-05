package com.example.curs2task6;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
    public static final int WRITE_PERMISSION_RC = 100;

// ++2) При запуске на Android 6+ - происходит запрос Runtime Permission на запись файлов.
// (WRITE_EXTERNAL_STORAGE)
//
// 3) При нажатии на кнопку 1 должна происходить загрузка файла, url которого указан в EditText.
//
// 4) Сохранение файла должно производиться в папку Downloads устройства.
// ++5) Приложение должно качать только изображения.
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
                downloadToFileIfValid(textURI);
            }
        });

        btnSetImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Поменять картинку в ivImage

            }
        });
        //запросить разрешение записи во внешнюю память, если sdk_int >= 23
        //и разрешение на запись не было дано
        if (Build.VERSION.SDK_INT >= 23
                && !isWritePermissionGranted()) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_PERMISSION_RC);
        }

    }

    //Загрузка файла
    private void downloadToFileIfValid(String textURI) {
        //Проверка текста на соответствие условиям
        if (isValidText(textURI)) {
            downloadInToFileWithPermissionRequestIfNeeded(textURI);
        }
    }

    //Скачать файл, если есть разрешение на запись
    private void downloadInToFileWithPermissionRequestIfNeeded(String textURI) {
        //Если есть разрешение записи
        if (isWritePermissionGranted()) {
            //Скачать файл
            downloadToFile(textURI);
        } else {
            //Запросить разрешение
            requestWritePermission();
        }
    }

    //Показать запрос на разрешение записи в файл
    private void requestWritePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //Показать объяснение
            new AlertDialog.Builder(this)
                    .setMessage("Без разрешения невозможно сохранить изображение в файл.")
                    .setPositiveButton("Понятно", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    WRITE_PERMISSION_RC);
                        }
                    }).show();
        } else {
            //Иначе запросить разрешение
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_PERMISSION_RC);
        }
    }

    //Закгрузка файла
    private void downloadToFile(String textURI) {
        showMessage("Загрузка: " + textURI);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != WRITE_PERMISSION_RC) return;

        if (grantResults.length != 1) return;

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            String textURI = etURI.getText().toString();
            if (!TextUtils.isEmpty(textURI)) { //Не войдёт при первом запуске
                downloadToFile(textURI);
            }
        } else {
            //Если пользователь не дал разрешения, мы ему сообщаем, что он всегда может сделтаь это
            //в настройках приложения
            new AlertDialog.Builder(this)
                    .setMessage("Вы можете дать разрешение в настройках устройства в любой момент.")
                    .setPositiveButton("Понятно", null).show();
        }
    }

    //Проверка разрешения на запись в файл
    private boolean isWritePermissionGranted() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    //Проверка ссылки
    private boolean isValidText(String text) {
        //Если текст пустой
        if (TextUtils.isEmpty(text)) {
            showMessage("Пустая ссылка на файл");
            return false;
        }
        //Если не формат URL
        if (!URLUtil.isValidUrl(text)) {
            showMessage("Текст не является ссылкой");
            return false;
        }
        //Если не содержит на конце формат изображения
        if (!text.matches("(.*).jpeg")
                && !text.matches("(.*).png")
                && !text.matches("(.*).bmp")) {
            showMessage("Ссылка должна указывать на изображение и оканчиваться " +
                    "на .jpeg/.png/.bmp");
            return false;
        }
        return true;
    }

    private void showMessage(String textMessage) {
        Toast.makeText(this, textMessage, Toast.LENGTH_LONG).show();
    }
}
