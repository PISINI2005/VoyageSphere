import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TravelPackageDTO, TravelPackageResponseDTO } from '../models/admin.model';

@Injectable({
  providedIn: 'root'
})
export class TravelPackageService {
  private apiUrl = 'http://localhost:8080/api/v1/packages';

  constructor(private http: HttpClient) {}

  addPackage(payload: TravelPackageDTO): Observable<any> {
    return this.http.post<any>(this.apiUrl, payload);
  }

  updatePackage(id: number, payload: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, payload);
  }

  updatePackageStatus(id: number, payload: any): Observable<any> {
    return this.http.patch<any>(`${this.apiUrl}/${id}/status`, payload);
  }

  getAllPackages(category?: string, page: number = 0, size: number = 5): Observable<any> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (category) params = params.set('category', category);
    return this.http.get<any>(this.apiUrl, { params });
  }

  deletePackage(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }

  getPackageById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }
}
