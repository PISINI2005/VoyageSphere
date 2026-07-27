import {
  ChangeDetectorRef,
  Component
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { TransportService } from '../../../../core/services/transport';

@Component({
  selector: 'app-update-transport',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './update-transport.html',
  styleUrl: './update-transport.css'
})
export class UpdateTransport {

  transportId!: number;

  transport: any = null;

  loading = false;

  showPopup = false;

  popupTitle = '';

  popupMessage = '';

  shouldRedirect = false;

  constructor(
    private readonly transportService: TransportService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef
  ) {}

  loadTransport(): void {

    if (!this.transportId) {

      this.popupTitle = 'Validation Error';

      this.popupMessage =
        'Please enter Transport ID';

      this.showPopup = true;

      return;

    }

    this.loading = true;

    this.transportService
      .getTransportById(this.transportId)
      .subscribe({
        next: (response: any) => {
          this.transport = response;
          if (!this.transport.seats) {
            this.transport.seats = [];
          }
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (error: any) => {
          console.error(error);
          this.loading = false;
          this.popupTitle = 'Error';
          this.popupMessage = error?.error?.message ?? 'Transport not found';
          this.showPopup = true;
          this.cdr.detectChanges();
        }
      });

  }

  addSeat(): void {

    this.transport.seats.push({

      transportClass: 'SEATER',

      price: 0,

      totalSeats: 1

    });

  }


  updateTransport(): void {

    const payload = {

      transportNumber:
        this.transport.transportNumber,

      source:
        this.transport.source,

      destination:
        this.transport.destination,

      transportType:
        this.transport.transportType,

      departureTime:
        this.transport.departureTime,

      arrivalTime:
        this.transport.arrivalTime,

      transportStatus:
        this.transport.transportStatus,

      partnerId:
        this.transport.partnerId,

      seats:
        this.transport.seats

    };

    this.loading = true;

    this.transportService
      .updateTransport(
        this.transport.transportId,
        payload
      )
      .subscribe({
        next: () => {
          this.loading = false;
          this.popupTitle = 'Success';
          this.popupMessage = 'Transport updated successfully';
          this.showPopup = true;
          this.shouldRedirect = true;
          this.cdr.detectChanges();
        },
        error: (error: any) => {
          console.error(error);
          this.loading = false;
          this.popupTitle = 'Error';
          this.popupMessage = error?.error?.message ?? 'Failed to update transport';
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
        '/admin/transport/view'
      ]);

    }

  }
  isTransportTimingValid(): boolean {

  if (
    !this.transport?.departureTime ||
    !this.transport?.arrivalTime ||
    this.transport?.arrivalDayOffset === null ||
    this.transport?.arrivalDayOffset === undefined
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

}