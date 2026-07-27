import {
  ChangeDetectorRef,
  Component
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TravelPackageService } from '../../../../core/services/travel-package';



@Component({
  selector: 'app-update-package',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './update-travelpackage.html',
  styleUrl: './update-travelpackage.css'
})
export class UpdatePackage {

  packageId!: number;

  packageData: any = null;

  loading = false;

  showPopup = false;

  popupTitle = '';

  popupMessage = '';

  shouldRedirect = false;

  constructor(
    private readonly packageService: TravelPackageService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef
  ) {}

  loadPackage(): void {

    if (!this.packageId) {

      this.popupTitle = 'Validation Error';

      this.popupMessage =
        'Please enter Package ID';

      this.showPopup = true;

      this.cdr.detectChanges();

      return;

    }

    this.loading = true;

    this.packageService
      .getPackageById(this.packageId)
      .subscribe({

        next: (response) => {

          this.packageData = response;

          this.loading = false;

          console.log(response);

          this.cdr.detectChanges();

        },

        error: (error) => {

          console.error(error);

          this.loading = false;

          this.popupTitle = 'Error';

          this.popupMessage =
            error?.error?.message ??
            'Package not found';

          this.showPopup = true;

          this.cdr.detectChanges();

        }

      });

  }

  updatePackage(): void {

    this.loading = true;

    const payload = {

      packageName:
        this.packageData.packageName,

      source:
        this.packageData.source,

      destination:
        this.packageData.destination,

      price:
        this.packageData.price,

      durationDays:
        this.packageData.durationDays,

      totalSlots:
        this.packageData.totalSlots,

      description:
        this.packageData.description,

      dayWisePlan:
        this.packageData.dayWisePlan,

      category:
        this.packageData.category,

      status:
        this.packageData.status,

      partnerId:
        this.packageData.partnerId

    };

    this.packageService
      .updatePackage(
        this.packageData.packageId,
        payload
      )
      .subscribe({

        next: () => {

          this.loading = false;

          this.popupTitle = 'Success';

          this.popupMessage =
            'Package updated successfully';

          this.showPopup = true;

          this.shouldRedirect = true;

          this.cdr.detectChanges();

        },

        error: (error) => {

          console.error(error);

          this.loading = false;

          this.popupTitle = 'Error';

          this.popupMessage =
            error?.error?.message ??
            'Failed to update package';

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

      this.router.navigate([
        'admin/package/view'
      ]);

    }

  }

}