import { FlightStatus, HotelStatus, HotelRoomType, PartnerStatus, PartnerType, Role, TransportSeatType, TransportStatus, TravelPackageCategory, PackageStatus, UserStatus } from '../enums/admin-enums';

export interface FlightSeat {
  seatType: string;
  price: number;
  totalSeats: number;
}

export interface FlightRequest {
  arrivalDayOffset: null;
  flightNumber?: string;
  partnerId?: number;
  source?: string;
  destination?: string;
  arrivalTime?: string;
  departureTime?: string;
  status?: string;
  seats: FlightSeat[];
}

export interface FlightResponse {
  flightId: number;
  flightNumber: string;
  airlineName: string;
  source: string;
  destination: string;
  arrivalTime: string;
  departureTime: string;
  status: string;
  seats: FlightSeat[];
}

export interface FlightStatusUpdateDTO {
  status: FlightStatus;
}

export interface HotelRoomDTO {
  roomType: HotelRoomType;
  price: number;
  totalRooms: number;
}

export interface HotelDTO {
  hotelName: string;
  ratings: number;
  city: string;
  address: string;
  contactNo: string;
  emailId: string;
  status: HotelStatus;
  partnerId: number;
  rooms: HotelRoomDTO[];
}

export interface HotelResponseDTO {
  hotelId: number;
  hotelName: string;
  ratings: number;
  city: string;
  address: string;
  contactNo: string;
  emailId: string;
  status: HotelStatus;
  rooms: HotelRoomDTO[];
}

export interface HotelStatusUpdateDTO {
  status: HotelStatus;
}

export interface PartnerDTO {
  name: string;
  type: PartnerType;
  status: PartnerStatus;
}

export interface PartnerResponseDTO {
  partnerId: number;
  name: string;
  type: PartnerType;
  status: PartnerStatus;
}

export interface PartnerStatusUpdateDTO {
  status: PartnerStatus;
}

export interface TransportSeatDTO {
  transportClass: TransportSeatType;
  price: number;
  totalSeats: number;
}

export interface TransportDTO {
  transportNumber: number;
  source: string;
  destination: string;
  transportType: string;
  departureTime: string;
  arrivalTime: string;
  transportStatus: TransportStatus;
  partnerId: number;
  seats: TransportSeatDTO[];
  arrivalDayOffset:null
}

export interface TransportResponseDTO {
  transportId: number;
  transportNumber: number;
  source: string;
  destination: string;
  transportType: string;
  departureTime: string;
  arrivalTime: string;
  transportStatus: TransportStatus;
  seats: TransportSeatDTO[];
}

export interface TransportStatusUpdateDTO {
  status: TransportStatus;
}

export interface TravelPackageDTO {
  packageName: string;
  source: string;
  destination: string;
  price: number;
  durationDays: number;
  totalSlots: number;
  description: string;
  category: TravelPackageCategory;
  status: PackageStatus;
  partnerId: number;
  dayWisePlan: string;
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
  category: TravelPackageCategory;
  status: PackageStatus;
}

export interface CreateUserDTO {
  firstName:string;
  lastName:string;
  email: string;
  role: Role;
  phoneNo: number;
}

export interface UserDTO {
  email: string;
  password: string;
  phoneNo: number;
}

export interface UserResponseDTO {
  firstName:string,
  lastName:string;
  phoneNo:number;
  userId: number;
  email: string;
  role: Role;
  status: UserStatus;
}

export interface UserStatusUpdateDTO {
  status: UserStatus;
}
