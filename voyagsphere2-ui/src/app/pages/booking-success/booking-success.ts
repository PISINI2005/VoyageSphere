import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-booking-success',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './booking-success.html',
  styleUrl: './booking-success.css'
})
export class BookingSuccessComponent {

  bookingId!: number;

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.bookingId =
      +this.route.snapshot.params['bookingId'];
  }

  viewTrips() {
    this.router.navigate(['/trips']);
  }
}