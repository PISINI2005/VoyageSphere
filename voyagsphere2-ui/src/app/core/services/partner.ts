import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PartnerDTO, PartnerResponseDTO, PartnerStatusUpdateDTO } from '../models/admin.model';

@Injectable({
  providedIn: 'root'
})
export class PartnerService {
  private apiUrl = 'http://localhost:8080/api/v1/partners';

  constructor(private http: HttpClient) {}

  createPartner(payload: PartnerDTO): Observable<any> {
    return this.http.post<any>(this.apiUrl, payload);
  }

  updatePartner(id: number, payload: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, payload);
  }

  updatePartnerStatus(id: number, payload: PartnerStatusUpdateDTO): Observable<any> {
    return this.http.patch<any>(`${this.apiUrl}/${id}/status`, payload);
  }

  deletePartner(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }

  getPartnerById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  getPartnersByCategory(type: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/category/${type}`);
  }

  getAllPartners(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }
}
