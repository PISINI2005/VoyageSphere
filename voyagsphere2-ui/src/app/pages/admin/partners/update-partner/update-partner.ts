import {
  ChangeDetectorRef,
  Component
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PartnerResponseDTO } from '../../../../core/models/admin.model';
import { PartnerService } from '../../../../core/services/partner';



@Component({
  selector: 'app-update-partner',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './update-partner.html',
  styleUrl: './update-partner.css'
})
export class UpdatePartner {

  partnerId!: number;

  partner: PartnerResponseDTO | null = null;

  selectedType = '';

  selectedStatus = '';

  isLoading = false;

  showPopup = false;

  popupTitle = '';

  popupMessage = '';

  shouldRedirect = false;

  partnerTypes = [
    'FLIGHT',
    'HOTEL',
    'BUS',
    'PACKAGE'
  ];

  partnerStatuses = [
    'ACTIVE',
    'INACTIVE',
    'SUSPENDED',
    'UNDER_REVIEW',
    'TERMINATED'
  ];

  constructor(
    private readonly partnerService: PartnerService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef
  ) {}

  loadPartner(): void {

    if (!this.partnerId) {

      this.popupTitle = 'Validation Error';

      this.popupMessage =
        'Please enter a Partner ID';

      this.showPopup = true;

      this.cdr.detectChanges();

      return;
    }

    this.isLoading = true;

    this.partnerService
      .getPartnerById(this.partnerId)
      .subscribe({

        next: (response) => {

          this.partner = response;

          this.selectedType =
            response.type;

          this.selectedStatus =
            response.status;

          this.isLoading = false;

          this.cdr.detectChanges();
        },

        error: (error) => {

          this.isLoading = false;

          this.popupTitle = 'Error';

          this.popupMessage =
            error?.error?.message ??
            'Partner not found';

          this.showPopup = true;

          this.cdr.detectChanges();
        }

      });
  }

  updatePartner(): void {

    if (!this.partner) {

      this.popupTitle = 'Validation Error';

      this.popupMessage =
        'Please load a partner first';

      this.showPopup = true;

      this.cdr.detectChanges();

      return;
    }

    this.isLoading = true;

    this.partner.type =
      this.selectedType as any;

    this.partner.status =
      this.selectedStatus as any;

    this.partnerService
      .updatePartner(
        this.partner.partnerId,
        this.partner
      )
      .subscribe({

        next: () => {

          this.isLoading = false;

          this.popupTitle = 'Success';

          this.popupMessage =
            'Partner updated successfully';

          this.showPopup = true;

          this.shouldRedirect = true;

          this.cdr.detectChanges();
        },

        error: (error) => {

          this.isLoading = false;

          this.popupTitle = 'Error';

          this.popupMessage =
            error?.error?.message ??
            'Failed to update partner';

          this.showPopup = true;

          this.cdr.detectChanges();
        }

      });
  }

  closePopup(): void {

    this.showPopup = false;

    this.cdr.detectChanges();

    if (this.shouldRedirect) {

      this.shouldRedirect = false;

      this.resetForm();

      this.router.navigate([
        '/admin/partners/view'
      ]);
    }
  }

  resetForm(): void {

    this.partnerId = 0 as any;

    this.partner = null;

    this.selectedType = '';

    this.selectedStatus = '';
  }

}