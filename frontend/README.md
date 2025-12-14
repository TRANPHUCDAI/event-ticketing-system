# Frontend - Hệ thống bán vé sự kiện

Giao diện web đơn giản để quản lý bán vé sự kiện.

## 📋 Tính năng

- ✅ Trang chủ với thống kê
- ✅ Danh sách sự kiện
- ✅ Đặt vé sự kiện
- ✅ Báo cáo doanh thu
- ✅ Responsive design (hoạt động trên điện thoại)

## 🚀 Cách chạy

### Cách 1: Dùng Python (đơn giản nhất)

```bash
cd frontend
python3 -m http.server 3000
```

Sau đó mở trình duyệt: http://localhost:3000

### Cách 2: Dùng Node.js

```bash
cd frontend
npx http-server -p 3000
```

### Cách 3: Dùng Docker

```bash
docker run -p 3000:80 -v $(pwd):/usr/share/nginx/html nginx:alpine
```

Sau đó mở: http://localhost:3000

## 📁 Cấu trúc file

```
frontend/
├── index.html      # Giao diện chính
├── style.css       # CSS (thiết kế đẹp)
├── app.js          # JavaScript chính
├── api.js          # Gọi API backend
└── README.md       # File này
```

## 🔌 Kết nối Backend

API Gateway chạy trên: http://localhost:8000

Ứng dụng sẽ tự động:

- Gọi API từ backend
- Nếu API không khả dụng, sử dụng dữ liệu mẫu (mock)

## 📊 API Endpoints

Giao diện kết nối với các API:

```
GET  /api/events              - Danh sách sự kiện
POST /api/events              - Tạo sự kiện
GET  /api/seats/available/:id - Số vé trống
POST /api/seats/reserve       - Đặt vé
POST /api/payments            - Thanh toán
GET  /api/reports/summary     - Báo cáo tổng hợp
GET  /api/tickets/user/:id    - Vé của người dùng
```

## 🎨 Thiết kế

- **Màu chính**: Purple & Blue gradient
- **Font**: Segoe UI (modern)
- **Layout**: Responsive grid
- **Hiệu ứng**: Animation mượt mà

## 💾 Dữ liệu Mẫu

Nếu backend không chạy, ứng dụng sử dụng dữ liệu mẫu:

- 3 sự kiện mẫu (Taylor Swift, BTS, Coldplay)
- Thống kê mẫu
- Giá vé từ 500k-750k VNĐ

## 🔄 Quy trình đặt vé

1. Chọn sự kiện
2. Nhập số lượng vé
3. Nhập thông tin cá nhân
4. Nhấn "Đặt vé"
5. Nhận xác nhận qua email

## 📱 Responsive

- ✅ Desktop (1200px+)
- ✅ Tablet (768px - 1200px)
- ✅ Mobile (< 768px)

## ⚙️ Yêu cầu

- Trình duyệt hiện đại (Chrome, Firefox, Safari, Edge)
- JavaScript bật
- CORS enabled trên backend (tuỳ chọn)

## 🐛 Troubleshooting

### "CORS Error"

Backend cần cấu hình CORS. Thêm vào API Gateway:

```properties
spring.web.cors.allowed-origins=http://localhost:3000
spring.web.cors.allowed-methods=GET,POST,PUT,DELETE
```

### "API không hoạt động"

Ứng dụng sẽ tự động sử dụng dữ liệu mẫu. Kiểm tra:

- Backend đã chạy? `curl http://localhost:8000/api/events`
- Network tab trong DevTools

## 📝 Ghi chú

- Tất cả dữ liệu lưu trong bộ nhớ (F5 là mất dữ liệu)
- Để lưu trữ vĩnh viễn, cần backend database
- UI được thiết kế với UX trực quan cho người dùng

## 👨‍💻 Phát triển thêm

Muốn thêm tính năng:

1. Thêm HTML trong `index.html`
2. Thêm CSS trong `style.css`
3. Thêm logic trong `app.js`
4. Gọi API trong `api.js`

Đơn giản vậy! 🎉
