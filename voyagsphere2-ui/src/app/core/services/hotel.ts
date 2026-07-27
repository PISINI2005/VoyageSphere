import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { HotelDTO, HotelResponseDTO, HotelStatusUpdateDTO } from '../models/admin.model';

@Injectable({
  providedIn: 'root'
})
export class HotelService {
  private apiUrl = 'http://localhost:8080/api/v1/hotels';

  constructor(private http: HttpClient) {}

  addHotel(payload: HotelDTO): Observable<any> {
    return this.http.post<any>(this.apiUrl, payload);
  }

  getHotelById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  updateHotel(id: number, payload: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, payload);
  }

  updateHotelStatus(id: number, payload: HotelStatusUpdateDTO): Observable<any> {
    return this.http.patch<any>(`${this.apiUrl}/${id}/status`, payload);
  }

  deleteHotel(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }

  searchHotels(city?: string, ratings?: number, minPrice?: number, maxPrice?: number, page: number = 0, size: number = 5): Observable<any> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (city) params = params.set('city', city);
    if (ratings) params = params.set('ratings', ratings);
    if (minPrice) params = params.set('minPrice', minPrice);
    if (maxPrice) params = params.set('maxPrice', maxPrice);
    return this.http.get<any>(`${this.apiUrl}/search`, { params });
  }
}
