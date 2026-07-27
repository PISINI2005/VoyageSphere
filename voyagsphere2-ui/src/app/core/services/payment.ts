import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { InvoiceResponseDTO, PaymentDTO, PaymentResponseDTO } from '../models/travel.model';
import { PageResponse } from '../models/pageResponse.model';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private readonly paymentBaseUrl = 'http://localhost:8080/api/v1/payments';
  private readonly invoiceBaseUrl = 'http://localhost:8080/api/v1/invoices';

  constructor(private http: HttpClient) {}

  getMyInvoices(
  page: number = 0,
  size: number = 5,
  userId?: number
): Observable<PageResponse<InvoiceResponseDTO>> {

  let url =
    `${this.invoiceBaseUrl}/me?page=${page}&size=${size}`;

  if (userId) {
    url += `&userId=${userId}`;
  }

  return this.http.get<PageResponse<InvoiceResponseDTO>>(url);
}

  getInvoicesByBooking(bookingId: number): Observable<InvoiceResponseDTO[]> {
    return this.http.get<InvoiceResponseDTO[]>(`${this.invoiceBaseUrl}/booking/${bookingId}`);
  }

  getInvoiceById(id: number): Observable<InvoiceResponseDTO> {
    return this.http.get<InvoiceResponseDTO>(`${this.invoiceBaseUrl}/${id}`);
  }

  makePayment(dto: PaymentDTO): Observable<PaymentResponseDTO> {
    return this.http.post<PaymentResponseDTO>(this.paymentBaseUrl, dto);
  }

  getPaymentsForInvoice(invoiceId: number): Observable<PaymentResponseDTO[]> {
    return this.http.get<PaymentResponseDTO[]>(`${this.paymentBaseUrl}/invoice/${invoiceId}`);
  }

  getPaymentById(id: number): Observable<PaymentResponseDTO> {
    return this.http.get<PaymentResponseDTO>(`${this.paymentBaseUrl}/${id}`);
  }

  getPayments(): Observable<PaymentResponseDTO[]> {
    return this.http.get<PaymentResponseDTO[]>(this.paymentBaseUrl);
  }
}
