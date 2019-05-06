package com.example.curs2task6;

import android.Manifest;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int WRITE_PERMISSION_RC = 100;

// ++2) При запуске на Android 6+ - происходит запрос Runtime Permission на запись файлов.
// (WRITE_EXTERNAL_STORAGE)
//
// ++3) При нажатии на кнопку 1 должна происходить загрузка файла, url которого указан в EditText.
//
// ++4) Сохранение файла должно производиться в папку Downloads устройства.
// ++5) Приложение должно качать только изображения.
//    Если ссылка не оканчивается на .jpeg/.png/.bmp или ее вообще нет,
//    то при нажатии на кнопку 1 выводить тост с текстом ошибки
//    (на усмотрение исполнителя).
//
// ++6) После окончания загрузки, кнопка 2 становится активной (setEnabled(true))
//    При нажатии на эту кнопку, скачанное изображение загружается в ImageView.
//

    private EditText etURI;
    private Button btnDownload;
    private ImageView ivImage;
    private Button btnSetImage;
    private DownloadManager downloadManager;
    private Uri downloadUri;
    private long refid;
    private List<Long> listRefid = new ArrayList<>();
    private String lastDawnloadFileName;
    private long referenceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etURI = findViewById(R.id.et_uriImage);
        btnDownload = findViewById(R.id.btn_download);
        ivImage = findViewById(R.id.iv_image);
        btnSetImage = findViewById(R.id.btn_set_image);

        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

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
                String path = "/sdcard/Download/" + lastDawnloadFileName;
                File imageFile = new File(path);
                if (imageFile.exists()) {
                    ivImage.setImageDrawable(Drawable.createFromPath(path));
                } else {
                    showMessage("Ошибка: данного файла не существует!");
                }
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

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
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
        btnSetImage.setEnabled(false);

        listRefid.clear();

        showMessage("Загрузка: " + "/sdcard/Download/" + getFileName(textURI));
        downloadUri = Uri.parse(textURI);
        String fileName = getFileName(textURI);

        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
                | DownloadManager.Request.NETWORK_MOBILE);
        request.setAllowedOverRoaming(false);
        request.setTitle("Скачивание файла");
        request.setDescription("Description");
        request.setVisibleInDownloadsUi(true);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                "/" + fileName);

        refid = downloadManager.enqueue(request);
        listRefid.add(refid);
    }

    //Получить имя файла ссылке на его
    private String getFileName(String textURI) {
        String fileName;
        int lastIndexPoint = textURI.lastIndexOf("/");
        fileName = textURI.substring(lastIndexPoint + 1, textURI.length());
        return fileName;
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
                && !text.matches("(.*).jpg") //тоже самое, что и .jpeg
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

    BroadcastReceiver onComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            listRefid.remove(referenceId);

            if (listRefid.isEmpty()) {
                NotificationCompat.Builder builder
                        = new NotificationCompat.Builder(MainActivity.this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Загрузка изображения")
                        .setContentText("Загрузка завершена");

                NotificationManager notificationManager
                        = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(455, builder.build());

                //Если скачивание не удалось, кнопка не станет активной
                if (!isDownloadSuccessful()) {
                    btnSetImage.setEnabled(true);
                    lastDawnloadFileName = getFileName(etURI.getText().toString());
                }

            }
        }
    };

    //Проверка на успешное скачивание
    private boolean isDownloadSuccessful() {
        Cursor cursor = downloadManager.query(new DownloadManager.Query()
                .setFilterById(referenceId));
        cursor.moveToFirst();

        int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));

        if (status != DownloadManager.STATUS_SUCCESSFUL) {
            return true;
        }

        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onComplete);
    }
}
