import {
  ChangeDetectorRef,
  Component,
  OnInit
} from '@angular/core';

import { CommonModule } from '@angular/common';


import { RouterLink } from '@angular/router';
import { PartnerResponseDTO } from '../../../core/models/admin.model';
import { PartnerService } from '../../../core/services/partner';
import { FlightService } from '../../../core/services/flight';
import { HotelService } from '../../../core/services/hotel';
import { TransportService } from '../../../core/services/transport';
import { TravelPackageService } from '../../../core/services/travel-package';
import { BookingService } from '../../../core/services/booking';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {

  recentPartners: PartnerResponseDTO[] = [];

  recentBookings: any[] = [];

  flightCount = 0;

  hotelCount = 0;

  transportCount = 0;

  packageCount = 0;

  constructor(
    private readonly partnerService: PartnerService,
    private readonly flightService: FlightService,
    private readonly hotelService: HotelService,
    private readonly transportService: TransportService,
    private readonly packageService: TravelPackageService,
    private readonly bookingService: BookingService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {

    this.loadRecentPartners();

    this.loadRecentBookings();

    this.loadFlightCount();

    this.loadHotelCount();

    this.loadTransportCount();

    this.loadPackageCount();

  }

  private getCount(
    response: any
  ): number {

    if (Array.isArray(response)) {

      return response.length;

    }

    if (
      response?.content &&
      Array.isArray(response.content)
    ) {

      return response.content.length;

    }

    return 0;

  }

  loadFlightCount(): void {

    this.flightService
      .getAllFlights()
      .subscribe({

        next: (response) => {

          this.flightCount =response.totalElements;

          this.cdr.detectChanges();

        }

      });

  }

  loadHotelCount(): void {

    this.hotelService
      .searchHotels()
      .subscribe({

        next: (response) => {

          this.hotelCount =
           response.totalElements;

          this.cdr.detectChanges();

        }

      });

  }

  loadTransportCount(): void {

    this.transportService
      .getTransports()
      .subscribe({

        next: (response) => {

          this.transportCount =
            response.totalElements;

          this.cdr.detectChanges();

        }

      });

  }

  loadPackageCount(): void {

    this.packageService
      .getAllPackages()
      .subscribe({

        next: (response) => {

          this.packageCount =
            response.totalElements;

          this.cdr.detectChanges();

        }

      });

  }

  loadRecentPartners(): void {

    this.partnerService
      .getAllPartners()
      .subscribe({

        next: (response: any) => {

          const partners =
            Array.isArray(response)
              ? response
              : response?.content || [];

          this.recentPartners =
            partners
              .slice(-5)
              .reverse();

          this.cdr.detectChanges();

        },

        error: () => {

          this.recentPartners = [];

          this.cdr.detectChanges();

        }

      });

  }

  loadRecentBookings(): void {

    this.bookingService
      .getAllBookings()
      .subscribe({

        next: (response: any) => {

          const bookings =
            Array.isArray(response)
              ? response
              : response?.content || [];

          this.recentBookings =
            bookings
              .slice(-5)
              .reverse();

          this.cdr.detectChanges();

        },

        error: () => {

          this.recentBookings = [];

          this.cdr.detectChanges();

        }

      });

  }

}