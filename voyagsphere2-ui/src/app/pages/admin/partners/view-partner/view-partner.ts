import {
  ChangeDetectorRef,
  Component,
  OnInit
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { RouterLink } from '@angular/router';
import { PartnerResponseDTO } from '../../../../core/models/admin.model';
import { PartnerService } from '../../../../core/services/partner';
import { PartnerStatus } from '../../../../core/enums/admin-enums';



@Component({
  selector: 'app-view-partner',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    RouterLink
  ],
  templateUrl: './view-partner.html',
  styleUrl: './view-partner.css'
})
export class ViewPartner implements OnInit {

  partners: PartnerResponseDTO[] = [];

  filteredPartners: PartnerResponseDTO[] = [];

  searchQuery = '';

  selectedType = '';

  selectedStatus = '';

  partnerTypes: string[] = [];

  partnerStatuses = [
    'ACTIVE',
    'INACTIVE',
    'SUSPENDED',
    'UNDER_REVIEW',
    'TERMINATED'
  ];

  showPopup = false;

  popupTitle = '';

  popupMessage = '';

  constructor(
    private readonly partnerService: PartnerService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {

    this.loadPartners();

  }

  loadPartners(): void {

    this.partnerService
      .getAllPartners()
      .subscribe({

        next: (response) => {

          this.partners = response;

          this.partnerTypes = [
            ...new Set(
              response.map(
                partner => partner.type
              )
            )
          ];

          this.applyFilters();

          this.cdr.detectChanges();

        },

        error: (error) => {

          console.error(
            'Error loading partners',
            error
          );

        }

      });

  }

  applyFilters(): void {

    this.filteredPartners =
      this.partners.filter(partner => {

        const matchesSearch =
          partner.name
            .toLowerCase()
            .includes(
              this.searchQuery.toLowerCase()
            );

        const matchesType =
          !this.selectedType ||
          partner.type === this.selectedType;

        const matchesStatus =
          !this.selectedStatus ||
          partner.status === this.selectedStatus;

        return (
          matchesSearch &&
          matchesType &&
          matchesStatus
        );

      });

    this.cdr.detectChanges();

  }

  onSearchChange(): void {

    this.applyFilters();

  }

  onFilterChange(): void {

    this.applyFilters();

  }

  updateStatus(
    partner: PartnerResponseDTO,
    status: any
  ): void {
    const typedStatus = status as PartnerStatus;

    const payload = {
      status: typedStatus
    };

    this.partnerService
      .updatePartnerStatus(
        partner.partnerId,
        payload
      )
      .subscribe({

        next: () => {

          partner.status = typedStatus as any;

          this.applyFilters();

          this.popupTitle = 'Success';

          this.popupMessage =
            'Partner status updated successfully';

          this.showPopup = true;

          this.cdr.detectChanges();

        },

        error: (error) => {

          this.popupTitle = 'Error';

          this.popupMessage =
            error?.error?.message ??
            'Failed to update partner status';

          this.showPopup = true;

          this.cdr.detectChanges();

        }

      });

  }

  closePopup(): void {

    this.showPopup = false;

  }

}