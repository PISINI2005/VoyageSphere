export type Role = 'CUSTOMER' | 'TRAVEL_AGENT' | 'ADMIN' | 'FINANCE_OFFICER'|'COMPLIANCE_OFFICER';
export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
export type Gender = 'MALE' | 'FEMALE' | 'OTHER';
export type SeatType = 'ECONOMY' | 'BUSINESS' | 'FIRST_CLASS';
export type TransportClass = 'AC' | 'NON_AC' | 'SLEEPER';
export type BookingStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED';
export type BookingType = 'FLIGHT' | 'HOTEL' | 'TRANSPORT' | 'PACKAGE';
export type HotelRoomType = 'SINGLE' | 'DOUBLE' | 'SUITE' | 'DELUXE';
export type TravelPackageCategory = 'ADVENTURE' | 'BEACH' | 'FAMILY' | 'HONEYMOON';
export type PackageStatus = 'ACTIVE' | 'INACTIVE';
export type IdentificationType = 'PASSPORT' | 'AADHAAR' | 'PAN' | 'DRIVING_LICENSE';
export type Nationality = 'INDIAN' | 'AMERICAN' | 'BRITISH' | 'OTHER';
export type PassengerStatus = 'ACTIVE' | 'CANCELLED';
export type PaymentStatus = 'PENDING' | 'SUCCESS' | 'FAILED' | 'CANCELLED' | 'REFUNDED' | 'PARTIALLY_REFUNDED';
export type NotificationCategory = 'BOOKING' | 'PAYMENT' | 'CANCELLATION' | 'SYSTEM';
export type NotificationStatus = 'READ' | 'UNREAD';
export type ComplaintStatus = 'PENDING' | 'IN_PROGRESS' | 'RESOLVED' | 'REJECTED';
export type ComplaintTargetType = 'BOOKING' | 'INVOICE' | 'PAYMENT';

export interface UserDTO {
  email: string;
  password: string;
  phoneNo: number;
}

export interface LoginDTO {
  email: string;
  password: string;
}

export interface UserResponseDTO {
  userId: number;
  email: string;
  role: Role;
  status: UserStatus;
}

export interface AuthResponseDTO {
  token: string;
  user: UserResponseDTO;
}

export interface PassengerProfileRequestDTO {
  passengerName: string;
  dateOfBirth: string;
  gender: Gender;
  contactNo: string;
  emailAddress: string;
  nationality: Nationality;
  identificationType: IdentificationType;
  identificationNumber: string;
}

export interface PassengerProfileResponseDTO {
  userId?:number,
  passengerProfileId: number;
  passengerName: string;
  dateOfBirth: string;
  gender: Gender;
  contactNo: string;
  emailAddress: string;
  nationality: Nationality;
  identificationType: IdentificationType;
  identificationNumber: string;
}

export interface PassengerResponseDTO {
  passengerId: number;
  passengerProfileId: number;
  passengerName: string;
  dateOfBirth: string;
  gender: Gender;
  contactNo: string;
  emailAddress: string;
  nationality: Nationality;
  identificationType: IdentificationType;
  identificationNumber: string;
  status: PassengerStatus;
}

export interface BookingResponseDTO {
  bookingId: number;
  bookingType: BookingType;
  amount: number;
  status: BookingStatus;
  userId: number;
  email: string;
  units: number;
  flightId?: number;
  flightNumber?: string;
  hotelId?: number;
  hotelName?: string;
  transportId?: number;
  transportType?: string;
  packageId?: number;
  packageName?: string;
  itineraryId?: number;
  passengers?: PassengerResponseDTO[];
}

export interface ItineraryResponseDTO {
  itineraryId: number;
  tripName: string;
  description?: string;
  startDate: string;
  endDate: string;
  createdAt: string;
  userId: number;
  email: string;
  bookings: BookingResponseDTO[];
  totalTripAmount: number;
}

export interface InvoiceResponseDTO {
  invoiceId: number;
  amount: number;
  status: PaymentStatus;
  bookingId: number;
  userId: number;
  email: string;
}

export interface NotificationResponseDTO {
  notificationId: number;
  message: string;
  category: NotificationCategory;
  status: NotificationStatus;
  createdDate: string;
}

export interface BookingCancelDTO {
  userId?: number;
  bookingId: number;
}

export interface BookingCancelResponseDTO {
  bookingId: number;
  userId: number;
  status: BookingStatus;
  originalAmount: number;
  refundAmount: number;
  deductionAmount: number;
  bookingDate: string;
  cancelledAt: string;
  refundStatus: string;
  message: string;
}

export interface PassengerCancelResponseDTO {
  bookingId: number;
  passengerId: number;
  passengerName: string;
  bookingStatus: BookingStatus;
  remainingUnits: number;
  refundAmount: number;
  deductionAmount: number;
  refundStatus: string;
  cancelledAt: string;
  message: string;
}

export interface CreateItineraryDTO {
  userId?: number;
  tripName: string;
  description?: string;
  startDate: string;
  endDate: string;
}

export interface AddBookingDTO {
  itineraryId: number;
  bookingId: number;
}

export interface PaymentDTO {
  invoiceId: number;
  amount: number;
  paymentMethod: 'CREDIT_CARD' | 'DEBIT_CARD' | 'UPI' | 'NET_BANKING' | 'PAYPAL';
}

export interface PaymentResponseDTO {
  paymentId: number;
  amount: number;
  status: PaymentStatus;
}

export interface ComplaintRequestDTO {
  subject: string;
  description: string;
  targetType?: ComplaintTargetType;
  targetId?: number;
}

export interface ComplaintResponseDTO {
  complaintId: number;
  subject: string;
  description: string;
  status: ComplaintStatus;
  targetType?: ComplaintTargetType;
  targetId?: number;
  resolutionNote?: string;
  createdDate: string;
  resolvedDate?: string;
  userId: number;
}

export interface PackageItineraryResponseDTO {
  packageId: number;
  packageName: string;
  description: string;
  durationDays: number;
  price: number;
  status: PackageStatus;
  destination: string;
  packageItineraryId: number;
  notes: string;
  createdAt: string;
  detailedDescription: string;
  keyHighlights: string;
  guideName: string;
  supportContact: string;
  dayWisePlan: string;
}

// --- New Booking DTOs aligned with Backend ---

export interface BookingFlightDTO {
  userId?: number;
  flightId: number;
  units: number;
  bookingName: string;
  travelDate: string;
  gender: Gender;
  seatType: SeatType;
  passengerProfileIds: number[];
}

export interface BookingHotelDTO {
  userId?: number;
  hotelId: number;
  units: number;
  bookingName: string;
  gender: Gender;
  roomType: HotelRoomType;
  checkInDate: string;
  checkOutDate: string;
}

export interface BookingPackageDTO {
  userId?: number;
  packageId: number;
  travelDate: string;
  units: number;
  bookingName: string;
  gender: Gender;
}

export interface BookingTransportDTO {
  userId?: number;
  transportId: number;
  units: number;
  bookingName: string;
  gender: Gender;
  transportClass: TransportClass;
  travelDate: string;
  passengerProfileIds: number[];
}

export interface CatalogItem {
  id: number;
  type: BookingType;
  title: string;
  subtitle: string;
  price: number;
  identifierKey: string;
}

export interface SearchParams {
  type: BookingType;
  source?: string;
  destination?: string;
  city?: string;
  startDate?: string;
  endDate?: string;
  min?: number;
  max?: number;
  ratings?: number;
  category?: TravelPackageCategory;
  page?: number;
  size?: number;
}
