import { ChangeDetectorRef, Component, OnInit } from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { FlightResponse } from '../../../../core/models/admin.model';
import { FlightService } from '../../../../core/services/flight';
import { PageResponse } from '../../../../core/models/pageResponse.model';


@Component({
  selector: 'app-view-flight',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterLink],
  templateUrl: './view-flight.html',
  styleUrl: './view-flight.css',
})
export class ViewFlight implements OnInit {
  flights: FlightResponse[] = [];

  source = '';

  destination = '';

  min?: number;

  max?: number;

  loading = false;

  showPopup = false;

  popupTitle = '';

  popupMessage = '';
  

currentPage = 0;
pageSize = 10;

totalPages = 0;
totalElements = 0;

isFirst = true;
isLast = false;

pages: number[] = [];



  constructor(
    private readonly flightService: FlightService,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadFlights();
  }

  loadFlights(): void {

  this.loading = true;

  this.flightService
    .getAllFlights(this.currentPage, this.pageSize)
    .subscribe({

      next: (response) => {

        this.flights = response.content;

        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;

        this.currentPage = response.number;
        this.isFirst = response.first;
        this.isLast = response.last;

        this.pages = Array.from(
          { length: this.totalPages },
          (_, i) => i
        );

        this.loading = false;

        this.cdr.detectChanges();

      },

      error: () => {

        this.loading = false;

      }

    });

}
  searchFlights(): void {
    this.loading = true;

    this.flightService.searchFlights(this.source, this.destination, this.min, this.max).subscribe({
      next: (response: FlightResponse[]) => {
        this.flights = response;

        this.loading = false;

        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error(error);

        this.loading = false;

        this.popupTitle = 'Error';

        this.popupMessage = 'Failed to search flights';

        this.showPopup = true;

        this.cdr.detectChanges();
      },
    });
  }

  updateStatus(flight: FlightResponse, status: string): void {
    this.flightService.updateFlightStatus(flight.flightId, status).subscribe({
      next: () => {
        flight.status = status as any;

        this.popupTitle = 'Success';

        this.popupMessage = 'Flight status updated successfully';

        this.showPopup = true;

        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error(error);

        this.popupTitle = 'Error';

        this.popupMessage = error?.error?.message ?? 'Failed to update flight status';

        this.showPopup = true;

        this.cdr.detectChanges();
      },
    });
  }

  closePopup(): void {
    this.showPopup = false;

    this.cdr.detectChanges();
  }
  previousPage(): void {

  if (!this.isFirst) {

    this.currentPage--;

    this.loadFlights();

  }

}

nextPage(): void {

  if (!this.isLast) {

    this.currentPage++;

    this.loadFlights();

  }

}

goToPage(page: number): void {

  if (page !== this.currentPage) {

    this.currentPage = page;

    this.loadFlights();

  }

}
}
