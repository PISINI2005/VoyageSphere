import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  PassengerProfileRequestDTO,
  PassengerProfileResponseDTO
} from '../models/travel.model';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {

  private readonly baseUrl = 'http://localhost:8080/api/v1/passengers/profiles';

  constructor(private http: HttpClient) {}

  getMyProfiles(userId?: number): Observable<PassengerProfileResponseDTO[]> {
    let url = `${this.baseUrl}/me`;

    if (userId) {
      url += `?userId=${userId}`;
    }

    return this.http.get<PassengerProfileResponseDTO[]>(url);
  }

  getProfileById(
    id: number,
    userId?: number
  ): Observable<PassengerProfileResponseDTO> {

    let url = `${this.baseUrl}/${id}`;

    if (userId) {
      url += `?userId=${userId}`;
    }

    return this.http.get<PassengerProfileResponseDTO>(url);
  }

  createProfile(
    dto: PassengerProfileRequestDTO
  ): Observable<PassengerProfileResponseDTO> {
    return this.http.post<PassengerProfileResponseDTO>(this.baseUrl, dto);
  }

  updateProfile(
    id: number,
    dto: PassengerProfileRequestDTO,
    userId?: number
  ): Observable<PassengerProfileResponseDTO> {

    let url = `${this.baseUrl}/${id}`;

    if (userId) {
      url += `?userId=${userId}`;
    }

    return this.http.put<PassengerProfileResponseDTO>(url, dto);
  }

  deleteProfile(
    id: number,
    userId?: number
  ): Observable<void> {

    let url = `${this.baseUrl}/${id}`;

    if (userId) {
      url += `?userId=${userId}`;
    }

    return this.http.delete<void>(url);
  }
}