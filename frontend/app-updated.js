// ============= Main Application Logic =============

let currentUser = {
    id: 'user123',
    name: 'Test User',
};

let events = [];
let selectedEventId = null;
let pendingBooking = null; // Lưu thông tin đặt vé đang chờ thanh toán

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
        
        // Calculate total tickets from all events
        let totalTickets = 0;
        let totalRevenue = 0;
        
        eventsData.forEach(event => {
            const soldSeats = event.totalSeats - (event.availableSeats || event.totalSeats);
            totalTickets += soldSeats;
            totalRevenue += soldSeats * (event.ticketPrice || 500000);
        });
        
        document.getElementById('eventCount').textContent = totalEvents;
        document.getElementById('ticketCount').textContent = totalTickets;
        document.getElementById('revenueCount').textContent = totalRevenue.toLocaleString() + ' VNĐ';
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
            const availableSeats = event.availableSeats !== undefined ? event.availableSeats : event.totalSeats;
            const soldSeats = event.totalSeats - availableSeats;
            const ticketPrice = event.ticketPrice || 500000;
            
            const eventCard = document.createElement('div');
            eventCard.className = 'event-card';
            eventCard.innerHTML = `
                <h3>${event.name || event.eventName}</h3>
                <p><strong>📍</strong> ${event.venueName || event.location || 'N/A'}</p>
                <p><strong>📅</strong> ${event.date ? new Date(event.date).toLocaleDateString() : 'TBA'}</p>
                <p><strong>💺</strong> Còn lại: <span class="seats-available">${availableSeats}/${event.totalSeats}</span></p>
                <p><strong>💵</strong> Giá: <span class="price">${ticketPrice.toLocaleString()} VNĐ</span></p>
                <p class="description">${event.description || ''}</p>
                <div class="progress-bar">
                    <div class="progress-fill" style="width: ${(soldSeats/event.totalSeats*100).toFixed(1)}%"></div>
                </div>
                <p class="sold-info">Đã bán: ${soldSeats} vé (${(soldSeats/event.totalSeats*100).toFixed(1)}%)</p>
            `;
            eventsList.appendChild(eventCard);
        });

        // Update event select options
        loadEventOptions();
        
        // Update home stats
        if (document.getElementById('home').style.display !== 'none') {
            updateHomeStats();
        }
        
    } catch (error) {
        console.error('Error loading events:', error);
        document.getElementById('eventsList').innerHTML = 
            '<p class="error">❌ Không thể tải sự kiện. Vui lòng kiểm tra kết nối backend.</p>';
    }
}

function showCreateEventForm() {
    document.getElementById('createEventForm').style.display = 'block';
}

function hideCreateEventForm() {
    document.getElementById('createEventForm').style.display = 'none';
    document.getElementById('eventForm').reset();
}

async function createNewEvent() {
    const eventName = document.getElementById('eventName').value.trim();
    const venueName = document.getElementById('venueName').value.trim();
    const description = document.getElementById('eventDescription').value.trim();
    const totalSeats = parseInt(document.getElementById('totalSeats').value);
    const ticketPrice = parseInt(document.getElementById('ticketPrice').value);

    const messageDiv = document.getElementById('createEventMessage');
    messageDiv.style.display = 'block';
    messageDiv.className = 'message info';
    messageDiv.textContent = '🔄 Đang tạo sự kiện...';

    try {
        const eventData = {
            name: eventName,
            venueName: venueName,
            description: description,
            totalSeats: totalSeats,
            availableSeats: totalSeats,
            ticketPrice: ticketPrice,
            date: new Date().toISOString()
        };

        const response = await createEvent(eventData);
        console.log('Event created:', response);

        messageDiv.className = 'message success';
        messageDiv.textContent = '✅ Tạo sự kiện thành công!';

        setTimeout(() => {
            hideCreateEventForm();
            loadEvents();
        }, 1500);

    } catch (error) {
        console.error('Error creating event:', error);
        messageDiv.className = 'message error';
        messageDiv.textContent = `❌ Lỗi: ${error.message}`;
    }
}

function loadEventOptions() {
    const eventSelect = document.getElementById('eventSelect');
    eventSelect.innerHTML = '<option value="">-- Chọn sự kiện --</option>';
    
    events.forEach(event => {
        const option = document.createElement('option');
        option.value = event.id;
        option.textContent = `${event.name || event.eventName} - ${event.venueName || ''}`;
        option.dataset.price = event.ticketPrice || 500000;
        eventSelect.appendChild(option);
    });
}

function updateSelectedEvent() {
    const eventSelect = document.getElementById('eventSelect');
    const selectedOption = eventSelect.options[eventSelect.selectedIndex];
    
    if (selectedOption && selectedOption.dataset.price) {
        document.getElementById('paymentAmount').value = selectedOption.dataset.price;
    }
}

// ============= Booking Flow với Payment Modal =============

async function fullBookingFlow() {
    const eventId = document.getElementById('eventSelect').value;
    const seatId = document.getElementById('seatId').value.trim();
    const userId = document.getElementById('userId').value.trim();
    const amount = parseFloat(document.getElementById('paymentAmount').value);
    const paymentMethod = document.getElementById('paymentMethod').value;

    const messageDiv = document.getElementById('bookingMessage');
    messageDiv.style.display = 'block';

    // Validate
    if (!eventId || !seatId || !userId || !amount) {
        messageDiv.className = 'message error';
        messageDiv.innerHTML = '❌ Vui lòng điền đầy đủ thông tin!';
        return;
    }

    // Lưu thông tin booking để xử lý sau khi thanh toán
    pendingBooking = { eventId, seatId, userId, amount, paymentMethod };

    // Hiển thị modal thanh toán
    showPaymentModal(eventId, seatId, amount);
}

function showPaymentModal(eventId, seatId, amount) {
    const modal = document.getElementById('paymentModal');
    const eventSelect = document.getElementById('eventSelect');
    const selectedOption = eventSelect.options[eventSelect.selectedIndex];
    const eventName = selectedOption ? selectedOption.text : 'N/A';

    document.getElementById('paymentEventName').textContent = eventName;
    document.getElementById('paymentSeatId').textContent = seatId;
    document.getElementById('paymentTotalAmount').textContent = amount.toLocaleString() + ' VNĐ';

    modal.style.display = 'flex';
}

function closePaymentModal() {
    document.getElementById('paymentModal').style.display = 'none';
    document.getElementById('paymentDetailsForm').reset();
}

async function submitPayment() {
    const cardHolderName = document.getElementById('cardHolderName').value.trim();
    const cardNumber = document.getElementById('cardNumber').value.trim();
    const expiryDate = document.getElementById('expiryDate').value.trim();
    const cvv = document.getElementById('cvv').value.trim();

    if (!cardHolderName || !cardNumber || !expiryDate || !cvv) {
        alert('❌ Vui lòng nhập đầy đủ thông tin thẻ!');
        return;
    }

    if (!pendingBooking) {
        alert('❌ Không tìm thấy thông tin đặt vé!');
        return;
    }

    const messageDiv = document.getElementById('bookingMessage');
    closePaymentModal();

    try {
        const { eventId, seatId, userId, amount, paymentMethod } = pendingBooking;

        // BƯỚC 1: Hold Seat
        messageDiv.className = 'message info';
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
            pendingBooking = null;
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
            ✅ Bước 2: Đã thanh toán (Thẻ: **** ${cardNumber.slice(-4)})<br>
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

        // Clear form and reload events to update available seats
        document.getElementById('bookingForm').reset();
        pendingBooking = null;
        
        // Reload events để cập nhật số ghế còn lại
        setTimeout(() => {
            loadEvents();
        }, 2000);

    } catch (error) {
        messageDiv.className = 'message error';
        messageDiv.innerHTML = `❌ <strong>Lỗi trong quá trình đặt vé:</strong><br>${error.message}<br><br>
            <small>Vui lòng mở Console (F12) để xem chi tiết lỗi.</small>`;
        console.error('Booking flow error:', error);
        pendingBooking = null;
    }
}

// ============= My Tickets =============

async function loadMyTickets() {
    const userId = document.getElementById('userId').value.trim();
    if (!userId) {
        alert('❌ Vui lòng nhập User ID');
        return;
    }

    try {
        const response = await getUserTickets(userId);
        const tickets = response.data || [];
        
        const ticketsList = document.getElementById('myTicketsList');
        ticketsList.innerHTML = '';

        if (tickets.length === 0) {
            ticketsList.innerHTML = '<p class="no-data">Bạn chưa có vé nào.</p>';
            return;
        }

        const ticketsHtml = tickets.map(ticket => `
            <div class="ticket-card">
                <div class="ticket-header">
                    <span class="ticket-id">🎫 ${ticket.id}</span>
                    <span class="status-badge status-${ticket.status.toLowerCase()}">${ticket.status}</span>
                </div>
                <div class="ticket-body">
                    <p><strong>Event:</strong> ${ticket.eventId}</p>
                    <p><strong>Seat:</strong> ${ticket.seatId}</p>
                    <p><strong>QR:</strong> <code>${ticket.qrCode}</code></p>
                    <p><strong>Created:</strong> ${new Date(ticket.createdAt).toLocaleString()}</p>
                </div>
                <div class="ticket-actions">
                    ${ticket.status === 'ACTIVE' ? 
                        `<button onclick="checkIn('${ticket.id}')" class="btn btn-sm btn-success">✓ Check-in</button>` : 
                        `<span class="checked-in">✓ Checked In</span>`}
                </div>
            </div>
        `).join('');

        ticketsList.innerHTML = ticketsHtml;

    } catch (error) {
        console.error('Error loading tickets:', error);
        document.getElementById('myTicketsList').innerHTML = 
            '<p class="error">❌ Không thể tải vé. Vui lòng thử lại.</p>';
    }
}

async function checkIn(ticketId) {
    if (!confirm('Xác nhận check-in vé này?')) return;

    try {
        await checkInTicket(ticketId);
        alert('✅ Check-in thành công!');
        loadMyTickets();
    } catch (error) {
        alert(`❌ Lỗi check-in: ${error.message}`);
    }
}

// ============= Reports =============

async function loadReports() {
    try {
        const eventsData = await fetchEvents();
        
        // Calculate totals
        let totalEvents = eventsData.length;
        let totalTickets = 0;
        let totalRevenue = 0;
        let uniqueUsers = new Set();
        
        eventsData.forEach(event => {
            const soldSeats = event.totalSeats - (event.availableSeats || event.totalSeats);
            totalTickets += soldSeats;
            totalRevenue += soldSeats * (event.ticketPrice || 500000);
        });
        
        // Update summary stats
        document.getElementById('reportTotalEvents').textContent = totalEvents;
        document.getElementById('reportTotalTickets').textContent = totalTickets;
        document.getElementById('reportTotalRevenue').textContent = totalRevenue.toLocaleString() + ' VNĐ';
        document.getElementById('reportTotalUsers').textContent = Math.floor(totalTickets / 2); // Estimate
        
        // Create detailed event table
        const reportContainer = document.getElementById('eventsReport');
        
        if (eventsData.length === 0) {
            reportContainer.innerHTML = '<p class="no-data">Chưa có dữ liệu báo cáo</p>';
            return;
        }
        
        let tableHtml = `
            <table class="report-table">
                <thead>
                    <tr>
                        <th>Sự kiện</th>
                        <th>Địa điểm</th>
                        <th>Tổng ghế</th>
                        <th>Đã bán</th>
                        <th>Còn lại</th>
                        <th>Tỷ lệ</th>
                        <th>Giá vé</th>
                        <th>Doanh thu</th>
                    </tr>
                </thead>
                <tbody>
        `;
        
        eventsData.forEach(event => {
            const availableSeats = event.availableSeats !== undefined ? event.availableSeats : event.totalSeats;
            const soldSeats = event.totalSeats - availableSeats;
            const sellRate = (soldSeats / event.totalSeats * 100).toFixed(1);
            const ticketPrice = event.ticketPrice || 500000;
            const revenue = soldSeats * ticketPrice;
            
            tableHtml += `
                <tr>
                    <td><strong>${event.name || event.eventName}</strong></td>
                    <td>${event.venueName || event.location || '-'}</td>
                    <td>${event.totalSeats}</td>
                    <td class="sold">${soldSeats}</td>
                    <td class="available">${availableSeats}</td>
                    <td>
                        <div class="progress-bar-small">
                            <div class="progress-fill" style="width: ${sellRate}%"></div>
                        </div>
                        <span>${sellRate}%</span>
                    </td>
                    <td>${ticketPrice.toLocaleString()} VNĐ</td>
                    <td class="revenue">${revenue.toLocaleString()} VNĐ</td>
                </tr>
            `;
        });
        
        tableHtml += '</tbody></table>';
        reportContainer.innerHTML = tableHtml;
        
    } catch (error) {
        console.error('Error loading reports:', error);
        document.getElementById('eventsReport').innerHTML = 
            '<p class="error">❌ Không thể tải báo cáo</p>';
    }
}
