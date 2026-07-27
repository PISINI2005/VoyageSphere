export interface PaymentResponseDto {
  paymentId: number;
  amount: number;
  status: 'SUCCESS' | 'PENDING' | 'FAILED' | string; // Using string union to strongly type your statuses
  invoiceID:number;
}