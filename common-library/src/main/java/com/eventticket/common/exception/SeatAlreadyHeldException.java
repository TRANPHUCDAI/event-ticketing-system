package com.eventticket.common.exception;

public class SeatAlreadyHeldException extends ApiException {
     public SeatAlreadyHeldException(String seatId) {
          super(
                    "Seat " + seatId + " is already held by another user",
                    "SEAT_ALREADY_HELD",
                    409);
     }
}
