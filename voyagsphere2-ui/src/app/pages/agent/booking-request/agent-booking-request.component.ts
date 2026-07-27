import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AgentNavbar } from '../../../layout/agent-navbar/agent-navbar';
import { BookingRequestService } from '../../../core/services/booking-request.service';
import { BookingRequestResponseDTO } from '../../../core/models/travel.model';

@Component({
  selector: 'app-agent-booking-request',
  standalone: true,
  imports: [CommonModule, FormsModule, AgentNavbar, RouterModule],
  templateUrl: './agent-booking-request.html',
  styleUrls: ['./agent-booking-request.css']
})
export class AgentBookingRequestComponent implements OnInit {
  requests: BookingRequestResponseDTO[] = [];
  isLoading = false;

  // View Mode
  viewMode: 'ALL' | 'MY_REQUESTS' = 'ALL';

  // Pagination and Filtering
  selectedStatus = 'PENDING';
  currentPage = 0;
  pageSize = 5;
  totalPages = 0;
  totalElements = 0;

  constructor(
    private bookingRequestService: BookingRequestService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadRequests();
  }

  loadRequests(): void {
    this.isLoading = true;

    if (this.viewMode === 'ALL') {
      this.bookingRequestService.getBookingRequests(this.selectedStatus, this.currentPage, this.pageSize).subscribe({
        next: (data: any) => {
          this.requests = data?.content ?? [];
          this.totalPages = data?.totalPages ?? 0;
          this.totalElements = data?.totalElements ?? 0;
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error loading requests:', err);
          this.isLoading = false;
          alert('Error loading booking requests');
        }
      });
    } else {
      this.bookingRequestService.getAgentBookingRequests(this.currentPage, this.pageSize).subscribe({
        next: (data: any) => {
          this.requests = data?.content ?? [];
          this.totalPages = data?.totalPages ?? 0;
          this.totalElements = data?.totalElements ?? 0;
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error loading my requests:', err);
          this.isLoading = false;
          alert('Error loading your requests');
        }
      });
    }
  }

  onStatusChange(status: string): void {
    this.selectedStatus = status;
    this.currentPage = 0;
    this.loadRequests();
  }

  toggleViewMode(mode: 'ALL' | 'MY_REQUESTS'): void {
    this.viewMode = mode;
    this.currentPage = 0;
    this.loadRequests();
  }

  changePage(page: number): void {
    this.currentPage = page;
    this.loadRequests();
  }

  claimRequest(id: number): void {
    this.bookingRequestService.claimRequest(id).subscribe({
      next: (response) => {
        alert('Request claimed successfully');
        this.loadRequests();
        this.router.navigate(['/agent/booking-request-detail', id]);
      },
      error: (err) => {
        console.error('Error claiming request:', err);
        const errorMsg = err.error?.message || 'Failed to claim request';
        alert(errorMsg);
      }
    });
  }

  viewRequest(id: number): void {
    this.router.navigate(['/agent/booking-request-detail', id]);
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'PENDING': return 'bg-warning text-dark';
      case 'ASSIGNED': return 'bg-primary';
      case 'ACCEPTED': return 'bg-info text-dark';
      case 'AWAITING_FEEDBACK': return 'bg-info text-dark';
      case 'COMPLETED': return 'bg-success';
      case 'REJECTED': return 'bg-danger';
      default: return 'bg-secondary';
    }
  }
}
