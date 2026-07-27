import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  ActivatedRoute,
  Router,
  RouterModule
} from '@angular/router';

import { NavbarComponent } from '../../layout/navbar/navbar';
import { BookingService } from '../../core/services/booking';
import { ProfileService } from '../../core/services/profile';
import { SearchService } from '../../core/services/search';
import { BookingFlightDTO, BookingHotelDTO, BookingPackageDTO, BookingTransportDTO, PassengerProfileResponseDTO } from '../../core/models/travel.model';
import { AuthService } from '../../core/services/auth';
import { AgentContextService } from '../../core/services/agent-context';

@Component({
  selector: 'app-booking',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NavbarComponent,
    RouterModule
  ],
  templateUrl: './booking.html',
  styleUrl: './booking.css'
})
export class BookingComponent implements OnInit {

  type!: string;
  id!: number;

  bookingForm = {
    bookingName: '',
    gender: 'MALE',
    travelDate: '',
    roomType: '',
    checkInDate: '',
    checkOutDate: '',
    transportClass: '',
    seatType: '',
    units: 1
  };

  passengers: PassengerProfileResponseDTO[] = [];
  selectedPassengers: number[] = [];
  selectedItem: any = null;

  constructor(
    private route: ActivatedRoute,
    public router: Router,
    private bookingService: BookingService,
    private profileService: ProfileService,
    private searchService: SearchService,
    private cdr:ChangeDetectorRef,
    public authService:AuthService,
    public agentContext:AgentContextService
  ) {}

  ngOnInit(): void {
  this.type = this.route.snapshot.params['type'];
  this.id = +this.route.snapshot.params['id'];

  const queryParams = this.route.snapshot.queryParams;

  if (this.type === 'FLIGHT' || this.type === 'TRANSPORT' || this.type === 'PACKAGE') {
    this.bookingForm.travelDate = queryParams['date'] || '';
  }
  if (this.type === 'FLIGHT') {
  this.bookingForm.seatType = queryParams['category'] || '';
}

if (this.type === 'TRANSPORT') {
  this.bookingForm.transportClass = queryParams['category'] || '';
}

  if (this.type === 'HOTEL') {
    this.bookingForm.checkInDate = queryParams['checkInDate'] || '';
    this.bookingForm.checkOutDate = queryParams['checkOutDate'] || '';
  }

  this.loadData();

  // existing code...


 this.router.events.subscribe(() => {

  if (
    this.authService.isTravelAgent() &&
    this.agentContext.selectedCustomerId
  ) {

    this.profileService
      .getMyProfiles(this.agentContext.selectedCustomerId)
      .subscribe({
        next: (data) => {
          this.passengers = [...data];
        }
      });

  } else {

    this.profileService.getMyProfiles().subscribe({
      next: (data) => {
        this.passengers = [...data];
      }
    });

  }

});
}
  addPassenger(): void {
  this.router.navigate(
    ['/profiles'],
    {
      queryParams: {
        returnUrl: this.router.url
      }
    }
  );
}

  loadData() {
    this.searchService.getItemById(
      this.id,
      this.type,
      this.bookingForm.travelDate,
      this.bookingForm.checkInDate,
      this.bookingForm.checkOutDate
    ).subscribe({
      next: (data) => this.selectedItem = data,
      error: (err) => console.error('Error loading item:', err)
    });

    if (
  this.authService.isTravelAgent() &&
  this.agentContext.selectedCustomerId
) {

  this.profileService
    .getMyProfiles(this.agentContext.selectedCustomerId)
    .subscribe({
      next: (data) => this.passengers = data,
      error: (err) => console.error('Error loading profiles:', err)
    });

} else {

  this.profileService.getMyProfiles().subscribe({
    next: (data) => this.passengers = data,
    error: (err) => console.error('Error loading profiles:', err)
  });

}
  }

  onDateChange(): void {
    this.loadData();
  }

  togglePassenger(id: number): void {
    if (this.selectedPassengers.includes(id)) {
      this.selectedPassengers = this.selectedPassengers.filter(pId => pId !== id);
    } else {
      this.selectedPassengers.push(id);
    }
  }

  get units(): number {
    if (this.type === 'HOTEL' || this.type === 'PACKAGE') {
      return this.bookingForm.units;
    }
    return this.selectedPassengers.length;
  }

  get availableSeatTypes(): string[] {
    if (!this.selectedItem || this.type !== 'FLIGHT') return [];
    const types = this.selectedItem.seats?.map((s: any) => s.seatType) || [];
    return [...new Set(types)] as string[];
  }

  get availableRoomTypes(): string[] {
    if (!this.selectedItem || this.type !== 'HOTEL') return [];
    const types = this.selectedItem.rooms?.map((r: any) => r.roomType) || [];
    return [...new Set(types)] as string[];
  }

  get availableTransportClasses(): string[] {
    if (!this.selectedItem || this.type !== 'TRANSPORT') return [];
    const types = this.selectedItem.seats?.map((s: any) => s.transportClass) || [];
    return [...new Set(types)] as string[];
  }


  getItemTitle(item: any): string {
    if (!item) return '';
    switch (this.type?.toUpperCase()) {
      case 'FLIGHT': return item.airlineName || item.flightNumber || 'Unknown Flight';
      case 'HOTEL': return item.hotelName || 'Unknown Hotel';
      case 'TRANSPORT': return `${item.transportType || ''} #${item.transportNumber || ''}`;
      case 'PACKAGE': return item.packageName || 'Unknown Package';
      default: return 'Unknown Item';
    }
  }

  getItemSubtitle(item: any): string {
    if (!item) return '';
    switch (this.type?.toUpperCase()) {
      case 'FLIGHT': return `${item.source || ''} → ${item.destination || ''}`;
      case 'HOTEL': return item.city || 'City not specified';
      case 'TRANSPORT': return `${item.source || ''} → ${item.destination || ''}`;
      case 'PACKAGE': return `${item.source || ''} → ${item.destination || ''}`;
      default: return '';
    }
  }

  getItemPrice(item: any): number {
    if (!item) return 0;
    if (this.type === 'FLIGHT' && item.seats?.length > 0) {
      const selectedSeat = item.seats.find((s: any) => s.seatType === this.bookingForm.seatType);
      return selectedSeat ? selectedSeat.price : item.seats[0].price;
    }
    if (this.type === 'HOTEL' && item.rooms?.length > 0) {
      const selectedRoom = item.rooms.find((r: any) => r.roomType === this.bookingForm.roomType);
      return selectedRoom ? selectedRoom.price : item.rooms[0].price;
    }
    if (this.type === 'TRANSPORT' && item.seats?.length > 0) {
      const selectedSeat = item.seats.find((s: any) => s.transportClass === this.bookingForm.transportClass);
      return selectedSeat ? selectedSeat.price : item.seats[0].price;
    }
    if (item.price) return item.price;
    return 0;
  }

  proceedToPayment(): void {
    if (this.units <= 0) {
      alert('Please specify at least one traveller or room');
      return;
    }

    const bookingDto = this.prepareBookingDto();

    this.bookingService.createBooking(this.type, bookingDto).subscribe({
      next: (response) => {
        this.router.navigate(['/payment', response.bookingId]);
      },
      error: (err) => {
        console.error('Booking creation failed:', err);
        alert('Booking failed. Please check your inputs.');
      }
    });
  }

  private prepareBookingDto(): any {
    const firstSelectedPassenger = this.passengers.find(p =>
      this.selectedPassengers.includes(p.passengerProfileId)
    );

    const base = {
      
userId:

    this.authService.isTravelAgent()
      ? this.agentContext.selectedCustomerId
      : undefined,

      bookingName: firstSelectedPassenger?.passengerName || this.bookingForm.bookingName || 'Guest',
      gender: firstSelectedPassenger?.gender || this.bookingForm.gender || 'MALE',
      units: this.units
    };

    switch (this.type.toUpperCase()) {
      case 'FLIGHT':
        return {
          ...base,
          flightId: this.id,
          travelDate: this.bookingForm.travelDate,
          seatType: this.bookingForm.seatType,
          passengerProfileIds: this.selectedPassengers
        };
      case 'HOTEL':
        return {
          ...base,
          hotelId: this.id,
          roomType: this.bookingForm.roomType,
          checkInDate: this.bookingForm.checkInDate,
          checkOutDate: this.bookingForm.checkOutDate
        };
      case 'TRANSPORT':
        return {
          ...base,
          transportId: this.id,
          travelDate: this.bookingForm.travelDate,
          transportClass: this.bookingForm.transportClass,
          passengerProfileIds: this.selectedPassengers
        };
      case 'PACKAGE':
        return {
          ...base,
          packageId: this.id,
          travelDate: this.bookingForm.travelDate
        };
      default:
        return base;
    }
  }
}
