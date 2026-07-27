import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AgentNavbar } from '../../../layout/agent-navbar/agent-navbar';
import { BookingService } from '../../../core/services/booking';
import { BookingResponseDTO } from '../../../core/models/travel.model';

@Component({
  selector: 'app-bookings',
  standalone: true,
  imports: [CommonModule, AgentNavbar],
  templateUrl: './bookings.html',
  styleUrl: './bookings.css',
})
export class Bookings implements OnInit {
  allBookings: BookingResponseDTO[] = [];
  isLoading = true;

  constructor(
    private bookingService: BookingService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadAllBookings();
  }

  loadAllBookings(): void {
    this.bookingService.getAllBookings().subscribe({
      next: (data:any) => {
        console.log(data);
        this.allBookings = data.content;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading all bookings:', err);
        this.isLoading = false;
      }
    });
  }

  viewDetails(bookingId: number): void {
    this.router.navigate(['/agent/booking-details', bookingId]);
  }
}
