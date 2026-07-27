import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Complaint } from '../../../core/models/compliance.model';
import { ComplaintStatus } from '../../../core/enums/compliance-enums';
import { ComplaintService } from '../../../core/services/complaint';
import { AuthService } from '../../../core/services/auth';


@Component({
  selector: 'app-complaint-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './complaint.html',
  styleUrls: ['./complaint.css'],
})
export class ComplaintList implements OnInit {
  complaints: Complaint[] = [];
  allComplaints: Complaint[] = [];
  statusFilter = '';
  searchTerm = '';
  isLoading = false;
  error = '';

  statusOptions = [
    { label: 'All', value: '' },
    { label: 'Open', value: ComplaintStatus.Open },
    { label: 'In Progress', value: ComplaintStatus.InProgress },
    { label: 'Resolved', value: ComplaintStatus.Resolved },
    { label: 'Closed', value: ComplaintStatus.Closed },
  ];

  // Data now comes exclusively from backend via `ComplaintService`.
  isRefreshing = false;
  dataLoaded = false;

  constructor(
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
    this.fetchComplaints();
  }

  fetchComplaints(): void {
    const initialLoad = !this.dataLoaded;
    if (initialLoad) {
      this.isLoading = true;
    } else {
      this.isRefreshing = true;
    }

    this.error = '';

    // Immediately reflect filter selections using current local data,
    // then refresh from backend in the background.
    if (this.dataLoaded) {
      this.applyFilters();
    }

    const status = this.statusFilter || undefined;

    this.complaintService.getAllComplaints(status).subscribe({
      next: (complaints) => {
        this.allComplaints = complaints || [];
        console.log(this.allComplaints)
        this.dataLoaded = true;
        this.applyFilters();
        this.isLoading = false;
        this.isRefreshing = false;
        this.cdr.detectChanges();
        console.log(complaints);
      },
      error: (err) => {
        console.warn('Failed to fetch complaints from backend', err);
        this.error = 'Failed to load complaints';
        if (!this.dataLoaded) {
          this.allComplaints = [];
          this.complaints = [];
        }
        this.isLoading = false;
        this.isRefreshing = false;
      },
    });
  }

  applyFilters(): void {
    const term = this.searchTerm.trim().toLowerCase();
    const status = this.statusFilter;

    let filtered = [...this.allComplaints];
    if (status) {
      filtered = filtered.filter((complaint) => complaint.status === status);
    }

    if (!term) {
      this.complaints = filtered;
      return;
    }

    this.complaints = filtered.filter((complaint) => {
      console.log(complaint.userId);
      return [
        complaint.complaintId?.toString() ?? '',
        complaint.subject,
        complaint.userId,
        complaint.status,
        complaint.targetId,
      ]
        .join(' ')
        .toLowerCase()
        .includes(term);
        
    });
  }
}
