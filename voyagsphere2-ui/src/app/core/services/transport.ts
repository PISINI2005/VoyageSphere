import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TransportDTO, TransportResponseDTO, TransportStatusUpdateDTO } from '../models/admin.model';

@Injectable({
  providedIn: 'root'
})
export class TransportService {
  private apiUrl = 'http://localhost:8080/api/v1/transports';

  constructor(private http: HttpClient) {}

  addTransport(payload: TransportDTO): Observable<any> {
    return this.http.post<any>(this.apiUrl, payload);
  }

  getTransportById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  updateTransport(id: number, payload: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, payload);
  }

  updateTransportStatus(id: number, payload: TransportStatusUpdateDTO): Observable<any> {
    return this.http.patch<any>(`${this.apiUrl}/${id}/status`, payload);
  }

  getTransports(source?: string, destination?: string, status?: string, page: number = 0, size: number = 5): Observable<any> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (source) params = params.set('source', source);
    if (destination) params = params.set('destination', destination);
    if (status) params = params.set('status', status);
    return this.http.get<any>(this.apiUrl, { params });
  }

  deleteTransport(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }
}
