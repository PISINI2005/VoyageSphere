import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { AuthService } from './auth';
import { LoginDTO, CreateUserDTO, UserResponseDTO } from '../models/travel.model';
import { AuthResponseDTO } from '../models/dashboard.model';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const baseUrl = 'http://localhost:8080/api/v1/users';

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [AuthService, provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('HTTP Methods', () => {
    it('should send a POST request on login', () => {
      const loginDto: LoginDTO = { username: 'john', password: 'password123' } as any;
      const mockResponse: AuthResponseDTO = { token: 'mock-jwt-token' } as any;

      service.login(loginDto).subscribe((res) => {
        expect(res).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${baseUrl}/login`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(loginDto);
      req.flush(mockResponse);
    });

    it('should send a POST request on register', () => {
      const registerDto: CreateUserDTO = { username: 'jane', password: 'password123' } as any;
      const mockResponse = { message: 'User registered successfully' };

      service.register(registerDto).subscribe((res) => {
        expect(res).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${baseUrl}/register`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(registerDto);
      req.flush(mockResponse);
    });
  });

  describe('LocalStorage & Auth State Methods', () => {
    it('should remove token on logout', () => {
      localStorage.setItem('token', 'sample-token');
      expect(localStorage.getItem('token')).toBe('sample-token');

      service.logout();

      expect(localStorage.getItem('token')).toBeNull();
    });

    it('should save user to localStorage', () => {
      const user: UserResponseDTO = { id: 1, role: 'USER' } as any;

      service.setUser(user);

      expect(localStorage.getItem('user')).toBe(JSON.stringify(user));
    });

    it('should retrieve user from localStorage', () => {
      const user: UserResponseDTO = { id: 1, role: 'USER' } as any;
      localStorage.setItem('user', JSON.stringify(user));

      const retrievedUser = service.getUser();

      expect(retrievedUser).toEqual(user);
    });

    it('should return null when getting user if localStorage is empty', () => {
      expect(service.getUser()).toBeNull();
    });

    it('should return true for isLoggedIn when token exists', () => {
      localStorage.setItem('token', 'mock-token');

      expect(service.isLoggedIn()).toBeTruthy();
    });

    it('should return false for isLoggedIn when token does not exist', () => {
      expect(service.isLoggedIn()).toBeFalsy();
    });

    it('should set and get token in localStorage', () => {
      service.setToken('new-token');

      expect(localStorage.getItem('token')).toBe('new-token');
      expect(service.getToken()).toBe('new-token');
    });

    it('should return null for getToken when token is missing', () => {
      expect(service.getToken()).toBeNull();
    });

    it('should return user role when user exists in localStorage', () => {
      const user = { role: 'ADMIN' };
      localStorage.setItem('user', JSON.stringify(user));

      expect(service.getRole()).toBe('ADMIN');
    });

    it('should return empty string for getRole when no user exists', () => {
      expect(service.getRole()).toBe('');
    });

    it('should return true for isTravelAgent when role is TRAVEL_AGENT', () => {
      const user = { role: 'TRAVEL_AGENT' };
      localStorage.setItem('user', JSON.stringify(user));

      expect(service.isTravelAgent()).toBeTruthy();
    });

    it('should return false for isTravelAgent when role is not TRAVEL_AGENT', () => {
      const user = { role: 'USER' };
      localStorage.setItem('user', JSON.stringify(user));

      expect(service.isTravelAgent()).toBeFalsy();
    });
  });
});