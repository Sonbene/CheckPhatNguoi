ỨNG DỤNG ANDROID ĐỌC BIỂN SỐ XE, TRA CỨU PHẠT NGUỘI, GỬI THÔNG TIN QUA BLUETOOH


Đây là một ứng dụng Android tích hợp nhiều tính năng, cung cấp giải pháp toàn diện trong việc giám sát và điều khiển xe. Ứng dụng sử dụng các công nghệ tiên tiến như CameraX, ML Kit OCR, Speech Recognition và Bluetooth để hỗ trợ các chức năng chính sau:

Chụp và Nhận diện Biển số Xe:
Sử dụng CameraX để hiển thị live preview và chụp ảnh, sau đó ML Kit OCR được sử dụng để nhận diện biển số xe một cách chính xác.

Ghi Âm và Chuyển Đổi Giọng nói:
Tích hợp Android SpeechRecognizer cho phép ghi âm và chuyển đổi giọng nói thành văn bản, hiển thị kết quả theo thời gian thực.

Kết nối Bluetooth:
Ứng dụng gửi dữ liệu (bao gồm kết quả ghi âm và thông tin tra phạt nguội) qua Bluetooth tới các thiết bị ngoại vi, giúp tự động hóa quá trình xử lý thông tin.

Hiệu ứng Giao diện Thân thiện:
Giao diện được thiết kế sử dụng ConstraintLayout với các guideline, hỗ trợ hiển thị đồng nhất trên nhiều kích thước màn hình, kèm theo hiệu ứng ripple cho các nút bấm và animation loading hiện các dấu chấm khi chờ API tra phạt nguội.

Công nghệ sử dụng: 

CameraX & ML Kit OCR: Cho phép chụp ảnh và nhận diện biển số xe với tỷ lệ 16:9 chuẩn.
Android SpeechRecognizer: Hỗ trợ ghi âm và chuyển đổi giọng nói thành văn bản.
Retrofit & OkHttp: Để giao tiếp với API tra phạt nguội, sử dụng các thiết lập timeout phù hợp.
Bluetooth SPP: Kết nối và truyền dữ liệu qua Bluetooth đến thiết bị ngoại vi.
ConstraintLayout: Xây dựng giao diện linh hoạt, tương thích với nhiều loại màn hình.
Cách cài đặt
Mở git bash và gõ câu lệnh: git clone https://github.com/your-username/your-repo.git
Mở dự án trong Android Studio.

Cài đặt các quyền cần thiết:
Đảm bảo thiết bị có quyền sử dụng CAMERA, RECORD_AUDIO, BLUETOOTH và các quyền liên quan.

Build và chạy ứng dụng trên thiết bị Android hoặc emulator với API 23 trở lên.

Hướng phát triển:
Cải thiện cơ chế xử lý lỗi của API và giao diện phản hồi.
Tích hợp thêm các tính năng giám sát, phân tích dữ liệu xe theo thời gian thực.
Tối ưu hóa giao diện người dùng và trải nghiệm tương tác.

Liên hệ:
Đóng góp, báo lỗi và đề xuất cải tiến luôn được hoan nghênh. Mọi thắc mắc, góp ý xin liên hệ qua email: nguyenanhson10042003@gmail.com


Follow me to do this project:

Bước 1: Tạo một dự án android mơi với ngôn ngữ java
Bước 2: Copy file activity_main.xml và file MainActivity.java vào project của bạn
!!Lưu ý: đối với file MainActivity.java bạn phải sửa package là package theo tên project của bạn:
![image](https://github.com/user-attachments/assets/58904ff0-fad7-477d-8b61-0769e458e1b8) 

Bước 3: Mở file AndroidManifest copy các quyền sau và dán vào:
<!-- Quyền ghi âm -->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />

    <!-- Quyền Bluetooth -->
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />

    <!-- Với Android 12+ -->
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
    <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />

    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.RECORD_AUDIO"/>

    <uses-permission android:name="android.permission.CAMERA" />
    <uses-feature android:name="android.hardware.camera" />
    <uses-permission android:name="android.permission.CAMERA" />

    <uses-permission android:name="android.permission.INTERNET" />

    example:
![image](https://github.com/user-attachments/assets/26cf8cdf-0438-47cf-9655-1205136cefe5)

Bước 4: Mở file buld.gradle.kts (Module:app), thêm các dòng sau vao mục dependencies:

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")

    // Thêm các dependency cần thiết của CameraX:
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    // Thêm dependency của ML Kit Text Recognition:
    implementation("com.google.mlkit:text-recognition:16.0.0")

  

![image](https://github.com/user-attachments/assets/a6b35444-2daf-4c1c-b446-ee4df64a95bb)

Sau đó bấn sync đề đồng bộ lại hệ thống.

Bước 6: Chạy project và xem kết quả.






