export interface KpiDto {
  reportId?: number;
  generatedAt?: string;
  startDate?: string;
  endDate?: string;
  reportLabel?: string;
  totalRevenue?: number;
  totalBookings?: number;
  totalCancellations?: number;
  cancellationRate?: number;
  averageBookingValue?: number;
  flightRevenue?: number;
  hotelRevenue?: number;
  transportRevenue?: number;
  packageRevenue?: number;
  refundedAmount?: number;
}