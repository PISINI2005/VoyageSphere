import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { NavbarComponent } from '../../layout/navbar/navbar';
import { ItineraryService } from '../../core/services/itinerary';
import { BookingService } from '../../core/services/booking';
import { ItineraryResponseDTO, CreateItineraryDTO, AddBookingDTO, BookingResponseDTO } from '../../core/models/travel.model';
import { AuthService } from '../../core/services/auth';
import { AgentContextService } from '../../core/services/agent-context';

@Component({
  selector: 'app-itineraries',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NavbarComponent
  ],
  templateUrl: './itineraries.html',
  styleUrl: './itineraries.css'
})
export class ItinerariesComponent implements OnInit {

  itineraries: ItineraryResponseDTO[] = [];
  selectedItinerary: ItineraryResponseDTO | null = null;
  allUserBookings: BookingResponseDTO[] = [];

  itineraryForm: CreateItineraryDTO = {
    tripName: '',
    description: '',
    startDate: '',
    endDate: ''
  };
  bookingPage = 0;
bookingSize = 5;

bookingTotalPages = 0;
bookingIsFirst = true;
bookingIsLast = true;

  constructor(
    private itineraryService: ItineraryService,
    private bookingService: BookingService,public authService:AuthService,
    public agentContext:AgentContextService
  ) {}

  ngOnInit(): void {
    this.loadItineraries();
    this.loadBookings();
  }

  loadItineraries(): void {

  const userId =
    this.authService.isTravelAgent()
      ? this.agentContext.selectedCustomerId
      : undefined;

  this.itineraryService
    .getMyItineraries(userId)
    .subscribe({
      next: (data) => this.itineraries = data,
      error: (err) => console.error(err)
    });
}

 loadBookings(): void {

  const userId =
    this.authService.isTravelAgent()
      ? this.agentContext.selectedCustomerId
      : undefined;

  this.bookingService
    .getMyBookings(
      this.bookingPage,
      this.bookingSize,
      userId
    )
    .subscribe({

      next: (response: any) => {

        this.allUserBookings = response.content;

        this.bookingTotalPages = response.totalPages;
        this.bookingIsFirst = response.first;
        this.bookingIsLast = response.last;

      },

      error: err =>
        console.error(err)

    });

}
  createItinerary(): void {

  if (!this.itineraryForm.tripName) {
    return;
  }

  if (
    this.authService.isTravelAgent() &&
    this.agentContext.selectedCustomerId
  ) {
    this.itineraryForm.userId =
      this.agentContext.selectedCustomerId;
  }

  this.itineraryService
    .createItinerary(this.itineraryForm)
    .subscribe({
      next: () => {

        this.loadItineraries();

        this.itineraryForm = {
          tripName: '',
          description: '',
          startDate: '',
          endDate: ''
        };
      },
      error: (err) =>
        console.error('Error creating itinerary:', err)
    });
}

  deleteItinerary(id: number): void {
    this.itineraryService.deleteItinerary(id).subscribe({
      next: () => {
        this.itineraries = this.itineraries.filter(item => item.itineraryId !== id);
        if (this.selectedItinerary?.itineraryId === id) {
          this.selectedItinerary = null;
        }
      },
      error: (err: any) => console.error('Error deleting itinerary:', err)
    });
  }

  selectItinerary(itinerary: ItineraryResponseDTO): void {
    this.selectedItinerary = itinerary;
  }

  addBooking(bookingId: number): void {
    if (!this.selectedItinerary) return;

    const dto: AddBookingDTO = {
      itineraryId: this.selectedItinerary.itineraryId,
      bookingId: bookingId
    };

    this.itineraryService.addBookingToItinerary(dto).subscribe({
      next: (updated: ItineraryResponseDTO) => {
        this.selectedItinerary = updated;
        this.loadItineraries();
      },
      error: (err: any) => {console.error('Error adding booking:', err);
        alert(err.error.message);
      }
    });
  }

  removeBooking(bookingId: number): void {
    if (!this.selectedItinerary) return;

    const dto: AddBookingDTO = {
      itineraryId: this.selectedItinerary.itineraryId,
      bookingId: bookingId
    };

    this.itineraryService.removeBookingFromItinerary(dto).subscribe({
      next: (updated: ItineraryResponseDTO) => {
        this.selectedItinerary = updated;
        this.loadItineraries();
      },
      error: (err: any) => console.error('Error removing booking:', err)
    });
  }

  previousBookingPage(): void {

  if (!this.bookingIsFirst) {

    this.bookingPage--;

    this.loadBookings();

  }

}

nextBookingPage(): void {

  if (!this.bookingIsLast) {

    this.bookingPage++;

    this.loadBookings();

  }

}
}
