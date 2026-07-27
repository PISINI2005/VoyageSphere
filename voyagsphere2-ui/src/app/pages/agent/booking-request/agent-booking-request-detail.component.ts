import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AgentNavbar } from '../../../layout/agent-navbar/agent-navbar';
import { BookingRequestService } from '../../../core/services/booking-request.service';
import { BookingRequestResponseDTO, BookingRequestSubmitDTO, BookingRequestRejectDTO } from '../../../core/models/travel.model';

@Component({
  selector: 'app-agent-booking-request-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, AgentNavbar, RouterModule],
  templateUrl: './agent-booking-request-detail.html',
  styleUrls: ['./agent-booking-request-detail.css']
})
export class AgentBookingRequestDetailComponent implements OnInit {
  request: BookingRequestResponseDTO | null = null;
  isLoading = false;
  showRejectModal = false;

  // Forms
  rejectionRemarks = '';
  submitForm: BookingRequestSubmitDTO = {
    agentRemarks: '',
    linkedBookingIds: []
  };
  bookingIdInput = '';

  constructor(
    private route: ActivatedRoute,
    private bookingRequestService: BookingRequestService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.params['id'];
    if (id) {
      this.loadRequest(id);
    }
  }

  loadRequest(id: number): void {
    this.isLoading = true;
    this.bookingRequestService.getRequestById(id).subscribe({
      next: (data) => {
        this.request = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading request details:', err);
        this.isLoading = false;
        alert('Error loading request details');
      }
    });
  }

  claimRequest(): void {
    if (!this.request) return;
    this.bookingRequestService.claimRequest(this.request.bookingRequestId).subscribe({
      next: (response) => {
        alert('Request claimed successfully');
        this.request = response;
      },
      error: (err) => {
        console.error('Error claiming request:', err);
        const errorMsg = err.error?.message || 'Failed to claim request';
        alert(errorMsg);
      }
    });
  }

  acceptRequest(): void {
    if (!this.request) return;
    this.bookingRequestService.acceptRequest(this.request.bookingRequestId).subscribe({
      next: (response) => {
        alert('Request accepted successfully');
        this.request = response;
      },
      error: (err) => {
        console.error('Error accepting request:', err);
        alert('Failed to accept request');
      }
    });
  }


  rejectRequest(): void {
    if (!this.request || !this.rejectionRemarks.trim()) {
      alert('Please provide rejection remarks');
      return;
    }

    const rejectDto: BookingRequestRejectDTO = {
      remarks: this.rejectionRemarks
    };

    this.bookingRequestService.rejectRequest(this.request.bookingRequestId, rejectDto).subscribe({
      next: (response) => {
        alert('Request rejected successfully');
        this.request = response;
        this.router.navigate(['/agent/booking-request']);
      },
      error: (err) => {
        console.error('Error rejecting request:', err);
        alert('Failed to reject request');
      }
    });
  }

  addBookingId(): void {
    const value = this.bookingIdInput?.toString().trim();
    if (value) {
      this.submitForm.linkedBookingIds.push(Number(value));
      this.bookingIdInput = '';
    }
  }

  removeBookingId(index: number): void {
    this.submitForm.linkedBookingIds.splice(index, 1);
  }

  submitFulfillment(): void {
    if (!this.request) return;
    if (!this.submitForm.agentRemarks.trim()) {
      alert('Agent remarks are required');
      return;
    }
    if (this.submitForm.linkedBookingIds.length === 0) {
      alert('At least one linked booking ID is required');
      return;
    }

    this.bookingRequestService.submitFulfillment(this.request.bookingRequestId, this.submitForm).subscribe({
      next: (response) => {
        alert('Booking request fulfilled successfully');
        this.request = response;
        this.router.navigate(['/agent/booking-request']);
      },
      error: (err) => {
        console.error('Error submitting fulfillment:', err);
        alert('Failed to submit fulfillment');
      }
    });
  }
}
