import { Component, OnInit, ChangeDetectorRef } from '@angular/core';

import { CommonModule } from '@angular/common';

import { FormsModule } from '@angular/forms';
import { BookingService } from '../../../core/services/booking';



@Component({
  selector: 'app-view-bookings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './booking.html',
  styleUrls: ['./booking.css'],
})
export class Booking implements OnInit {
  bookings: any[] = [];

  userId?: number;

  loading = false;

  showPopup = false;

  popupTitle = '';

  popupMessage = '';

  showPassengerPopup = false;

  selectedPassengers: any[] = [];

  currentPage = 0;

pageSize = 5;

totalPages = 0;

totalElements = 0;

isFirst = true;

isLast = false;

pages: number[] = [];

  constructor(
    private readonly bookingService: BookingService,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadAllBookings();
  }

  loadAllBookings(): void {

  this.loading = true;

  this.bookingService
    .getAllBookings(this.currentPage, this.pageSize)
    .subscribe({

      next: (response: any) => {

        this.bookings = response.content;

        this.currentPage = response.number;
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;

        this.isFirst = response.first;
        this.isLast = response.last;

        this.pages = Array.from(
          { length: this.totalPages },
          (_, i) => i
        );

        this.loading = false;

        this.cdr.detectChanges();

      },

      error: (error) => {

        console.error(error);

        this.loading = false;

        this.popupTitle = 'Error';

        this.popupMessage = 'Failed to load bookings';

        this.showPopup = true;

        this.cdr.detectChanges();

      }

    });

}

 searchByUser(): void {

  if (!this.userId) {

    this.currentPage = 0;

    this.loadAllBookings();

    return;

  }

  this.loading = true;

  this.bookingService
    .getMyBookings(
      this.currentPage,
      this.pageSize,
      this.userId
    )
    .subscribe({

      next: (response: any) => {

        this.bookings = response.content;

        this.currentPage = response.number;
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;

        this.isFirst = response.first;
        this.isLast = response.last;

        this.pages = Array.from(
          { length: this.totalPages },
          (_, i) => i
        );

        this.loading = false;

        this.cdr.detectChanges();

      },

      error: (error) => {

        console.error(error);

        this.loading = false;

        this.popupTitle = 'Error';

        this.popupMessage = 'Failed to load user bookings';

        this.showPopup = true;

        this.cdr.detectChanges();

      }

    });

}

  viewPassengers(booking: any): void {
    this.selectedPassengers = booking.passengers ?? [];

    this.showPassengerPopup = true;

    this.cdr.detectChanges();
  }

  closePassengerPopup(): void {
    this.showPassengerPopup = false;

    this.selectedPassengers = [];

    this.cdr.detectChanges();
  }

  closePopup(): void {
    this.showPopup = false;

    this.cdr.detectChanges();
  }

  previousPage(): void {

  if (!this.isFirst) {

    this.currentPage--;

    this.userId
      ? this.searchByUser()
      : this.loadAllBookings();

  }

}

nextPage(): void {

  if (!this.isLast) {

    this.currentPage++;

    this.userId
      ? this.searchByUser()
      : this.loadAllBookings();

  }

}

goToPage(page: number): void {

  if (page !== this.currentPage) {

    this.currentPage = page;

    this.userId
      ? this.searchByUser()
      : this.loadAllBookings();

  }

}
}
