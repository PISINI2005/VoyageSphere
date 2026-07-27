import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ItineraryResponseDTO, CreateItineraryDTO, AddBookingDTO } from '../models/travel.model';

@Injectable({
  providedIn: 'root'
})
export class ItineraryService {
  private readonly baseUrl = 'http://localhost:8080/api/v1/itineraries';

  constructor(private http: HttpClient) {}

 getMyItineraries(userId?: number): Observable<ItineraryResponseDTO[]> {

  let url = `${this.baseUrl}/me`;

  if (userId) {
    url += `?userId=${userId}`;
  }

  return this.http.get<ItineraryResponseDTO[]>(url);
}

  getItineraryById(id: number): Observable<ItineraryResponseDTO> {
    return this.http.get<ItineraryResponseDTO>(`${this.baseUrl}/${id}`);
  }

  createItinerary(dto: CreateItineraryDTO): Observable<ItineraryResponseDTO> {
    return this.http.post<ItineraryResponseDTO>(this.baseUrl, dto);
  }

  updateItinerary(id: number, dto: CreateItineraryDTO): Observable<ItineraryResponseDTO> {
    return this.http.put<ItineraryResponseDTO>(`${this.baseUrl}/${id}`, dto);
  }

  deleteItinerary(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  addBookingToItinerary(dto: AddBookingDTO): Observable<ItineraryResponseDTO> {
    return this.http.post<ItineraryResponseDTO>(`${this.baseUrl}/add-booking`, dto);
  }

  removeBookingFromItinerary(dto: AddBookingDTO): Observable<ItineraryResponseDTO> {
    return this.http.post<ItineraryResponseDTO>(`${this.baseUrl}/remove-booking`, dto);
  }
}
