import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoginDTO, CreateUserDTO, UserResponseDTO } from '../models/travel.model';
import { AuthResponseDTO } from '../models/dashboard.model';



@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly baseUrl = 'http://localhost:8080/api/v1/users';

  constructor(private http: HttpClient) {}

  login(dto: LoginDTO): Observable<AuthResponseDTO> {
    return this.http.post<AuthResponseDTO>(`${this.baseUrl}/login`, dto);
  }

  register(dto: CreateUserDTO): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/register`, dto);
  }

  logout(): void {
    localStorage.removeItem('token');
  }

  setUser(user: UserResponseDTO): void {
  localStorage.setItem('user', JSON.stringify(user));
}

getUser(): UserResponseDTO | null {
  const user = localStorage.getItem('user');

  return user ? JSON.parse(user) : null;
}

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  setToken(token: string): void {
    localStorage.setItem('token', token);
  }
  
  getRole(): string {
  const user = localStorage.getItem('user');

  if (!user) return '';

  return JSON.parse(user).role;
}

isTravelAgent(): boolean {
  return this.getRole() === 'TRAVEL_AGENT';
}

}
