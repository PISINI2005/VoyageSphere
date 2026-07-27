import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Complaint, ComplaintStatusUpdateDTO } from '../../../core/models/compliance.model';
import { ComplaintStatus } from '../../../core/enums/compliance-enums';
import { ComplaintService } from '../../../core/services/complaint';

@Component({
  selector: 'app-complaint-update-status',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './complaint-update-status.html',
  styleUrls: ['./complaint-update-status.css'],
})
export class ComplaintUpdateStatus implements OnChanges {
  @Input() complaint: Complaint | null = null;
  @Output() statusUpdated = new EventEmitter<Complaint>();

  selectedStatus = ComplaintStatus.Open;
  resolutionNote = '';
  isSaving = false;
  error = '';
  statusOptions = Object.values(ComplaintStatus);

  constructor(private complaintService: ComplaintService) {}

  ngOnChanges(_changes: SimpleChanges): void {
    if (this.complaint) {
      this.selectedStatus = this.complaint.status;
      this.resolutionNote = this.complaint.resolutionNote ?? '';
      this.error = '';
    }
  }

  submit(): void {
    if (!this.complaint) {
      return;
    }

    this.isSaving = true;
    this.error = '';

    const dto: ComplaintStatusUpdateDTO = {
      status: this.selectedStatus,
      resolutionNote: this.resolutionNote,
    };

    this.complaintService.updateStatus(this.complaint.complaintId, dto).subscribe({
      next: (updatedComplaint) => {
        this.statusUpdated.emit(updatedComplaint);
        this.isSaving = false;
       alert('Complaint Status successfully updated')
      },
      error: (err) => {
        this.error = 'Unable to update complaint status. Please try again.';
        console.error('Complaint update error', err);
        this.isSaving = false;
      },
    });
     
  }
}
