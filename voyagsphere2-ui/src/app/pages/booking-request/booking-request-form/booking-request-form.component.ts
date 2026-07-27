import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { NavbarComponent } from '../../../layout/navbar/navbar';
import { BookingRequestService } from '../../../core/services/booking-request.service';
import { BookingRequestCreateDTO } from '../../../core/models/travel.model';

@Component({
  selector: 'app-booking-request-form',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, RouterModule],
  templateUrl: './booking-request-form.html',
  styleUrls: ['./booking-request-form.css']
})
export class BookingRequestFormComponent {
  requestForm: BookingRequestCreateDTO = {
    type: 'BOOKING',
    budget: 0,
    requestDetails: '',
  };

  constructor(
    private bookingRequestService: BookingRequestService,
    public router: Router
  ) {}

  submitRequest(): void {
    if (!this.requestForm.requestDetails) {
      alert('Please provide the request details');
      return;
    }

    if (!this.requestForm.budget || this.requestForm.budget <= 0) {
      alert('Please enter a positive budget amount');
      return;
    }

    this.bookingRequestService.createBookingRequest(this.requestForm).subscribe({
      next: () => {
        alert('Booking request submitted successfully');
        this.router.navigate(['/booking-request/my-requests']);
      },
      error: (err) => {
        console.error('Error submitting request:', err);
        alert('Failed to submit request. Please check your inputs.');
      }
    });
  }
}
