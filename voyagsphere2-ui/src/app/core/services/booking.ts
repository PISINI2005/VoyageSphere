import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BookingResponseDTO, BookingCancelDTO, BookingCancelResponseDTO, PassengerCancelResponseDTO, BookingFlightDTO, BookingHotelDTO, BookingPackageDTO, BookingTransportDTO } from '../models/travel.model';

@Injectable({
  providedIn: 'root'
})
export class BookingService {
  private readonly baseUrl = 'http://localhost:8080/api/v1/bookings';

  constructor(private http: HttpClient) {}

 getMyBookings(
  page: number = 0,
  size: number = 5,
  userId?: number
) {
  let url = `${this.baseUrl}/me?page=${page}&size=${size}`;

  if (userId) {
    url += `&userId=${userId}`;
  }

  return this.http.get<any>(url);
}

 getAllBookings(
  page: number = 0,
  size: number = 5
): Observable<any> {

  return this.http.get<any>(
    `${this.baseUrl}?page=${page}&size=${size}`
  );

}
getBookingById(id: number) {
  return this.http.get<BookingResponseDTO>(
    `${this.baseUrl}/${id}`
  );
}



  cancelBooking(dto: BookingCancelDTO): Observable<BookingCancelResponseDTO> {
    return this.http.post<BookingCancelResponseDTO>(`${this.baseUrl}/cancel`, dto);
  }

  cancelPassenger(bookingId: number, passengerId: number): Observable<PassengerCancelResponseDTO> {
    return this.http.delete<PassengerCancelResponseDTO>(`${this.baseUrl}/${bookingId}/passengers/${passengerId}`);
  }

  createBooking(type: string, dto: any): Observable<any> {
    let endpoint = '';
    switch (type.toUpperCase()) {
      case 'FLIGHT': endpoint = '/flight'; break;
      case 'HOTEL': endpoint = '/hotel'; break;
      case 'PACKAGE': endpoint = '/package'; break;
      case 'TRANSPORT': endpoint = '/transport'; break;
      default: throw new Error('Invalid booking type');
    }
    return this.http.post<any>(`${this.baseUrl}${endpoint}`, dto);
  }
}

