export interface InvoiceDto {
  invoiceId?: number;
  amount?: number;
  status?: string;
  bookingId?: number;
  userId?: number;
  email?: string;
}