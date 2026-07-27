import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FlightRequest, FlightResponse, FlightStatusUpdateDTO } from '../models/admin.model';
import { PageResponse } from '../models/pageResponse.model';

@Injectable({
  providedIn: 'root'
})
export class FlightService {
  private apiUrl = 'http://localhost:8080/api/v1/flights';

  constructor(private http: HttpClient) {}

  getAllFlights(
  page: number = 0,
  size: number = 10
): Observable<PageResponse<FlightResponse>> {

  return this.http.get<PageResponse<FlightResponse>>(
    `${this.apiUrl}?page=${page}&size=${size}`
  );

}

  getFlightById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  addFlight(flight: FlightRequest): Observable<any> {
    return this.http.post<any>(this.apiUrl, flight);
  }

  updateFlight(id: number, flight: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, flight);
  }

  updateFlightStatus(id: number, status: string): Observable<any> {
    return this.http.patch<any>(`${this.apiUrl}/${id}/status`, { status });
  }

  deleteFlight(id: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/${id}`, { responseType: 'text' });
  }

  searchFlights(source: string, destination: string, min?: number, max?: number): Observable<any[]> {
    let params = new HttpParams().set('source', source).set('destination', destination);
    if (min != null) params = params.set('min', min);
    if (max != null) params = params.set('max', max);
    return this.http.get<any[]>(`${this.apiUrl}/search`, { params });
  }
}
