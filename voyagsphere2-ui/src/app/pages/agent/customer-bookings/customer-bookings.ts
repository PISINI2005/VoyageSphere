import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AgentNavbar } from '../../../layout/agent-navbar/agent-navbar';
import { BookingService } from '../../../core/services/booking';
import { AgentContextService } from '../../../core/services/agent-context';
import { BookingResponseDTO } from '../../../core/models/travel.model';

@Component({
  selector: 'app-customer-bookings',
  standalone: true,
  imports: [CommonModule, AgentNavbar],
  templateUrl: './customer-bookings.html',
  styleUrl: './customer-bookings.css',
})
export class CustomerBookings implements OnInit {
  bookings: BookingResponseDTO[] = [];
  isLoading = true;

  constructor(
    private bookingService: BookingService,
    public agentContext: AgentContextService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCustomerBookings();
  }

  loadCustomerBookings(): void {
    if (!this.agentContext.selectedCustomerId) {
      this.isLoading = false;
      return;
    }

    this.bookingService.getMyBookings(
  0,
  5,
  this.agentContext.selectedCustomerId
)
.subscribe({
  next: (data: any) => {
    this.bookings = data.content;
    this.isLoading = false;
  }
});
  }

  viewDetails(bookingId: number): void {
    this.router.navigate(['/agent/booking-details', bookingId]);
  }
}
