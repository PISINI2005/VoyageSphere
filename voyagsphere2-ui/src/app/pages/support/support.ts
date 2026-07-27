import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { NavbarComponent } from '../../layout/navbar/navbar';
import { ComplaintService } from '../../core/services/complaint';
import { ComplaintRequestDTO, ComplaintResponseDTO } from '../../core/models/travel.model';

@Component({
  selector: 'app-support',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NavbarComponent
  ],
  templateUrl: './support.html',
  styleUrl: './support.css'
})
export class SupportComponent implements OnInit {

  complaintForm: ComplaintRequestDTO = {
    subject: '',
    description: '',
    targetType: 'BOOKING',
    targetId: 0
  };

  complaints: ComplaintResponseDTO[] = [];

  constructor(private complaintService: ComplaintService) {}

  ngOnInit(): void {
    this.loadComplaints();
  }

  loadComplaints(): void {
    this.complaintService.getMyComplaints().subscribe({
      next: (data) => this.complaints = data,
      error: (err) => console.error('Error loading complaints:', err)
    });
  }

  submitComplaint(): void {
    if (!this.complaintForm.subject || !this.complaintForm.description) {
      alert('Please fill in subject and description');
      return;
    }

    this.complaintService.createComplaint(this.complaintForm).subscribe({
      next: () => {
        alert('Complaint submitted successfully');
        this.loadComplaints();
        this.complaintForm = {
          subject: '',
          description: '',
          targetType: 'BOOKING',
          targetId: 0
        };
      },
      error: (err) => console.error('Error submitting complaint:', err)
    });
  }
}
