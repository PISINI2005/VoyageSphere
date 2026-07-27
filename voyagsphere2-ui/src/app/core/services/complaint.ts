import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ComplaintRequestDTO, ComplaintResponseDTO } from '../models/travel.model';
import { Complaint, ComplaintStatusUpdateDTO } from '../models/compliance.model';

@Injectable({
  providedIn: 'root'
})
export class ComplaintService {
  private readonly baseUrl = 'http://localhost:8080/api/v1/complaints';

  constructor(private http: HttpClient) {}

  // Customer methods
  createComplaint(dto: ComplaintRequestDTO): Observable<ComplaintResponseDTO> {
    return this.http.post<ComplaintResponseDTO>(this.baseUrl, dto);
  }

  getMyComplaints(): Observable<ComplaintResponseDTO[]> {
    return this.http.get<ComplaintResponseDTO[]>(`${this.baseUrl}/me`);
  }

  // Compliance Officer methods
  getAllComplaints(status?: string): Observable<Complaint[]> {
    if (status) {
      return this.http.get<Complaint[]>(`${this.baseUrl}?status=${status}`);
    }
    return this.http.get<Complaint[]>(this.baseUrl);
  }

  getComplaintById(id: number): Observable<Complaint> {
    return this.http.get<Complaint>(`${this.baseUrl}/${id}`);
  }

  updateStatus(id: number, dto: ComplaintStatusUpdateDTO): Observable<Complaint> {
    return this.http.patch<Complaint>(`${this.baseUrl}/${id}/status`, dto);
  }
}
