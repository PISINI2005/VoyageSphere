import {
  ChangeDetectorRef,
  Component
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PartnerDTO } from '../../../../core/models/admin.model';
import { PartnerStatus, PartnerType } from '../../../../core/enums/admin-enums';
import { PartnerService } from '../../../../core/services/partner';


@Component({
  selector: 'app-add-partner',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './add-partner.html',
  styleUrl: './add-partner.css'
})
export class AddPartner {

  isLoading = false;

  showPopup = false;

  popupTitle = '';

  popupMessage = '';

  partner: PartnerDTO = {

    name: '',

    type: PartnerType.FLIGHT,

    status: PartnerStatus.ACTIVE

  };

  readonly partnerTypes =
    Object.values(PartnerType);

  readonly partnerStatuses =
    Object.values(PartnerStatus);

  constructor(
    private readonly partnerService: PartnerService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef
  ) {}

  onSubmit(): void {

    console.log('Partner Payload:', this.partner);
    console.log(
      '[AddPartner] Form Submitted'
    );

    this.showPopup = false;

    if (!this.partner.name?.trim()) {

      this.popupTitle =
        'Validation Error';

      this.popupMessage =
        'Partner name is required';

      this.showPopup = true;

      this.cdr.detectChanges();

      return;

    }

    this.addPartner();

  }

  addPartner(): void {

    this.isLoading = true;

    console.log(
      '[AddPartner] Payload:',
      this.partner
    );

    this.partnerService
      .createPartner(this.partner)
      .subscribe({

        next: (response) => {

          console.log(
            '[AddPartner] Success Response:',
            response
          );

          this.isLoading = false;

          this.popupTitle =
            'Success';

          this.popupMessage =
            'Partner added successfully';

          this.showPopup = true;

          this.cdr.detectChanges();

        },

        error: (error) => {

          console.error(
            '[AddPartner] Error:',
            error
          );

          console.error(
            '[AddPartner] Backend Response:',
            error?.error
          );

          this.isLoading = false;

          this.popupTitle =
            'Error';

          this.popupMessage =
            error?.error?.message ??
            'Failed to add partner';

          this.showPopup = true;

          this.cdr.detectChanges();

        },

        complete: () => {

          console.log(
            '[AddPartner] Request Completed'
          );

          this.isLoading = false;

          this.cdr.detectChanges();

        }

      });

  }

  closePopup(): void {

    console.log(
      '[AddPartner] Popup Closed'
    );

    this.showPopup = false;

    this.cdr.detectChanges();

    if (
      this.popupTitle === 'Success'
    ) {

      this.resetForm();

      this.router.navigate([
        'admin/partners/view'
      ]);

    }

  }

  resetForm(): void {

    this.partner = {

      name: '',

      type: PartnerType.FLIGHT,

      status: PartnerStatus.ACTIVE

    };

  }

}