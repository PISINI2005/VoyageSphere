export interface BookingCancelResponseDTO {
  bookingId: number;
  userId: number;
  status: string;
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
  bookingStatus: string;
  remainingUnits: number;
  refundAmount: number;
  deductionAmount: number;
  refundStatus: string;
  cancelledAt: string;
  message: string;
}

export interface PassengerResponseDTO {

  passengerId: number;
  passengerProfileId: number;
  passengerName: string;
  dateOfBirth: string;
  gender: 'MALE' | 'FEMALE' | 'OTHER';
  contactNo: string;
  emailAddress: string;
  nationality: string;
  identificationType: string;
  identificationNumber: string;
  status: string;
}

export interface BookingResponseDTO {
  bookingDate:string;
  bookingId: number;
  bookingType: 'FLIGHT' | 'HOTEL' | 'TRANSPORT' | 'PACKAGE';
  amount: number;
  status: string;
  userId: number;
  email: string;
  units: number;
  travelDate?: string;
  flightId?: number;
  flightNumber?: string;
  hotelId?: number;
  hotelName?: string;
  transportId?: number;
  transportType?: string;
  packageId?: number;
  packageName?: string;
  itineraryId?: number;
  passengers: PassengerResponseDTO[];
}

export interface BookingFlightResponseDTO {
  bookingId: number;
  bookingType: string;
  amount: number;
  status: string;
  userId: number;
  email: string;
  units: number;
  seatType: string;
  bookingName: string;
  gender: string;
  arrivalTime: string;
  departureTime: string;
  createdAt: string;
  travelDate: string;
  bookingDate: string;
  flightId: number;
  flightNumber: string;
  source: string;
  destination: string;
  passengers: PassengerResponseDTO[];
}

export interface BookingHotelResponseDTO {
  bookingId: number;
  bookingType: string;
  amount: number;
  status: string;
  userId: number;
  email: string;
  units: number;
  roomType: string;
  bookingName: string;
  gender: string;
  hotelId: number;
  hotelName: string;
  city: string;
  checkInDate: string;
  checkOutDate: string;
  days: number;
}

export interface BookingPackageResponseDTO {
  bookingId: number;
  bookingType: string;
  amount: number;
  status: string;
  bookingDate: string;
  userId: number;
  email: string;
  units: number;
  bookingName: string;
  gender: string;
  packageId: number;
  packageName: string;
  source: string;
  destination: string;
  durationDays: number;
  category: string;
  packageStatus: string;
}

export interface BookingTransportResponseDTO {
  bookingId: number;
  bookingType: string;
  amount: number;
  status: string;
  bookingDate: string;
  travelDate: string;
  userId: number;
  email: string;
  units: number;
  transportClass: string;
  bookingName: string;
  gender: string;
  transportId: number;
  source: string;
  destination: string;
  transportType: string;
  departureTime: string;
  arrivalTime: string;
  passengers: PassengerResponseDTO[];
}

export interface ItineraryResponseDTO {
  itineraryId: number;
  tripName: string;
  description: string;
  startDate: string;
  endDate: string;
  createdAt: string;
  userId: number;
  email: string;
  bookings: BookingResponseDTO[];
  totalTripAmount: number;
}

export interface CreateItineraryDTO {
  userId?: number;
  tripName: string;
  description: string;
  startDate: string;
  endDate: string;
}

export interface AddBookingDTO {
  itineraryId: number;
  bookingId: number;
}

export interface BookingCancelDTO {
  userId?: number;
  bookingId: number;
}

// --- Auth ---
export interface LoginDTO {
  email: string;
  password: string;
}

export interface CreateUserDTO {
  firstName:string;
  lastName:string;
  email: string;
  role: string;
  phoneNo: number;
  password: string;
}

export interface UserResponseDTO {
  firstName:string;
  lastName:string;
  userId: number;
  email: string;
  role: string;
  status: string;
  phoneNo: number;
}



// --- Booking Requests ---
export interface BookingFlightDTO {
  userId?: number;
  flightId: number;
  units: number;
  bookingName: string;
  travelDate: string;
  gender: 'MALE' | 'FEMALE' | 'OTHER';
  seatType: string;
  passengerProfileIds: number[];
}

export interface BookingHotelDTO {
  userId?: number;
  hotelId: number;
  units: number;
  bookingName: string;
  gender: 'MALE' | 'FEMALE' | 'OTHER';
  roomType: string;
  checkInDate: string;
  checkOutDate: string;
}

export interface BookingPackageDTO {
  userId?: number;
  packageId: number;
  travelDate: string;
  units: number;
  bookingName: string;
  gender: 'MALE' | 'FEMALE' | 'OTHER';
}

export interface BookingTransportDTO {
  userId?: number;
  transportId: number;
  units: number;
  bookingName: string;
  gender: 'MALE' | 'FEMALE' | 'OTHER';
  transportClass: string;
  travelDate: string;
  passengerProfileIds: number[];
}

// --- Search ---
export interface FlightSeatDTO {
  seatId: number;
  seatNumber: string;
  seatType: string;
  price: number;
  available: boolean;
}

export interface FlightResponseDTO {
  flightId: number;
  flightNumber: string;
  airlineName: string;
  source: string;
  destination: string;
  arrivalTime: string;
  departureTime: string;
  status: string;
  seats: FlightSeatDTO[];
}

export interface HotelRoomDTO {
  roomId: number;
  roomNumber: string;
  type: string;
  price: number;
  available: boolean;
}

export interface HotelResponseDTO {
  hotelId: number;
  hotelName: string;
  ratings: number;
  city: string;
  address: string;
  contactNo: string;
  emailId: string;
  status: string;
  rooms: HotelRoomDTO[];
}

export interface TransportSeatDTO {
  seatId: number;
  seatNumber: string;
  type: string;
  price: number;
  available: boolean;
}

export interface TransportResponseDTO {
  transportId: number;
  transportNumber: number;
  source: string;
  destination: string;
  transportType: string;
  departureTime: string;
  arrivalTime: string;
  transportStatus: string;
  seats: TransportSeatDTO[];
}

export interface TravelPackageResponseDTO {
  packageId: number;
  packageName: string;
  source: string;
  destination: string;
  price: number;
  durationDays: number;
  totalSlots: number;
  description: string;
  category: string;
  status: string;
  dayWisePlan: string;
}

// --- Profiles ---
export interface PassengerProfileRequestDTO {
  userId?:number,
  passengerName: string;
  dateOfBirth: string;
  gender: 'MALE' | 'FEMALE' | 'OTHER';
  contactNo: string;
  emailAddress: string;
  nationality: string;
  identificationType: string;
  identificationNumber: string;
}

export interface PassengerProfileResponseDTO {
  passengerProfileId: number;
  passengerName: string;
  dateOfBirth: string;
  gender: string;
  contactNo: string;
  emailAddress: string;
  nationality: string;
  identificationType: string;
  identificationNumber: string;
  status: string;
}

// --- Payments & Invoices ---
export interface PaymentDTO {
  invoiceId: number;
  amount: number;
  paymentMethod: 'CREDIT_CARD' | 'DEBIT_CARD' | 'UPI' | 'NET_BANKING' | 'PAYPAL';
}

export interface PaymentResponseDTO {
  paymentId: number;
  amount: number;
  date: string;
  status: string;
  invoiceId: number;
  transactionId:string;
}

export interface InvoiceResponseDTO {
  invoiceId: number;
  bookingId: number;
  totalAmount: number;
  amount: number;
  date: string;
  userId?:number;

  
  status: 'PAID' | 'UNPAID' | 'PARTIAL' | 'SUCCESS' | 'PENDING' | 'FAILED'|'CANCELLED'|'REFUNDED'|'PARTIALLY_REFUNDED';
}

// --- Complaints ---
export interface ComplaintRequestDTO {
  subject: string;
  description: string;
  targetType?: string;
  targetId?: number;
}

export interface ComplaintResponseDTO {
  complaintId: number;
  subject: string;
  description: string;
  status: string;
  createdAt: string;
  createdDate: string;
  targetType: string;
  targetId: number;
}


// --- Notifications ---
export type NotificationCategory = 'BOOKING' | 'INVOICE' | 'PAYMENT' | 'COMPLAINT';
export type NotificationStatus = 'UNREAD' | 'READ';

export interface NotificationResponseDTO {
  notificationId: number;
  message: string;
  category: NotificationCategory;
  status: NotificationStatus;
  createdDate: string;
}

export interface BookingRequestCreateDTO {
  type: 'BOOKING' | 'CANCELLATION';
  budget: number;
  requestDetails: string;
}

export interface BookingRequestResponseDTO {
  bookingRequestId: number;
  customerId: number;
  agentId: number;
  type: 'BOOKING' | 'CANCELLATION';
  budget: number;
  requestDetails: string;
  agentRemarks: string;
  modificationDetails: string;
  status: 'PENDING' | 'ASSIGNED' | 'ACCEPTED' | 'AWAITING_FEEDBACK' | 'REJECTED' | 'COMPLETED';
  customerStatus: string;
  linkedBookingIds: number[];
  createdAt: string;
}

export interface BookingRequestFeedbackDTO {
  customerStatus: 'SATISFIED' | 'MODIFICATION_REQUIRED';
  modificationDetails?: string;
}

export interface BookingRequestSubmitDTO {
  agentRemarks: string;
  linkedBookingIds: number[];
}

export interface BookingRequestRejectDTO {
  remarks: string;
}
