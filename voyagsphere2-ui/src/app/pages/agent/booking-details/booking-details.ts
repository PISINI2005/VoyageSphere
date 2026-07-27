import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Location } from '@angular/common';
import { AgentNavbar } from '../../../layout/agent-navbar/agent-navbar';
import { ConfirmationModalComponent } from '../../../shared/components/confirmation-modal/confirmation-modal';
import { BookingService } from '../../../core/services/booking';
import { BookingResponseDTO, BookingCancelDTO, BookingCancelResponseDTO, PassengerCancelResponseDTO } from '../../../core/models/travel.model';

@Component({
  selector: 'app-booking-details',
  standalone: true,
  imports: [
    CommonModule,
    AgentNavbar,
    ConfirmationModalComponent
  ],
  providers: [CurrencyPipe],
  templateUrl: './booking-details.html',
  styleUrl: './booking-details.css'
})
export class BookingDetails implements OnInit {
  booking: BookingResponseDTO | null = null;
  isLoading = true;

  showConfirmModal = false;
  confirmModalTitle = '';
  confirmModalMessage = '';
  pendingAction: (() => void) | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookingService: BookingService,
    private location: Location
  ) {}

  ngOnInit(): void {
    const bookingId = this.route.snapshot.params['id'];
    this.loadBooking(bookingId);
  }

  loadBooking(bookingId: string): void {
  this.isLoading = true;

  this.bookingService.getBookingById(+bookingId).subscribe({
    next: (booking) => {
      this.booking = booking;
      this.isLoading = false;
    },
    error: (err) => {
      console.error('Error fetching booking:', err);
      this.isLoading = false;

      if (err.status === 404) {
        alert('Booking not found!');
        this.router.navigate(['/agent/bookings']);
      }
    }
  });
}

  openConfirmModal(title: string, message: string, action: () => void): void {
    this.confirmModalTitle = title;
    this.confirmModalMessage = message;
    this.pendingAction = action;
    this.showConfirmModal = true;
  }

  onConfirm(): void {
    if (this.pendingAction) {
      this.pendingAction();
    }
    this.showConfirmModal = false;
    this.pendingAction = null;
  }

  onCancelConfirm(): void {
    this.showConfirmModal = false;
    this.pendingAction = null;
  }

  cancelBooking(): void {
    if (!this.booking) return;
    this.openConfirmModal(
      'Cancel Booking',
      `Are you sure you want to cancel booking #${this.booking.bookingId}? This action cannot be undone.`,
      () => this.executeCancelBooking(this.booking!.bookingId)
    );
  }

  executeCancelBooking(bookingId: number): void {
    const dto: BookingCancelDTO = {
      bookingId: bookingId
    };

    this.bookingService.cancelBooking(dto).subscribe({
      next: (res: BookingCancelResponseDTO) => {
        alert(`Booking Cancelled Successfully!\n\nOriginal Amount: ₹${res.originalAmount}\nRefund Amount: ₹${res.refundAmount}\nDeduction: ₹${res.deductionAmount}\nStatus: ${res.refundStatus}\n\nMessage: ${res.message}`);
        this.router.navigate(['/agent/bookings']);
      },
      error: (err: any) => console.error('Error cancelling booking:', err)
    });
  }

  cancelPassenger(passengerId: number, passengerName: string): void {
    if (!this.booking) return;
    this.openConfirmModal(
      'Cancel Passenger',
      `Are you sure you want to cancel passenger ${passengerName} from booking #${this.booking.bookingId}?`,
      () => this.executeCancelPassenger(this.booking!.bookingId, passengerId)
    );
  }

  executeCancelPassenger(bookingId: number, passengerId: number): void {
    this.bookingService.cancelPassenger(bookingId, passengerId).subscribe({
      next: (res: PassengerCancelResponseDTO) => {
        alert(`Passenger Cancelled Successfully!\n\nRefund Amount: ₹${res.refundAmount}\nDeduction: ₹${res.deductionAmount}\nStatus: ${res.refundStatus}\nRemaining Units: ${res.remainingUnits}\n\nMessage: ${res.message}`);
        this.loadBooking(this.route.snapshot.params['id']);
      },
      error: (err: any) => console.error('Error cancelling passenger:', err)
    });
  }

  goBack(): void {
    this.location.back();
  }
}
