import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';

import { NavbarComponent } from '../../layout/navbar/navbar';
import { ConfirmationModalComponent } from '../../shared/components/confirmation-modal/confirmation-modal';
import { BookingService } from '../../core/services/booking';
import { BookingResponseDTO, BookingCancelDTO, BookingCancelResponseDTO, PassengerCancelResponseDTO } from '../../core/models/travel.model';

@Component({
  selector: 'app-my-trips',
  standalone: true,
  imports: [
    CommonModule,
    NavbarComponent,
    ConfirmationModalComponent
  ],
  providers:[CurrencyPipe,DatePipe],
  templateUrl: './my-trips.html',
  styleUrl: './my-trips.css'
})
export class MyTripsComponent implements OnInit {

  bookings: BookingResponseDTO[] = [];
  currentPage = 0;
  pageSize = 5;

  totalPages = 0;
  totalElements = 0;

  isFirst = true;
  isLast = false;

  selectedBookingForPassengers: BookingResponseDTO | null = null;
  activeTab: 'ALL' | 'ACTIVE' | 'CANCELLED' = 'ALL';

  showConfirmModal = false;
  confirmModalTitle = '';
  confirmModalMessage = '';
  pendingAction: (() => void) | null = null;

  constructor(private bookingService: BookingService) {}

  ngOnInit(): void {
    this.loadBookings();
  }

  setTab(tab: 'ALL' | 'ACTIVE' | 'CANCELLED'): void {
    this.activeTab = tab;
    this.currentPage = 0;
    this.loadBookings();
  }

  get filteredBookings(): BookingResponseDTO[] {
    if (this.activeTab === 'ALL') return this.bookings;
    if (this.activeTab === 'ACTIVE') {
      return this.bookings.filter(b => b.status === 'CONFIRMED' || b.status === 'PENDING');
    }
    if (this.activeTab === 'CANCELLED') {
      return this.bookings.filter(b => b.status === 'CANCELLED');
    }
    return this.bookings;
  }

  loadBookings(): void {
    this.bookingService
      .getMyBookings(this.currentPage, this.pageSize)
      .subscribe({
        next: (data: any) => {
          this.bookings = data.content;
          this.currentPage = data.number;
          this.totalPages = data.totalPages;
          this.totalElements = data.totalElements;
          this.isFirst = data.first;
          this.isLast = data.last;
        },
        error: (err: any) =>
          console.error('Error fetching bookings:', err)
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

  cancelBooking(bookingId: number): void {
    this.openConfirmModal(
      'Cancel Booking',
      `Are you sure you want to cancel booking #${bookingId}? This action cannot be undone.`,
      () => this.executeCancelBooking(bookingId)
    );
  }

  executeCancelBooking(bookingId: number): void {
    const dto: BookingCancelDTO = {
      bookingId: bookingId
    };

    this.bookingService.cancelBooking(dto).subscribe({
      next: (res: BookingCancelResponseDTO) => {
        alert(`Booking Cancelled Successfully!\n\nOriginal Amount: ₹${res.originalAmount}\nRefund Amount: ₹${res.refundAmount}\nDeduction: ₹${res.deductionAmount}\nStatus: ${res.refundStatus}\n\nMessage: ${res.message}`);
        this.loadBookings();
      },
      error: (err: any) => console.error('Error cancelling booking:', err)
    });
  }

  managePassengers(booking: BookingResponseDTO): void {
    this.selectedBookingForPassengers = booking;
  }

  closePassengerManagement(): void {
    this.selectedBookingForPassengers = null;
  }

  cancelPassenger(bookingId: number, passengerId: number, passengerName: string): void {
    this.openConfirmModal(
      'Cancel Passenger',
      `Are you sure you want to cancel passenger ${passengerName} from booking #${bookingId}?`,
      () => this.executeCancelPassenger(bookingId, passengerId)
    );
  }

  executeCancelPassenger(bookingId: number, passengerId: number): void {
    this.bookingService.cancelPassenger(bookingId, passengerId).subscribe({
      next: (res: PassengerCancelResponseDTO) => {
        alert(`Passenger Cancelled Successfully!\n\nRefund Amount: ₹${res.refundAmount}\nDeduction: ₹${res.deductionAmount}\nStatus: ${res.refundStatus}\nRemaining Units: ${res.remainingUnits}\n\nMessage: ${res.message}`);
        this.loadBookings();
        this.closePassengerManagement();
      },
      error: (err: any) => console.error('Error cancelling passenger:', err)
    });
  }

  nextPage(): void {
    if (!this.isLast) {
      this.currentPage++;
      this.loadBookings();
    }
  }

  previousPage(): void {
    if (!this.isFirst) {
      this.currentPage--;
      this.loadBookings();
    }
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.loadBookings();
  }

  get pages(): number[] {
    return Array.from(
      { length: this.totalPages },
      (_, index) => index
    );
  }
}
