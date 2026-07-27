export interface TravelSummaryDto {
  totalRevenue: number;
  totalBookings: number;
  totalCancellations: number;
  averageBookingValue: number;
  flightRevenue: number;
  hotelRevenue: number;
  transportRevenue: number;
  packageRevenue: number;
  cancelledRevenue: number;
}