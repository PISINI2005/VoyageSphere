import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Location } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AgentNavbar } from '../../../layout/agent-navbar/agent-navbar';
import { ItineraryService } from '../../../core/services/itinerary';
import { BookingService } from '../../../core/services/booking';
import { AgentContextService } from '../../../core/services/agent-context';
import { ItineraryResponseDTO, BookingResponseDTO, AddBookingDTO } from '../../../core/models/travel.model';

@Component({
  selector: 'app-itinerary-details',
  standalone: true,
  imports: [CommonModule, FormsModule, AgentNavbar],
  templateUrl: './itinerary-details.html',
  styleUrl: './itinerary-details.css'
})
export class ItineraryDetails implements OnInit {
  itinerary: ItineraryResponseDTO | null = null;
  isLoading = true;

  showAddBooking = false;
  customerBookings: BookingResponseDTO[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private itineraryService: ItineraryService,
    private bookingService: BookingService,
    private agentContext: AgentContextService,
    private location: Location
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.params['id'];
    this.loadItinerary(id);
  }

 loadItinerary(id: string): void {
  this.isLoading = true;

  this.itineraryService.getItineraryById(Number(id)).subscribe({
    next: (data) => {
      this.itinerary = data;

      console.log('Itinerary:', data);

      this.isLoading = false;
    },
    error: (err) => {
      console.error('Error fetching itinerary:', err);
      this.isLoading = false;
    }
  });
}

 

 openAddBookingModal(): void {

  if (!this.itinerary) {
    return;
  }

  this.showAddBooking = true;

  this.bookingService.getMyBookings(
    0,
    5,
    this.itinerary.userId
  ).subscribe({
    next: (bookings: any) => {
      this.customerBookings = bookings.content;
    },
    error: (err) => {
      console.error(err);
    }
  });
}

  addBooking(bookingId: number): void {
    if (!this.itinerary) return;

    const dto: AddBookingDTO = {
      itineraryId: this.itinerary.itineraryId,
      bookingId: bookingId
    };

    this.itineraryService.addBookingToItinerary(dto).subscribe({
      next: (updatedItinerary) => {
        this.itinerary = updatedItinerary;
        this.showAddBooking = false;
        alert('Booking added to itinerary successfully!');
      },
      error: (err) => {
        console.error('Error adding booking to itinerary:', err);
        alert('Failed to add booking.');
      }
    });
  }

  removeBooking(bookingId: number): void {
    if (!this.itinerary) return;

    if (confirm(`Are you sure you want to remove booking #${bookingId} from this itinerary?`)) {
      const dto: AddBookingDTO = {
        itineraryId: this.itinerary.itineraryId,
        bookingId: bookingId
      };

      this.itineraryService.removeBookingFromItinerary(dto).subscribe({
        next: (updatedItinerary) => {
          this.itinerary = updatedItinerary;
          alert('Booking removed from itinerary successfully!');
        },
        error: (err) => {
          console.error('Error removing booking from itinerary:', err);
          alert('Failed to remove booking.');
        }
      });
    }
  }

  goBack(): void {
    this.location.back();
  }

  viewBooking(bookingId: number): void {
    this.router.navigate(['/agent/booking-details', bookingId]);
  }
}
