import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { NavbarComponent } from '../../../layout/navbar/navbar';
import { BookingRequestService } from '../../../core/services/booking-request.service';
import { BookingRequestResponseDTO, BookingRequestFeedbackDTO } from '../../../core/models/travel.model';

@Component({
  selector: 'app-booking-request-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, RouterModule],
  templateUrl: './booking-request-dashboard.html',
  styleUrls: ['./booking-request-dashboard.css']
})
export class BookingRequestDashboardComponent implements OnInit {
  requests: BookingRequestResponseDTO[] = [];
  isLoading = false;

  showFeedbackModal = false;
  selectedRequestId: number = 0;

  feedbackForm = {
    customerStatus: 'SATISFIED' as 'SATISFIED' | 'MODIFICATION_REQUIRED',
    modificationDetails: ''
  };

  constructor(
    private bookingRequestService: BookingRequestService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadMyRequests();
  }

  loadMyRequests(): void {
    this.isLoading = true;
    this.bookingRequestService.getMyBookingRequests().subscribe({
      next: (data: any) => {
        this.requests = data?.content ?? data ?? [];
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading requests:', err);
        this.isLoading = false;
      }
    });
  }

  openFeedbackModal(request: BookingRequestResponseDTO): void {
    this.selectedRequestId = request.bookingRequestId;
    this.feedbackForm = {
      customerStatus: 'SATISFIED',
      modificationDetails: ''
    };
    this.showFeedbackModal = true;
  }

  closeFeedbackModal(): void {
    this.showFeedbackModal = false;
  }

  submitFeedback(): void {
    if (!this.selectedRequestId) {
      alert('No request selected for feedback');
      return;
    }

    if (this.feedbackForm.customerStatus === 'MODIFICATION_REQUIRED' && !this.feedbackForm.modificationDetails?.trim()) {
      alert('Please provide modification details when requesting changes');
      return;
    }

    this.bookingRequestService.updateFeedback(this.selectedRequestId, this.feedbackForm).subscribe({
      next: () => {
        alert('Feedback submitted successfully');
        this.closeFeedbackModal();
        this.loadMyRequests();
      },
      error: (err) => {
        console.error('Error submitting feedback:', err);
        alert('Failed to submit feedback');
      }
    });
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'PENDING': return 'bg-warning text-dark';
      case 'CLAIMED': return 'bg-primary';
      case 'AWAITING_FEEDBACK': return 'bg-info text-dark';
      case 'COMPLETED': return 'bg-success';
      case 'REJECTED': return 'bg-danger';
      default: return 'bg-secondary';
    }
  }
}
