---------------------------------CREATE BY NGUYỄN ANH SƠN -------------------------------------

ỨNG DỤNG ANDROID ĐỌC BIỂN SỐ XE, TRA CỨU PHẠT NGUỘI, GỬI THÔNG TIN QUA BLUETOOTH

Ứng dụng Android này tích hợp nhiều tính năng nhằm cung cấp giải pháp toàn diện cho việc giám sát và điều khiển xe.
Dự án sử dụng các công nghệ tiên tiến như CameraX, ML Kit OCR, Speech Recognition và Bluetooth để thực hiện các chức năng chính sau:

Chức năng chính

•	**Chụp và Nhận diện Biển số Xe:** Sử dụng CameraX để hiển thị live preview và chụp ảnh, sau đó ML Kit OCR được áp dụng để nhận diện biển số xe một cách chính xác theo tỷ lệ chuẩn 16:9.

•	**Ghi Âm và Chuyển Đổi Giọng nói:** Tích hợp Android SpeechRecognizer cho phép ghi âm và chuyển đổi giọng nói thành văn bản, hiển thị kết quả theo thời gian thực.

•	**Kết nối Bluetooth:** Ứng dụng gửi dữ liệu, bao gồm kết quả ghi âm và thông tin tra phạt nguội, qua Bluetooth tới các thiết bị ngoại vi nhằm tự động hóa quá trình xử lý thông tin.

•	**Hiệu ứng Giao diện Thân thiện:** Giao diện được xây dựng bằng ConstraintLayout với các guideline giúp đảm bảo hiển thị đồng nhất trên nhiều loại màn hình. Ứng dụng còn sử dụng hiệu ứng ripple cho các nút bấm và animation loading (các dấu chấm tăng dần) khi chờ kết quả API tra phạt nguội.

Công nghệ sử dụng

- **CameraX & ML Kit OCR:** Cho phép chụp ảnh và nhận diện biển số xe với tỷ lệ 16:9 chuẩn.
  
- **Android SpeechRecognizer:** Hỗ trợ ghi âm và chuyển đổi giọng nói thành văn bản.
  
- **Retrofit & OkHttp:** Để giao tiếp với API tra phạt nguội với các thiết lập timeout phù hợp.
  
- **Bluetooth SPP:** Kết nối và truyền dữ liệu qua Bluetooth đến thiết bị ngoại vi.
  
- **ConstraintLayout:** Xây dựng giao diện linh hoạt, tương thích với nhiều loại màn hình.
  
Hướng dẫn cài đặt

Bước 1: Tạo dự án

Tạo một dự án Android mới sử dụng ngôn ngữ Java.

Bước 2: Thêm mã nguồn

Copy file **activity_main.xml** và file **MainActivity.java** vào dự án của bạn.

Lưu ý: Trong file **MainActivity.java**, chỉnh sửa package sao cho phù hợp với tên dự án hiện tại.

Bước 3: Cấu hình AndroidManifest

Mở file **AndroidManifest.xml** và thêm các quyền sau:
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" />
```
Bước 4: Cập nhật dependencies
Mở file **build.gradle.kts** (Module: app) và thêm các dòng sau vào mục dependencies:
```kotlin
implementation("androidx.core:core-ktx:1.9.0")
implementation("androidx.appcompat:appcompat:1.6.1")
implementation("com.google.android.material:material:1.8.0")

// Dependency của CameraX:
implementation("androidx.camera:camera-camera2:1.3.0")
implementation("androidx.camera:camera-lifecycle:1.3.0")
implementation("androidx.camera:camera-view:1.3.0")

implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// Dependency của ML Kit Text Recognition:
implementation("com.google.mlkit:text-recognition:16.0.0")
```
Sau đó, nhấn **Sync** để đồng bộ hệ thống.
Bước 5: Build và chạy
Build và chạy ứng dụng trên thiết bị Android hoặc emulator với API 23 trở lên.
Hướng phát triển
- Cải thiện cơ chế xử lý lỗi của API và giao diện phản hồi.
- Tích hợp thêm các tính năng giám sát và phân tích dữ liệu xe theo thời gian thực.
- Tối ưu hóa giao diện người dùng và trải nghiệm tương tác.
  
Liên hệ

Các đóng góp, báo lỗi và đề xuất cải tiến luôn được hoan nghênh. Mọi thắc mắc và góp ý xin liên hệ qua email: **nguyenanhson10042003@gmail.com**

Follow me to do this project

**Bước 1:** Tạo một dự án Android mới với ngôn ngữ Java.

**Bước 2:** Copy file **activity_main.xml** và file **MainActivity.java** vào dự án.

> Lưu ý: Trong file **MainActivity.java**, chỉnh sửa package theo tên dự án hiện tại.
> 
> ![Chỉnh sửa package](https://github.com/user-attachments/assets/58904ff0-fad7-477d-8b61-0769e458e1b8)
> 
**Bước 3:** Mở file **AndroidManifest.xml** và thêm các quyền như đã hướng dẫn ở Bước 3.

**Bước 4:** Mở file **build.gradle.kts** (Module: app), thêm các dependencies cần thiết, sau đó nhấn **Sync**.

**Bước 5:** Build và chạy ứng dụng để xem kết quả.

