import { CommonModule, DatePipe } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { ComplaintUpdateStatus } from '../complaint-update-status/complaint-update-status';
import { ComplaintService } from '../../../core/services/complaint';
import { Complaint } from '../../../core/models/compliance.model';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-complaint-details',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    ComplaintUpdateStatus,
    DatePipe
  ],
  templateUrl: './complaint-details.html',
  styleUrls: ['./complaint-details.css'],
})
export class ComplaintDetails implements OnInit {
  complaint: Complaint | null = null;
  isLoading = true;
  error = '';

  constructor(
    private route: ActivatedRoute,
    private complaintService: ComplaintService,
    private cdr: ChangeDetectorRef,
    private authService: AuthService,
    private router: Router,
  ) {}

  logout(): void {
    this.authService.logout();
    this.router.navigate(['']);
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    if (!id) {
      this.error = 'Invalid Complaint ID';
      this.isLoading = false;
      return;
    }

    this.complaintService.getComplaintById(id).subscribe({
      next: (complaint) => {
        this.complaint = complaint;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to fetch complaint', err);
        this.error = 'Complaint not found';
        this.isLoading = false;
      },
    });
  }

  onStatusUpdated(updatedComplaint: Complaint): void {
    this.complaint = { ...updatedComplaint };
  }
}
