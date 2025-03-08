package com.example.bsxdth_211600654_nas;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class MainActivity extends AppCompatActivity {

    // ================================================================
    // REGION: BLUETOOTH & GHI ÂM (Speech Recognition)
    // ================================================================
    private static final String TAG = "MainActivity";

    // UI liên quan đến ghi âm & Bluetooth
    private Button btnRecord, btnSend, btnSelectDevice;
    private TextView tvResult;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private boolean isRecording = false;
    private String recognizedText = ""; // Nội dung ghi âm tích lũy

    // Bluetooth
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 100;
    private static final int REQUEST_PERMISSION_BT = 200;
    // UUID cho SPP (Serial Port Profile)
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // SharedPreferences lưu thiết bị mặc định
    private static final String PREFS_NAME = "BluetoothPrefs";
    private static final String KEY_DEFAULT_DEVICE = "default_device_address";
    private String defaultDeviceAddress = null;
    // ================================================================


    // ================================================================
    // REGION: CAMERA, ML Kit OCR & API tra cứu phạt nguội
    // ================================================================
    // UI liên quan đến camera và tra cứu
    private PreviewView previewView;
    private Button btnCapture, btnGetPhatNguoi, btnSendPhatNguoi;
    private TextView txtBienSoXe, txtKetQuaPhatNguoi;
    private EditText edtBienSoXe;

    // CameraX
    private ImageCapture imageCapture;
    private ProcessCameraProvider cameraProvider;

    // API
    private static final String BASE_URL = "https://api.checkphatnguoi.vn/";

    // Launcher yêu cầu quyền CAMERA
    private ActivityResultLauncher<String> permissionLauncher;
    // ================================================================


    //================================================================
    // Animation
    // ===============================================================
    private Handler loadingHandler = new Handler(Looper.getMainLooper());
    private Runnable loadingRunnable;
    private int dotCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Nếu bạn muốn sử dụng EdgeToEdge (có thể bỏ nếu không cần)
        EdgeToEdge.enable(this);
        // Dùng layout đã gộp (merged_layout.xml)
        setContentView(R.layout.activity_main);

        // ============================================================
        // Ánh xạ view cho BLUETOOTH & GHI ÂM
        // ============================================================
        btnRecord = findViewById(R.id.btnRecord);
        btnSend = findViewById(R.id.btnSend);
        btnSelectDevice = findViewById(R.id.btnSelectDevice);
        tvResult = findViewById(R.id.tvResult);

        // ============================================================
        // Ánh xạ view cho CAMERA & TRA CỨU PHẠT NGUỘI
        // ============================================================
        previewView = findViewById(R.id.previewView);
        btnCapture = findViewById(R.id.btnCapture);
        btnGetPhatNguoi = findViewById(R.id.btnGetPhatNguoi);
        btnSendPhatNguoi = findViewById(R.id.btnSendPhatNguoi);
        //txtBienSoXe = findViewById(R.id.txtBienSoXe);
        edtBienSoXe = findViewById(R.id.edtBienSoXe);
        txtKetQuaPhatNguoi = findViewById(R.id.txtKetQuaPhatNguoi);

        // ============================================================
        // KHỞI TẠO GHI ÂM (Speech Recognition)
        // ============================================================
        initSpeechRecognizer();
        handleRecordButton();

        // ============================================================
        // KHỞI TẠO BLUETOOTH
        // ============================================================
        initBluetooth();

        // Sự kiện chọn thiết bị Bluetooth
        btnSelectDevice.setOnClickListener(view -> selectDefaultBluetoothDevice());

        // Sự kiện gửi nội dung ghi âm qua Bluetooth
        btnSend.setOnClickListener(view -> sendRecognizedText());

        // ============================================================
        // KHỞI TẠO CAMERA (CameraX) và yêu cầu quyền CAMERA
        // ============================================================
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        startCamera();
                    } else {
                        Toast.makeText(MainActivity.this, "Quyền sử dụng Camera cần được cấp", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }

        // Sự kiện chụp ảnh và xử lý nhận dạng biển số
        btnCapture.setOnClickListener(view -> capturePhotoAndFreeze());

        // Sự kiện gọi API tra cứu phạt nguội từ biển số (nhập bằng tay hoặc tự động sau nhận dạng)
        btnGetPhatNguoi.setOnClickListener(v -> {
            String plate = edtBienSoXe.getText().toString().trim();
            if (plate.isEmpty()) {
                Toast.makeText(MainActivity.this, "Vui lòng nhập biển số xe", Toast.LENGTH_SHORT).show();
                return;
            }
            callTrafficFineAPI(plate);
        });

        btnSendPhatNguoi.setOnClickListener(v -> {
            String txtPhatNguoi = txtKetQuaPhatNguoi.getText().toString().trim();

            if (txtPhatNguoi.equals("Kết quả tra phạt nguội")) {
                // Nếu nội dung chính xác là "Kết quả tra phạt nguội"
                Toast.makeText(MainActivity.this, "Không có kết quả phạt nguội để gửi", Toast.LENGTH_SHORT).show();
            } else if (txtPhatNguoi.startsWith("Lỗi API:")) {
                // Nếu nội dung bắt đầu bằng "Lỗi API:"
                Toast.makeText(MainActivity.this, "Có lỗi API trong nội dung!", Toast.LENGTH_SHORT).show();
            } else {
                // Trường hợp khác
                sendTextViaBluetooth(txtPhatNguoi);
                //Toast.makeText(MainActivity.this, "Nội dung: " + txtPhatNguoi, Toast.LENGTH_SHORT).show();
            }
        });


        // (Tuỳ chọn) Cấu hình insets cho root view nếu có ID "main" hoặc view tương tự
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // ================================================================
    // REGION: PHƯƠNG THỨC GHI ÂM
    // ================================================================

    // Khởi tạo SpeechRecognizer (chỉ phần liên quan)
    private void initSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech recognition không khả dụng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        // Các extra hỗ trợ nhận dạng đoạn nói dài
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 10000);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000);
        // Cho phép trả về kết quả tạm thời
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        speechRecognizer.setRecognitionListener(new android.speech.RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) { }
            @Override public void onBeginningOfSpeech() { }
            @Override public void onRmsChanged(float rmsdB) { }
            @Override public void onBufferReceived(byte[] buffer) { }
            @Override public void onEndOfSpeech() { }
            @Override
            public void onError(int error) {
                Log.e(TAG, "Speech onError: " + error);
                if (error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT && isRecording) {
                    speechRecognizer.startListening(speechRecognizerIntent);
                } else {
                    Toast.makeText(MainActivity.this, "Lỗi ghi âm: " + error, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    // Nối kết quả hoàn chỉnh vào recognizedText
                    recognizedText += matches.get(0) + " ";
                    tvResult.setText(recognizedText);
                    // Nếu vẫn đang ghi âm thì tiếp tục nghe
                    if (isRecording) {
                        speechRecognizer.startListening(speechRecognizerIntent);
                    }
                }
            }
            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> partial = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (partial != null && !partial.isEmpty()) {
                    // Hiển thị tạm thời là nội dung đã ghi nhận (recognizedText)
                    // cộng với phần tạm thời mới nhất
                    tvResult.setText(recognizedText + partial.get(0));
                }
            }
            @Override public void onEvent(int eventType, Bundle params) { }
        });
    }

    private void handleRecordButton() {
        btnRecord.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isRecording = true;
                    // Reset nội dung cho phiên mới
                    recognizedText = "";
                    tvResult.setText("");
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.RECORD_AUDIO},
                                REQUEST_RECORD_AUDIO_PERMISSION);
                        return false;
                    }
                    speechRecognizer.startListening(speechRecognizerIntent);
                    btnRecord.setText("Đang ghi âm...");
                    break;
                case MotionEvent.ACTION_UP:
                    isRecording = false;
                    speechRecognizer.stopListening();
                    btnRecord.setText("Giữ để ghi âm");
                    // Không cần cập nhật recognizedText lại từ tvResult vì đã được cập nhật trong onPartialResults/onResults
                    break;
            }
            return true;
        });

    }

    // ================================================================
    // REGION: PHƯƠNG THỨC  BLUETOOTH
    // ================================================================



    @SuppressLint("MissingPermission")
    private void initBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Thiết bị không hỗ trợ Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        defaultDeviceAddress = prefs.getString(KEY_DEFAULT_DEVICE, null);
        if (defaultDeviceAddress != null) {
            try {
                BluetoothDevice defaultDevice = bluetoothAdapter.getRemoteDevice(defaultDeviceAddress);
                btnSelectDevice.setText("Thiết bị: " + defaultDevice.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void selectDefaultBluetoothDevice() {
        if (bluetoothAdapter == null) {
            Toast.makeText(MainActivity.this, "Thiết bị không hỗ trợ Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(MainActivity.this, "Bluetooth chưa bật, đang bật...", Toast.LENGTH_SHORT).show();
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices == null || pairedDevices.isEmpty()) {
            Toast.makeText(MainActivity.this, "Không có thiết bị đã ghép nối", Toast.LENGTH_SHORT).show();
            return;
        }
        final BluetoothDevice[] devices = pairedDevices.toArray(new BluetoothDevice[0]);
        String[] deviceNames = new String[devices.length];
        for (int i = 0; i < devices.length; i++) {
            deviceNames[i] = devices[i].getName() + " - " + devices[i].getAddress();
        }
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Chọn thiết bị mặc định")
                .setItems(deviceNames, (dialog, which) -> {
                    defaultDeviceAddress = devices[which].getAddress();
                    SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString(KEY_DEFAULT_DEVICE, defaultDeviceAddress);
                    editor.apply();
                    btnSelectDevice.setText("Thiết bị đã chọn: " + devices[which].getName());
                    Toast.makeText(MainActivity.this, "Đã chọn: " + devices[which].getName(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void sendRecognizedText() {
        // Lấy nội dung từ tvResult trước khi kiểm tra
        recognizedText = tvResult.getText().toString();

        if (recognizedText == null || recognizedText.trim().isEmpty()) {
            Toast.makeText(MainActivity.this, "Chưa có nội dung ghi âm để gửi", Toast.LENGTH_SHORT).show();
            return;
        }
        if (bluetoothAdapter == null) {
            Toast.makeText(MainActivity.this, "Thiết bị không hỗ trợ Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(MainActivity.this, "Bluetooth chưa bật, đang bật...", Toast.LENGTH_SHORT).show();
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        REQUEST_PERMISSION_BT);
                return;
            }
        }
        if (defaultDeviceAddress != null) {
            BluetoothDevice defaultDevice = bluetoothAdapter.getRemoteDevice(defaultDeviceAddress);
            connectToDevice(defaultDevice);
        } else {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices == null || pairedDevices.isEmpty()) {
                Toast.makeText(MainActivity.this, "Không có thiết bị đã ghép nối", Toast.LENGTH_SHORT).show();
                return;
            }
            final BluetoothDevice[] devices = pairedDevices.toArray(new BluetoothDevice[0]);
            String[] deviceNames = new String[devices.length];
            for (int i = 0; i < devices.length; i++) {
                deviceNames[i] = devices[i].getName() + " - " + devices[i].getAddress();
            }
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Chọn thiết bị gửi đến")
                    .setItems(deviceNames, (dialog, which) -> {
                        defaultDeviceAddress = devices[which].getAddress();
                        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                        editor.putString(KEY_DEFAULT_DEVICE, defaultDeviceAddress);
                        editor.apply();
                        btnSelectDevice.setText("Thiết bị đã chọn: " + devices[which].getName());
                        connectToDevice(devices[which]);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        }
    }


    private void connectToDevice(final BluetoothDevice device) {
        new Thread(() -> {
            BluetoothSocket tmpSocket = null;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT)
                            != PackageManager.PERMISSION_GRANTED) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Chưa có quyền Bluetooth Connect", Toast.LENGTH_SHORT).show());
                        return;
                    }
                }
                tmpSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                try {
                    bluetoothAdapter.cancelDiscovery();
                } catch (SecurityException se) {
                    Log.e(TAG, "cancelDiscovery error: " + se.getMessage());
                }
                tmpSocket.connect();
            } catch (IOException e) {
                Log.e(TAG, "Kết nối mặc định thất bại: " + e.getMessage());
                try {
                    Method method = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                    tmpSocket = (BluetoothSocket) method.invoke(device, 1);
                    try {
                        bluetoothAdapter.cancelDiscovery();
                    } catch (SecurityException se) {
                        Log.e(TAG, "cancelDiscovery (fallback) error: " + se.getMessage());
                    }
                    tmpSocket.connect();
                } catch (Exception e2) {
                    Log.e(TAG, "Fallback kết nối thất bại: " + e2.getMessage());
                    tmpSocket = null;
                }
            }
            boolean connectionSuccessful = false;
            if (tmpSocket != null) {
                try {
                    OutputStream os = tmpSocket.getOutputStream();
                    connectionSuccessful = true;
                } catch (IOException ex) {
                    Log.e(TAG, "Lỗi khi lấy OutputStream: " + ex.getMessage());
                }
            }
            if (connectionSuccessful) {
                bluetoothSocket = tmpSocket;
                runOnUiThread(() -> sendTextViaBluetooth(recognizedText));
            } else {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Kết nối đến " + device.getName() + " thất bại", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void sendTextViaBluetooth(String text) {
        if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
            try {
                OutputStream outputStream = bluetoothSocket.getOutputStream();
                byte[] bytes = text.getBytes();
                int chunkSize = 100; // ví dụ, gửi 100 bytes mỗi lần
                for (int i = 0; i < bytes.length; i += chunkSize) {
                    int length = Math.min(chunkSize, bytes.length - i);
                    outputStream.write(bytes, i, length);
                    outputStream.flush();
                    // Tạm dừng giữa các lần gửi (có thể điều chỉnh thời gian)
                    Thread.sleep(50);
                }
                Toast.makeText(MainActivity.this, "Đã gửi: " + text, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Gửi thất bại: " + e.getMessage());
                Toast.makeText(MainActivity.this, "Gửi thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "Chưa kết nối đến thiết bị Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    // ================================================================


    // ================================================================
    // REGION: CAMERA, ML KIT OCR & CALL API
    // ================================================================
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder().build();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Lỗi khởi tạo camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "startCamera error", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void capturePhotoAndFreeze() {
        // Nếu đang ở trạng thái "Chụp lại" => khôi phục camera
        if (btnCapture.getText().toString().equals("Chụp lại")) {
            btnCapture.setText("Chụp");
            //txtBienSoXe .setText("");
            resumeCamera();
            return;
        }
        if (imageCapture == null) {
            Toast.makeText(this, "ImageCapture chưa khởi tạo", Toast.LENGTH_SHORT).show();
            return;
        }
        File photoFile = new File(getCacheDir(), "captured_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        runOnUiThread(() -> {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = 2;
                            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);
                            if (bitmap != null) {
                                if (cameraProvider != null) {
                                    cameraProvider.unbindAll();
                                }
                                // Hiển thị ảnh tạm (overlay)
                                previewView.setForeground(new BitmapDrawable(getResources(), bitmap));
                                if (btnCapture.getText().toString().equals("Chụp")) {
                                    btnCapture.setText("Chụp lại");
                                }
                                processCapturedImage(bitmap);
                            } else {
                                Toast.makeText(MainActivity.this, "Không thể hiển thị ảnh", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        runOnUiThread(() ->
                                Toast.makeText(MainActivity.this, "Chụp ảnh thất bại: " + exception.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }
                }
        );
    }

    private void resumeCamera() {
        previewView.setForeground(null);
        startCamera();
        btnCapture.setEnabled(true);
    }

    private void processCapturedImage(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    String rawText = visionText.getText();
                    Log.d("MLKit", "Recognized text: " + rawText);
                    String cleanedText = rawText.replaceAll("[\\s-]+", "").trim().toUpperCase();
                    Log.d("MLKit", "Cleaned text: " + cleanedText);

                    // Regex mẫu cho biển số: 2 chữ số, 1-2 chữ cái, 4-5 chữ số
                    String regex = "\\d{2}[A-Z]{1,2}\\d{4,5}";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(cleanedText);
                    if (matcher.find()) {
                        String licensePlate = matcher.group();
                        Toast.makeText(MainActivity.this, "Biển số: " + licensePlate, Toast.LENGTH_LONG).show();
                        //txtBienSoXe.setText(licensePlate);
                        edtBienSoXe.setText(licensePlate);
                        // Tự động gọi API với biển số vừa nhận dạng
                        //callTrafficFineAPI(licensePlate);
                    } else {
                        Toast.makeText(MainActivity.this, "Không tìm thấy biển số xe", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Lỗi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("MLKit", "Text recognition error", e);
                });
    }

    private void callTrafficFineAPI(String licensePlate) {
        // Bắt đầu animation loading trước khi gọi API
        startLoadingAnimation();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TrafficFineApi api = retrofit.create(TrafficFineApi.class);
        PlateRequest request = new PlateRequest(licensePlate);
        api.getViolations(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                // Dừng animation khi có kết quả trả về
                stopLoadingAnimation();
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.getStatus() == 1) {
                        StringBuilder result = new StringBuilder();
                        List<Violation> violations = apiResponse.getData();
                        if (violations != null && !violations.isEmpty()) {
                            for (Violation v : violations) {
                                result.append(v.toString()).append("\n\n");
                            }
                        } else {
                            result.append("Không có vi phạm phạt nguội.");
                        }
                        DataInfo dataInfo = apiResponse.getDataInfo();
                        if (dataInfo != null) {
                            result.append("\nTổng số: ").append(dataInfo.getTotal())
                                    .append("\nChưa xử phạt: ").append(dataInfo.getChuaxuphat())
                                    .append("\nĐã xử phạt: ").append(dataInfo.getDaxuphat())
                                    .append("\nLatest: ").append(dataInfo.getLatest());
                        }
                        txtKetQuaPhatNguoi.setText(result.toString());
                    } else {
                        //txtKetQuaPhatNguoi.setText("Lỗi API: " + apiResponse.getMsg());
                        txtKetQuaPhatNguoi.setText("Không có lỗi phạt nguội");
                        Toast.makeText(MainActivity.this, "Lỗi API: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Lỗi API: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                stopLoadingAnimation();
                Toast.makeText(MainActivity.this, "Lỗi kết nối API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ================================================================


    // ================================================================
    // REGION: RETROFIT API INTERFACE & MODEL CLASSES
    // ================================================================
    public interface TrafficFineApi {
        @POST("phatnguoi")
        Call<ApiResponse> getViolations(@Body PlateRequest request);
    }

    public class PlateRequest {
        private String bienso;
        public PlateRequest(String bienso) {
            this.bienso = bienso;
        }
        public String getBienso() {
            return bienso;
        }
        public void setBienso(String bienso) {
            this.bienso = bienso;
        }
    }

    public class ApiResponse {
        private int status;
        private String msg;
        private List<Violation> data;
        @SerializedName("data_info")
        private DataInfo dataInfo;
        public int getStatus() {
            return status;
        }
        public String getMsg() {
            return msg;
        }
        public List<Violation> getData() {
            return data;
        }
        public DataInfo getDataInfo() {
            return dataInfo;
        }
    }

    public class DataInfo {
        private int total;
        private int chuaxuphat;
        private int daxuphat;
        private String latest;
        public int getTotal() {
            return total;
        }
        public int getChuaxuphat() {
            return chuaxuphat;
        }
        public int getDaxuphat() {
            return daxuphat;
        }
        public String getLatest() {
            return latest;
        }
    }

    public class Violation {
        @SerializedName("Biển kiểm soát")
        private String bienKiemSoat;
        @SerializedName("Màu biển")
        private String mauBien;
        @SerializedName("Loại phương tiện")
        private String loaiPhuongTien;
        @SerializedName("Thời gian vi phạm")
        private String thoiGianViPham;
        @SerializedName("Địa điểm vi phạm")
        private String diaDiemViPham;
        @SerializedName("Hành vi vi phạm")
        private String hanhViViPham;
        @SerializedName("Trạng thái")
        private String trangThai;
        @SerializedName("Đơn vị phát hiện vi phạm")
        private String donViPhatHien;
        @SerializedName("Nơi giải quyết vụ việc")
        private List<String> noiGiaiQuyet;
        public String getBienKiemSoat() {
            return bienKiemSoat;
        }
        public String getMauBien() {
            return mauBien;
        }
        public String getLoaiPhuongTien() {
            return loaiPhuongTien;
        }
        public String getThoiGianViPham() {
            return thoiGianViPham;
        }
        public String getDiaDiemViPham() {
            return diaDiemViPham;
        }
        public String getHanhViViPham() {
            return hanhViViPham;
        }
        public String getTrangThai() {
            return trangThai;
        }
        public String getDonViPhatHien() {
            return donViPhatHien;
        }
        public List<String> getNoiGiaiQuyet() {
            return noiGiaiQuyet;
        }
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Biển kiểm soát: ").append(bienKiemSoat).append("\n")
                    .append("Màu biển: ").append(mauBien).append("\n")
                    .append("Loại phương tiện: ").append(loaiPhuongTien).append("\n")
                    .append("Thời gian vi phạm: ").append(thoiGianViPham).append("\n")
                    .append("Địa điểm vi phạm: ").append(diaDiemViPham).append("\n")
                    .append("Hành vi vi phạm: ").append(hanhViViPham).append("\n")
                    .append("Trạng thái: ").append(trangThai).append("\n")
                    .append("Đơn vị phát hiện vi phạm: ").append(donViPhatHien).append("\n")
                    .append("Nơi giải quyết vụ việc: ");
            if (noiGiaiQuyet != null && !noiGiaiQuyet.isEmpty()) {
                for (String s : noiGiaiQuyet) {
                    sb.append("\n   - ").append(s);
                }
            }
            return sb.toString();
        }
    }
    // ================================================================


    // ================================================================
    // REGION: VÒNG ĐỜI ACTIVITY
    // ================================================================
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    // ================================================================


    // ================================================================
    // REGION: ANIMATION
    // ================================================================
    private void startLoadingAnimation() {
        dotCount = 0;
        loadingRunnable = new Runnable() {
            @Override
            public void run() {
                // Tạo chuỗi dấu chấm, tối đa 10 dấu
                StringBuilder dots = new StringBuilder();
                for (int i = 0; i < dotCount; i++) {
                    dots.append(".");
                }
                txtKetQuaPhatNguoi.setText("Đang tra phạt nguội" + dots.toString());
                dotCount++;
                if (dotCount > 10) {
                    dotCount = 0;
                }
                loadingHandler.postDelayed(this, 500); // cập nhật mỗi 500ms (có thể điều chỉnh)
            }
        };
        loadingHandler.post(loadingRunnable);
    }

    private void stopLoadingAnimation() {
        if (loadingRunnable != null) {
            loadingHandler.removeCallbacks(loadingRunnable);
        }
    }


    // =============================================================

}
