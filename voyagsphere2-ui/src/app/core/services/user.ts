import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateUserDTO, UserDTO, UserResponseDTO, UserStatusUpdateDTO } from '../models/admin.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = 'http://localhost:8080/api/v1/users';

  constructor(private http: HttpClient) {}

  register(payload: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/register`, payload);
  }

  login(payload: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/login`, payload);
  }

  createUser(payload: CreateUserDTO): Observable<any> {
    return this.http.post<any>(this.apiUrl, payload);
  }

  getAllUsers(role?: string, status?: string): Observable<any> {
    let url = this.apiUrl;
    const params: string[] = [];
    if (role) params.push(`role=${role}`);
    if (status) params.push(`status=${status}`);
    if (params.length > 0) {
      url += `?${params.join('&')}`;
    }
    return this.http.get<any>(url);
  }

  updateUserStatus(id: number, payload: UserStatusUpdateDTO): Observable<any> {
    return this.http.patch<any>(`${this.apiUrl}/${id}/status`, payload);
  }

  deleteUser(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }

  getCustomers() {
    return this.http.get<UserResponseDTO[]>(`${this.apiUrl}?role=CUSTOMER`);
  }
}
