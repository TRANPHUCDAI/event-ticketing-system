// ============= Main Application Logic =============

let currentUser = {
    id: 'user_' + Math.random().toString(36).substr(2, 9),
    name: '',
    email: '',
    phone: ''
};

let events = [];
let bookings = [];

// Initialize app when page loads
document.addEventListener('DOMContentLoaded', () => {
    loadData();
    showPage('home');
});

// ============= Page Navigation =============

function showPage(pageName) {
    // Hide all pages
    const pages = document.querySelectorAll('.page');
    pages.forEach(page => page.style.display = 'none');

    // Show selected page
    if (pageName === 'bookings') {
        // Show both booking form and history
        document.getElementById('bookings').style.display = 'block';
    } else {
        const page = document.getElementById(pageName);
        if (page) page.style.display = 'block';
    }

    // Load page-specific data
    if (pageName === 'events') {
        loadEvents();
    } else if (pageName === 'bookings') {
        loadEventOptions();
    } else if (pageName === 'reports') {
        loadReports();
    }
}

// ============= Data Loading =============

function loadData() {
    // Load mock events as fallback
    events = mockEvents;
    updateStats();
    loadEventOptions();
}

async function loadEvents() {
    try {
        // Try to fetch from API, fall back to mock data
        const apiEvents = await fetchEvents();
        events = apiEvents.length > 0 ? apiEvents : mockEvents;
    } catch (error) {
        console.log('Using mock data:', error);
        events = mockEvents;
    }

    const eventsList = document.getElementById('eventsList');
    eventsList.innerHTML = '';

    events.forEach(event => {
        const card = document.createElement('div');
        card.className = 'event-card';
        card.innerHTML = `
            <h3>${event.name}</h3>
            <p><strong>📍 Địa điểm:</strong> ${event.venue}</p>
            <p><strong>📅 Ngày:</strong> ${new Date(event.startTime).toLocaleDateString('vi-VN')}</p>
            <p><strong>⏰ Giờ:</strong> ${new Date(event.startTime).toLocaleTimeString('vi-VN', {hour: '2-digit', minute:'2-digit'})}</p>
            <div class="event-info">
                <span>💰 ${event.price.toLocaleString('vi-VN')} VNĐ</span>
                <span>🎫 ${event.availableSeats} vé trống</span>
            </div>
        `;
        eventsList.appendChild(card);
    });
}

function loadEventOptions() {
    const select = document.getElementById('eventSelect');
    select.innerHTML = '<option value="">-- Chọn sự kiện --</option>';

    events.forEach(event => {
        const option = document.createElement('option');
        option.value = event.id;
        option.text = `${event.name} - ${event.price.toLocaleString('vi-VN')} VNĐ`;
        select.appendChild(option);
    });
}

async function loadReports() {
    const reportsList = document.getElementById('reportsList');
    
    try {
        // Try API, fallback to mock
        const reports = await fetchReports();
        const data = reports.length > 0 ? reports : mockReports;
        
        reportsList.innerHTML = `
            <div class="report-item">
                <strong>📊 Tổng sự kiện:</strong>
                <div class="value">${data.totalEvents || 0}</div>
            </div>
            <div class="report-item">
                <strong>🎫 Vé bán được:</strong>
                <div class="value">${(data.totalTicketsSold || 0).toLocaleString('vi-VN')}</div>
            </div>
            <div class="report-item">
                <strong>💰 Tổng doanh thu:</strong>
                <div class="value">${(data.totalRevenue || 0).toLocaleString('vi-VN')} VNĐ</div>
            </div>
            <div class="report-item">
                <strong>💵 Giá vé trung bình:</strong>
                <div class="value">${(data.averageTicketPrice || 0).toLocaleString('vi-VN')} VNĐ</div>
            </div>
            <div class="report-item">
                <strong>⭐ Sự kiện hot:</strong>
                <div class="value">${data.topEvent || 'N/A'}</div>
            </div>
            <div class="report-item">
                <strong>📈 Doanh thu tuần:</strong>
                <div class="value">${(data.weeklyRevenue || 0).toLocaleString('vi-VN')} VNĐ</div>
            </div>
        `;
    } catch (error) {
        reportsList.innerHTML = '<p>Lỗi tải báo cáo</p>';
    }
}

// ============= Booking Function =============

async function bookTicket() {
    const eventId = document.getElementById('eventSelect').value;
    const seatCount = parseInt(document.getElementById('seatCount').value);
    const userName = document.getElementById('userName').value;
    const userEmail = document.getElementById('userEmail').value;
    const userPhone = document.getElementById('userPhone').value;

    const messageDiv = document.getElementById('bookingMessage');

    if (!eventId || !userName || !userEmail || !userPhone) {
        messageDiv.className = 'message error';
        messageDiv.textContent = '⚠️ Vui lòng điền đầy đủ thông tin';
        messageDiv.style.display = 'block';
        return;
    }

    if (seatCount < 1) {
        messageDiv.className = 'message error';
        messageDiv.textContent = '⚠️ Số lượng vé phải >= 1';
        messageDiv.style.display = 'block';
        return;
    }

    const selectedEvent = events.find(e => e.id === eventId);
    
    if (selectedEvent.availableSeats < seatCount) {
        messageDiv.className = 'message error';
        messageDiv.textContent = `⚠️ Chỉ còn ${selectedEvent.availableSeats} vé trống`;
        messageDiv.style.display = 'block';
        return;
    }

    try {
        // Mock booking (in production, call API)
        const bookingData = {
            userId: currentUser.id,
            eventId: eventId,
            seatCount: seatCount,
            totalPrice: selectedEvent.price * seatCount,
            userName: userName,
            userEmail: userEmail,
            userPhone: userPhone,
            bookingDate: new Date().toISOString(),
            status: 'PENDING'
        };

        // Save booking locally
        bookings.push(bookingData);

        // Update available seats
        selectedEvent.availableSeats -= seatCount;

        messageDiv.className = 'message success';
        messageDiv.innerHTML = `
            ✅ <strong>Đặt vé thành công!</strong><br>
            Sự kiện: ${selectedEvent.name}<br>
            Số vé: ${seatCount}<br>
            Tổng giá: ${(selectedEvent.price * seatCount).toLocaleString('vi-VN')} VNĐ<br>
            <em>Hóa đơn đã gửi đến email: ${userEmail}</em>
        `;
        messageDiv.style.display = 'block';

        // Clear form
        document.getElementById('bookingForm').reset();

        // Update stats
        updateStats();

        // Try to save to API
        try {
            await bookSeats(bookingData);
        } catch (error) {
            console.log('API call failed, booking saved locally');
        }

    } catch (error) {
        messageDiv.className = 'message error';
        messageDiv.textContent = '❌ Lỗi: ' + error.message;
        messageDiv.style.display = 'block';
    }
}

// ============= Statistics =============

function updateStats() {
    const totalEvents = events.length;
    const totalTicketsSold = bookings.reduce((sum, b) => sum + b.seatCount, 0);
    const totalRevenue = bookings.reduce((sum, b) => sum + b.totalPrice, 0);

    document.getElementById('eventCount').textContent = totalEvents;
    document.getElementById('ticketCount').textContent = totalTicketsSold.toLocaleString('vi-VN');
    document.getElementById('revenueCount').textContent = totalRevenue.toLocaleString('vi-VN') + ' VNĐ';
}

// ============= Utilities =============

function formatDate(dateString) {
    return new Date(dateString).toLocaleDateString('vi-VN');
}

function formatTime(dateString) {
    return new Date(dateString).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
}

function formatCurrency(amount) {
    return amount.toLocaleString('vi-VN') + ' VNĐ';
}
