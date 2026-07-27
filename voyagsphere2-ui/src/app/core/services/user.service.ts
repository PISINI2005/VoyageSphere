import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserResponseDTO } from '../models/travel.model';

export interface UpdateProfileDTO {
  firstName: string;
  lastName: string;
  phoneNo?: number;
}

export interface ChangePasswordDTO {
  oldPassword: string;
  newPassword: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly baseUrl = 'http://localhost:8080/api/v1/users';

  constructor(private http: HttpClient) {}

  getProfile(): Observable<UserResponseDTO> {
    return this.http.get<UserResponseDTO>(`${this.baseUrl}/profile`);
  }

  updateProfile(dto: UpdateProfileDTO): Observable<UserResponseDTO> {
    return this.http.put<UserResponseDTO>(`${this.baseUrl}/profile`, dto);
  }

  changePassword(dto: ChangePasswordDTO): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/change-password`, dto);
  }
}
