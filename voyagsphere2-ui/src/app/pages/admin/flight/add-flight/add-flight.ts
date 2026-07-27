import {
  ChangeDetectorRef,
  Component
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { FlightRequest } from '../../../../core/models/admin.model';
import { FlightStatus, SeatType } from '../../../../core/enums/admin-enums';
import { FlightService } from '../../../../core/services/flight';



@Component({
  selector: 'app-add-flight',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './add-flight.html'
})
export class AddFlight {

  loading = false;

  showSeatSection = false;

  showPopup = false;

  popupTitle = '';

  popupMessage = '';

  shouldRedirect = false;

  flight: FlightRequest = {
    flightNumber: '',

    partnerId: 0,

    source: '',

    destination: '',

    departureTime: '',

    arrivalTime: '',

    status: FlightStatus.SCHEDULED,

    seats: [
      {
        seatType: SeatType.ECONOMY,
        price: 0,
        totalSeats: 1
      }
    ],
    arrivalDayOffset: null
  };

  constructor(
    private readonly flightService: FlightService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef
  ) {}


proceedToSeats(): void {

  if (!this.isFlightTimingValid()) {

    this.popupTitle = 'Validation Error';

    this.popupMessage =
      this.getFlightTimingValidationMessage();

    this.showPopup = true;

    return;
  }

  this.showSeatSection = true;
}
  

  addSeat(): void {

    this.flight.seats.push({

      seatType: SeatType.ECONOMY,

      price: 0,

      totalSeats: 1

    });

  }

  removeSeat(index: number): void {

    if (this.flight.seats.length === 1) {

      this.popupTitle =
        'Validation Error';

      this.popupMessage =
        'At least one seat configuration is required';

      this.showPopup = true;

      this.cdr.detectChanges();

      return;

    }

    this.flight.seats.splice(
      index,
      1
    );

  }

  addFlight(): void {

    this.loading = true;

    console.log(
      '[AddFlight] Payload',
      this.flight
    );

    this.flightService
      .addFlight(this.flight)
      .subscribe({

        next: (response) => {

          console.log(
            '[AddFlight] Success',
            response
          );

          this.loading = false;

          this.popupTitle =
            'Success';

          this.popupMessage =
            'Flight added successfully';

          this.showPopup = true;

          this.shouldRedirect = true;

          this.cdr.detectChanges();

        },

        error: (error) => {

          console.error(
            '[AddFlight] Error',
            error
          );

          this.loading = false;

          this.popupTitle =
            'Error';

          this.popupMessage =
            error?.error?.message ??
            'Failed to add flight';

          this.showPopup = true;

          this.cdr.detectChanges();

        }

      });

  }

  closePopup(): void {

    this.showPopup = false;

    this.cdr.detectChanges();

    if (this.shouldRedirect) {

      this.shouldRedirect = false;

      this.router.navigate([
        '/admin/flight/view'
      ]);

    }

  }

  private getMinutes(time: string): number {

  const [hours, minutes] = time
    .split(':')
    .map(Number);

  return (hours * 60) + minutes;
}

isFlightTimingValid(): boolean {

  if (
    !this.flight.departureTime ||
    !this.flight.arrivalTime ||
    this.flight.arrivalDayOffset === null
  ) {
    return false;
  }

  // Same Day
  if (this.flight.arrivalDayOffset === 0) {
    return (
      this.flight.arrivalTime >
      this.flight.departureTime
    );
  }

  // Next Day
  return true;
}
getFlightTimingValidationMessage(): string {
  return 'For Same Day arrival, Arrival Time must be after Departure Time.';
}
}