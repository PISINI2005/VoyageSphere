import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BookingRequestCreateDTO, BookingRequestResponseDTO, BookingRequestFeedbackDTO, BookingRequestSubmitDTO, BookingRequestRejectDTO } from '../models/travel.model';

@Injectable({
  providedIn: 'root'
})
export class BookingRequestService {
  private readonly baseUrl = 'http://localhost:8080/api/v1/booking-requests';

  constructor(private http: HttpClient) {}

  // Customer Methods
  createBookingRequest(dto: BookingRequestCreateDTO): Observable<BookingRequestResponseDTO> {
    return this.http.post<BookingRequestResponseDTO>(this.baseUrl, dto);
  }

  getMyBookingRequests(): Observable<BookingRequestResponseDTO[]> {
    return this.http.get<BookingRequestResponseDTO[]>(`${this.baseUrl}/me`);
  }

  updateFeedback(id: number, feedbackDto: BookingRequestFeedbackDTO): Observable<BookingRequestResponseDTO> {
    return this.http.patch<BookingRequestResponseDTO>(`${this.baseUrl}/${id}/feedback`, feedbackDto);
  }

  // Agent Methods
  getBookingRequests(status: string = 'PENDING', page: number = 0, size: number = 10): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}?status=${status}&page=${page}&size=${size}`);
  }

  getAgentBookingRequests(page: number = 0, size: number = 10): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/agent/me?page=${page}&size=${size}`);
  }

  claimRequest(id: number): Observable<BookingRequestResponseDTO> {
    return this.http.patch<BookingRequestResponseDTO>(`${this.baseUrl}/${id}/claim`, {});
  }

  acceptRequest(id: number): Observable<BookingRequestResponseDTO> {
    return this.http.patch<BookingRequestResponseDTO>(`${this.baseUrl}/${id}/accept`, {});
  }

  submitFulfillment(id: number, dto: BookingRequestSubmitDTO): Observable<BookingRequestResponseDTO> {
    return this.http.patch<BookingRequestResponseDTO>(`${this.baseUrl}/${id}/submit`, dto);
  }

  rejectRequest(id: number, dto: BookingRequestRejectDTO): Observable<BookingRequestResponseDTO> {
    return this.http.patch<BookingRequestResponseDTO>(`${this.baseUrl}/${id}/reject`, dto);
  }

  getRequestById(id: number): Observable<BookingRequestResponseDTO> {
    return this.http.get<BookingRequestResponseDTO>(`${this.baseUrl}/${id}`);
  }
}
