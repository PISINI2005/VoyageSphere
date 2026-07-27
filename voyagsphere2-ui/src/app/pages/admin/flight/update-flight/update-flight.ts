import {
  ChangeDetectorRef,
  Component
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { FlightService } from '../../../../core/services/flight';


@Component({
  selector: 'app-update-flight',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './update-flight.html',
  styleUrl: './update-flight.css'
})
export class UpdateFlight {

  flightId!: number;

  flight: any = null;

  loading = false;

  showPopup = false;

  popupTitle = '';

  popupMessage = '';

  shouldRedirect = false;

  constructor(
    private readonly flightService: FlightService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef
  ) {}

  loadFlight(): void {

    if (!this.flightId) {

      this.popupTitle = 'Validation Error';

      this.popupMessage =
        'Please enter Flight ID';

      this.showPopup = true;

      this.cdr.detectChanges();

      return;

    }

    this.loading = true;

    this.flightService
      .getFlightById(this.flightId)
      .subscribe({

        next: (response) => {

          this.flight = response;
          console.log(response)

          if (!this.flight.seats) {

            this.flight.seats = [];

          }

          this.loading = false;

          this.cdr.detectChanges();

        },

        error: (error) => {

          console.error(error);

          this.loading = false;

          this.popupTitle = 'Error';

          this.popupMessage =
            error?.error?.message ??
            'Flight not found';

          this.showPopup = true;

          this.cdr.detectChanges();

        }

      });

  }

  addSeat(): void {

    this.flight.seats.push({

      seatType: 'ECONOMY',

      price: 0,

      totalSeats: 1

    });

    this.cdr.detectChanges();

  }

  updateFlight(): void {

    if (!this.flight) {

      this.popupTitle =
        'Validation Error';

      this.popupMessage =
        'Load a flight first';

      this.showPopup = true;

      this.cdr.detectChanges();

      return;

    }

    this.loading = true;

    const payload = {

      flightNumber:
        this.flight.flightNumber,

      partnerId:
        this.flight.partnerId,

      source:
        this.flight.source,

      destination:
        this.flight.destination,

      departureTime:
        this.flight.departureTime,

      arrivalTime:
        this.flight.arrivalTime,

      status:
        this.flight.status,

      seats:
        this.flight.seats

    };

    this.flightService
      .updateFlight(
        this.flight.flightId,
        payload
      )
      .subscribe({

        next: () => {

          this.loading = false;

          this.popupTitle =
            'Success';

          this.popupMessage =
            'Flight updated successfully';

          this.showPopup = true;

          this.shouldRedirect = true;

          this.cdr.detectChanges();

        },

        error: (error) => {

          console.error(error);

          this.loading = false;

          this.popupTitle =
            'Error';

          this.popupMessage =
            error?.error?.message ??
            'Failed to update flight';

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
    !this.flight?.departureTime ||
    !this.flight?.arrivalTime ||
    this.flight?.arrivalDayOffset === null ||
    this.flight?.arrivalDayOffset === undefined
  ) {
    return true;
  }

  const departure =
    this.getMinutes(this.flight.departureTime);

  const arrival =
    this.getMinutes(this.flight.arrivalTime);

  // Same Day
  if (this.flight.arrivalDayOffset === 0) {
    return arrival > departure;
  }

  // Next Day
  if (this.flight.arrivalDayOffset === 1) {
    return arrival < departure;
  }

  return false;
}

getFlightTimingValidationMessage(): string {

  if (
    this.flight?.arrivalDayOffset === 0
  ) {
    return 'For Same Day arrival, Arrival Time must be after Departure Time.';
  }

  if (
    this.flight?.arrivalDayOffset === 1
  ) {
    return 'For Next Day arrival, Arrival Time must be before Departure Time.';
  }

  return '';
}
}