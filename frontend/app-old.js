// ============= Main Application Logic =============

let currentUser = {
    id: 'user123',
    name: 'Test User',
};

let events = [];
let selectedEventId = null;

// Initialize app when page loads
document.addEventListener('DOMContentLoaded', () => {
    loadEvents();
    showPage('home');
});

// ============= Page Navigation =============

function showPage(pageName) {
    // Hide all pages
    const pages = document.querySelectorAll('.page');
    pages.forEach(page => page.style.display = 'none');

    // Show selected page
    const page = document.getElementById(pageName);
    if (page) page.style.display = 'block';

    // Load page-specific data
    if (pageName === 'events') {
        loadEvents();
    } else if (pageName === 'bookings') {
        loadEventOptions();
    } else if (pageName === 'reports') {
        loadReports();
    } else if (pageName === 'home') {
        updateHomeStats();
    }
}

// ============= Home Page Stats =============

async function updateHomeStats() {
    try {
        const eventsData = await fetchEvents();
        const totalEvents = eventsData.length;
        
        document.getElementById('eventCount').textContent = totalEvents;
        document.getElementById('ticketCount').textContent = '0';
        document.getElementById('revenueCount').textContent = '0 VNĐ';
    } catch (error) {
        console.error('Error loading stats:', error);
    }
}

// ============= Events Management =============

async function loadEvents() {
    try {
        const response = await fetchEvents();
        events = response || [];
        
        const eventsList = document.getElementById('eventsList');
        eventsList.innerHTML = '';

        if (events.length === 0) {
            eventsList.innerHTML = '<p class="no-data">Chưa có sự kiện nào. Hãy tạo sự kiện mới!</p>';
            return;
        }

        events.forEach(event => {
            const card = document.createElement('div');
            card.className = 'event-card';
            card.innerHTML = `
                <h3>${event.name}</h3>
                <p><strong>📍 Địa điểm:</strong> ${event.venueName || event.venue || 'N/A'}</p>
                <p><strong>📝 Mô tả:</strong> ${event.description || 'Không có mô tả'}</p>
                <div class="event-info">
                    <span>🎫 Tổng ghế: ${event.totalSeats}</span>
                    <span>✅ Còn trống: ${event.availableSeats || event.totalSeats}</span>
                </div>
                <p><small>ID: ${event.id}</small></p>
            `;
            eventsList.appendChild(card);
        });
    } catch (error) {
        console.error('Error loading events:', error);
        document.getElementById('eventsList').innerHTML = 
            '<p class="error">❌ Lỗi tải danh sách sự kiện. Vui lòng kiểm tra kết nối API.</p>';
    }
}

function showCreateEventForm() {
    document.getElementById('createEventForm').style.display = 'block';
    document.getElementById('createEventMessage').style.display = 'none';
}

function hideCreateEventForm() {
    document.getElementById('createEventForm').style.display = 'none';
    document.getElementById('eventForm').reset();
}

async function createNewEvent() {
    const name = document.getElementById('eventName').value;
    const venueName = document.getElementById('venueName').value;
    const description = document.getElementById('eventDescription').value;
    const totalSeats = parseInt(document.getElementById('totalSeats').value);

    const messageDiv = document.getElementById('createEventMessage');

    if (!name || !venueName || totalSeats < 1) {
        messageDiv.className = 'message error';
        messageDiv.textContent = '⚠️ Vui lòng điền đầy đủ thông tin';
        messageDiv.style.display = 'block';
        return;
    }

    try {
        const eventData = { name, venueName, description, totalSeats };
        const response = await createEvent(eventData);

        messageDiv.className = 'message success';
        messageDiv.innerHTML = `
            ✅ <strong>Tạo sự kiện thành công!</strong><br>
            Tên: ${response.data.name}<br>
            ID: ${response.data.id}
        `;
        messageDiv.style.display = 'block';

        // Reset form and reload
        document.getElementById('eventForm').reset();
        setTimeout(() => {
            hideCreateEventForm();
            loadEvents();
        }, 2000);

    } catch (error) {
        messageDiv.className = 'message error';
        messageDiv.textContent = '❌ Lỗi tạo sự kiện: ' + error.message;
        messageDiv.style.display = 'block';
    }
}

// ============= Booking Flow =============

function loadEventOptions() {
    const select = document.getElementById('eventSelect');
    select.innerHTML = '<option value="">-- Chọn sự kiện --</option>';

    events.forEach(event => {
        const option = document.createElement('option');
        option.value = event.id;
        option.text = `${event.name} (ID: ${event.id})`;
        select.appendChild(option);
    });
}

function updateSelectedEvent() {
    selectedEventId = document.getElementById('eventSelect').value;
}

async function fullBookingFlow() {
    const eventId = document.getElementById('eventSelect').value;
    const seatId = document.getElementById('seatId').value;
    const userId = document.getElementById('userId').value;
    const amount = document.getElementById('paymentAmount').value;
    const paymentMethod = document.getElementById('paymentMethod').value;

    const messageDiv = document.getElementById('bookingMessage');
    messageDiv.style.display = 'block';
    messageDiv.className = 'message info';

    if (!eventId || !seatId || !userId) {
        messageDiv.className = 'message error';
        messageDiv.textContent = '⚠️ Vui lòng điền đầy đủ thông tin';
        return;
    }

    try {
        // BƯỚC 1: Hold Seat
        messageDiv.innerHTML = '🔄 Bước 1/4: Đang giữ chỗ...';
        try {
            const holdResponse = await holdSeat(eventId, seatId, userId);
            console.log('Hold seat:', holdResponse);
        } catch (holdError) {
            console.error('Hold seat failed:', holdError);
            messageDiv.className = 'message error';
            messageDiv.innerHTML = `❌ <strong>Lỗi ở bước Hold Seat:</strong><br>
                ${holdError.message}<br><br>
                <small>Chi tiết: Backend có thể chưa sẵn sàng hoặc seat không tồn tại.<br>
                Hãy thử lại với seat khác (vd: A2, B1, C3) hoặc kiểm tra console logs.</small>`;
            return;
        }

        messageDiv.innerHTML = `✅ Bước 1/4: Đã giữ chỗ thành công!<br>
                                🔄 Bước 2/4: Đang tạo thanh toán...`;

        // BƯỚC 2: Create Payment
        const paymentResponse = await createPayment(userId, eventId, amount, paymentMethod);
        const paymentId = paymentResponse.data;
        console.log('Payment created:', paymentId);

        messageDiv.innerHTML = `✅ Bước 1/4: Đã giữ chỗ<br>
                                ✅ Bước 2/4: Đã tạo thanh toán (ID: ${paymentId})<br>
                                🔄 Bước 3/4: Đang xác nhận thanh toán...`;

        // BƯỚC 3: Confirm Payment
        const transactionId = 'txn_' + Date.now();
        await confirmPayment(paymentId, transactionId);
        console.log('Payment confirmed');

        messageDiv.innerHTML = `✅ Bước 1/4: Đã giữ chỗ<br>
                                ✅ Bước 2/4: Đã thanh toán<br>
                                ✅ Bước 3/4: Đã xác nhận thanh toán<br>
                                🔄 Bước 4/4: Đang tạo vé...`;

        // BƯỚC 4: Create Ticket
        const ticketResponse = await createTicket(eventId, seatId, userId, paymentId);
        const ticket = ticketResponse.data;
        console.log('Ticket created:', ticket);

        messageDiv.className = 'message success';
        messageDiv.innerHTML = `
            🎉 <strong>ĐẶT VÉ THÀNH CÔNG!</strong><br><br>
            ✅ Bước 1: Đã giữ chỗ ghế ${seatId}<br>
            ✅ Bước 2: Đã tạo thanh toán<br>
            ✅ Bước 3: Đã xác nhận thanh toán<br>
            ✅ Bước 4: Đã tạo vé<br><br>
            <strong>Thông tin vé:</strong><br>
            📌 Ticket ID: ${ticket.id}<br>
            🎫 Event ID: ${ticket.eventId}<br>
            💺 Seat ID: ${ticket.seatId}<br>
            👤 User ID: ${ticket.userId}<br>
            🔐 QR Code: ${ticket.qrCode}<br>
            📊 Status: ${ticket.status}
        `;

        // Clear form
        document.getElementById('bookingForm').reset();

    } catch (error) {
        messageDiv.className = 'message error';
        messageDiv.innerHTML = `❌ <strong>Lỗi trong quá trình đặt vé:</strong><br>${error.message}<br><br>
            <small>Vui lòng mở Console (F12) để xem chi tiết lỗi.</small>`;
        console.error('Booking flow error:', error);
    }
}

// ============= My Tickets =============

async function loadMyTickets() {
    const userId = document.getElementById('userId').value || 'user123';
    const ticketsList = document.getElementById('myTicketsList');

    try {
        ticketsList.innerHTML = '<p>🔄 Đang tải vé...</p>';
        const response = await getUserTickets(userId);
        const tickets = response.data || [];

        if (tickets.length === 0) {
            ticketsList.innerHTML = '<p class="no-data">Bạn chưa có vé nào.</p>';
            return;
        }

        let html = '<div class="tickets-grid">';
        tickets.forEach(ticket => {
            html += `
                <div class="ticket-card">
                    <h4>🎫 Vé #${ticket.id}</h4>
                    <p><strong>Sự kiện:</strong> ${ticket.eventId}</p>
                    <p><strong>Ghế:</strong> ${ticket.seatId}</p>
                    <p><strong>Trạng thái:</strong> 
                        <span class="status-${ticket.status.toLowerCase()}">${ticket.status}</span>
                    </p>
                    <p><strong>QR Code:</strong> ${ticket.qrCode}</p>
                    ${ticket.status === 'ACTIVE' ? 
                        `<button onclick="checkIn('${ticket.id}')" class="btn btn-small">✓ Check-in</button>` 
                        : ''}
                </div>
            `;
        });
        html += '</div>';
        ticketsList.innerHTML = html;

    } catch (error) {
        ticketsList.innerHTML = '<p class="error">❌ Lỗi tải vé: ' + error.message + '</p>';
    }
}

async function checkIn(ticketId) {
    try {
        const response = await checkInTicket(ticketId);
        alert('✅ Check-in thành công! Vé đã được sử dụng.');
        loadMyTickets(); // Reload tickets
    } catch (error) {
        alert('❌ Lỗi check-in: ' + error.message);
    }
}

// ============= Reports =============

async function loadReports() {
    const reportsList = document.getElementById('reportsList');
    
    try {
        const eventsData = await fetchEvents();
        const totalEvents = eventsData.length;
        const totalSeats = eventsData.reduce((sum, e) => sum + (e.totalSeats || 0), 0);
        
        reportsList.innerHTML = `
            <div class="report-item">
                <strong>📊 Tổng sự kiện:</strong>
                <div class="value">${totalEvents}</div>
            </div>
            <div class="report-item">
                <strong>🎫 Tổng số ghế:</strong>
                <div class="value">${totalSeats.toLocaleString('vi-VN')}</div>
            </div>
            <div class="report-item">
                <strong>ℹ️ Thông tin:</strong>
                <div class="value">Dữ liệu từ ${totalEvents} sự kiện</div>
            </div>
        `;
    } catch (error) {
        reportsList.innerHTML = '<p class="error">Lỗi tải báo cáo</p>';
    }
}
