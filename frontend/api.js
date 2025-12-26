// ============= API Configuration =============

const API_BASE_URL = 'http://localhost:8000/api';

// Helper function to make API calls
async function apiCall(endpoint, method = 'GET', data = null) {
    const options = {
        method,
        headers: {
            'Content-Type': 'application/json'
        }
    };

    if (data) {
        options.body = JSON.stringify(data);
    }

    try {
        console.log(`🔵 API Call: ${method} ${API_BASE_URL}${endpoint}`, data);
        const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
        
        console.log(`📡 Response status: ${response.status}`);
        
        if (!response.ok) {
            const errorText = await response.text();
            console.error(`❌ API Error: ${response.status} - ${errorText}`);
            throw new Error(`API Error ${response.status}: ${errorText || response.statusText}`);
        }

        const result = await response.json();
        console.log(`✅ API Response:`, result);
        return result;
    } catch (error) {
        console.error('❌ API Call Error:', error);
        throw error;
    }
}

// ============= Events API =============

async function fetchEvents() {
    try {
        const response = await apiCall('/events');
        return response.data || [];
    } catch (error) {
        console.error('Error fetching events:', error);
        return [];
    }
}

async function createEvent(eventData) {
    return apiCall('/events', 'POST', eventData);
}

// ============= Seats API =============

async function holdSeat(eventId, seatId, userId) {
    const params = new URLSearchParams({ eventId, seatId, userId });
    return apiCall(`/seats/hold?${params}`, 'POST');
}

async function releaseSeat(eventId, seatId, userId) {
    const params = new URLSearchParams({ eventId, seatId, userId });
    return apiCall(`/seats/release?${params}`, 'POST');
}

async function confirmSeat(eventId, seatId, userId) {
    const params = new URLSearchParams({ eventId, seatId, userId });
    return apiCall(`/seats/confirm?${params}`, 'POST');
}

async function getSeatStatus(eventId, seatId) {
    const params = new URLSearchParams({ eventId, seatId });
    return apiCall(`/seats/status?${params}`);
}

// ============= Payments API =============

async function createPayment(userId, eventId, amount, paymentMethod) {
    const params = new URLSearchParams({ userId, eventId, amount, paymentMethod });
    return apiCall(`/payments?${params}`, 'POST');
}

async function confirmPayment(paymentId, transactionId) {
    const params = new URLSearchParams({ transactionId });
    return apiCall(`/payments/${paymentId}/confirm?${params}`, 'POST');
}

async function getPaymentStatus(paymentId) {
    return apiCall(`/payments/${paymentId}`);
}

// ============= Tickets API =============

async function createTicket(eventId, seatId, userId, paymentId) {
    const params = new URLSearchParams({ eventId, seatId, userId, paymentId });
    return apiCall(`/tickets?${params}`, 'POST');
}

async function getTicket(ticketId) {
    return apiCall(`/tickets/${ticketId}`);
}

async function getUserTickets(userId) {
    return apiCall(`/tickets/user/${userId}`);
}

async function getEventTickets(eventId) {
    return apiCall(`/tickets/event/${eventId}`);
}

async function checkInTicket(ticketId) {
    return apiCall(`/tickets/${ticketId}/checkin`, 'POST');
}

// ============= Reports API =============

async function fetchReports() {
    try {
        const response = await apiCall('/reports/summary');
        return response.data || {};
    } catch (error) {
        console.error('Error fetching reports:', error);
        return {};
    }
}

async function fetchEventReports(eventId) {
    try {
        const response = await apiCall(`/reports/event/${eventId}`);
        return response.data || {};
    } catch (error) {
        console.error('Error fetching event reports:', error);
        return {};
    }
}

// ============= Notifications API =============

async function fetchNotifications(userId) {
    try {
        const response = await apiCall(`/notifications/user/${userId}`);
        return response.data || [];
    } catch (error) {
        console.error('Error fetching notifications:', error);
        return [];
    }
}

// ============= Mock Data (for offline testing) =============

const mockEvents = [
    {
        id: '1',
        name: 'Taylor Swift Concert',
        venue: 'Sân Mỹ Đình',
        startTime: '2025-03-15T19:00:00',
        endTime: '2025-03-15T22:00:00',
        totalSeats: 10000,
        availableSeats: 8500,
        price: 500000
    },
    {
        id: '2',
        name: 'BTS Permission to Dance',
        venue: 'Trung tâm Hội chợ Triển lãm',
        startTime: '2025-04-20T18:00:00',
        endTime: '2025-04-20T21:00:00',
        totalSeats: 20000,
        availableSeats: 12000,
        price: 750000
    },
    {
        id: '3',
        name: 'Coldplay Live Tour',
        venue: 'Nhà thi đấu Phú Thọ',
        startTime: '2025-05-10T19:30:00',
        endTime: '2025-05-10T22:30:00',
        totalSeats: 8000,
        availableSeats: 3000,
        price: 600000
    }
];

const mockReports = {
    totalEvents: 45,
    totalTicketsSold: 125000,
    totalRevenue: 187500000,
    averageTicketPrice: 1500,
    topEvent: 'Taylor Swift Concert',
    weeklyRevenue: 45000000
};
