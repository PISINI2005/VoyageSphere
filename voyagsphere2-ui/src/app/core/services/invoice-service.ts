import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
// import { TokenService } from './token-service';

@Injectable({
  providedIn: 'root',
})
export class InvoiceService {
  private baseurl = "http://localhost:8080/api/v1/invoices";

  constructor(private http: HttpClient) {}

  getallInvoice(): Observable<any> {
    return this.http.get(`${this.baseurl}`);
  }

  getbyID(invoiceId: number): Observable<any> {
    return this.http.get(`${this.baseurl}/${invoiceId}`);
  }

  // Added endpoint for booking ID fetch
  getbyBookingID(bookingId: number): Observable<any> {
    return this.http.get(`${this.baseurl}/booking/${bookingId}`);
  }
  // Add this method inside your InvoiceService class
  getbyUserID(userId: number): Observable<any> {
  return this.http.get(`${this.baseurl}/user/${userId}`);
}
}