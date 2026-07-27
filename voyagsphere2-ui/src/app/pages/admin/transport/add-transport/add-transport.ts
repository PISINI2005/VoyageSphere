import {
  ChangeDetectorRef,
  Component
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { TransportDTO } from '../../../../core/models/admin.model';
import { TransportService } from '../../../../core/services/transport';

@Component({
  selector: 'app-add-transport',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './add-transport.html',
  styleUrl: './add-transport.css'
})
export class AddTransport {

  loading = false;

  showPopup = false;

  popupTitle = '';

  popupMessage = '';

  shouldRedirect = false;

  showSeatConfiguration = false;

  transport: TransportDTO = {

    transportNumber: 0,

    source: '',

    destination: '',

    transportType: 'BUS' as any,

    departureTime: '',

    arrivalTime: '',

    transportStatus: 'AVAILABLE' as any,

    partnerId: 0,

    seats: [],

    arrivalDayOffset:null

  };

  constructor(
    private readonly transportService: TransportService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef
  ) {}

  proceedToSeatConfiguration(): void {

  if (
    this.transport.source.trim().toLowerCase() ===
    this.transport.destination.trim().toLowerCase()
  ) {

    this.popupTitle = 'Validation Error';
    this.popupMessage =
      'Source and Destination cannot be the same.';
    this.showPopup = true;

    return;
  }

  if (!this.isTransportTimingValid()) {

    this.popupTitle = 'Validation Error';
    this.popupMessage =
      this.getTransportTimingValidationMessage();
    this.showPopup = true;

    return;
  }

  this.showSeatConfiguration = true;
}

  addSeat(): void {

    this.transport.seats.push({

      transportClass: 'SEATER' as any,

      price: 0,

      totalSeats: 1

    });

  }

  removeSeat(index: number): void {

    this.transport.seats.splice(index, 1);

  }

  addTransport(): void {

    this.loading = true;

    this.transportService
      .addTransport(this.transport)
      .subscribe({

        next: () => {
          this.loading = false;
          this.popupTitle = 'Success';
          this.popupMessage = 'Transport added successfully';
          this.showPopup = true;
          this.shouldRedirect = true;
          this.cdr.detectChanges();
        },
        error: (error: any) => {
          console.error(error);
          this.loading = false;
          this.popupTitle = 'Error';
          this.popupMessage = error?.error?.message ?? 'Failed to add transport';
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
        'admin/transport/view'
      ]);

    }

  }
  private getMinutes(time: string): number {

  const [hours, minutes] =
    time.split(':').map(Number);

  return (hours * 60) + minutes;
}

isTransportTimingValid(): boolean {

  if (
    !this.transport.departureTime ||
    !this.transport.arrivalTime ||
    this.transport.arrivalDayOffset === null
  ) {
    return false;
  }

  // Same Day
  if (this.transport.arrivalDayOffset === 0) {

    return (
      this.transport.arrivalTime >
      this.transport.departureTime
    );

  }

  // Next Day
  return true;
}

getTransportTimingValidationMessage(): string {

  return 'For Same Day arrival, Arrival Time must be after Departure Time.';
}

}