import {
  ChangeDetectorRef,
  Component
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TravelPackageService } from '../../../../core/services/travel-package';
import { PackageStatus, TravelPackageCategory } from '../../../../core/enums/admin-enums';


@Component({
  selector: 'app-add-package',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule
  ],
  templateUrl: './add-travelpackage.html',
  styleUrl: './add-travelpackage.css'
})
export class AddPackage {

  loading = false;

  showPopup = false;

  popupTitle = '';

  popupMessage = '';

  shouldRedirect = false;

  travelPackage = {

    packageName: '',

    source: '',

    destination: '',

    price: 0,

    durationDays: 1,

    totalSlots: 1,

    description: '',

    dayWisePlan: '',

    category: TravelPackageCategory.ADVENTURE,

    status: PackageStatus.AVAILABLE,

    partnerId: 0

  };

  constructor(
    private readonly packageService: TravelPackageService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef
  ) {}

  addPackage(): void {

    this.loading = true;

    this.packageService
      .addPackage(this.travelPackage)
      .subscribe({

        next: () => {

          this.loading = false;

          this.popupTitle =
            'Success';

          this.popupMessage =
            'Package added successfully';

          this.showPopup = true;

          this.shouldRedirect = true;

          this.cdr.detectChanges();

        },

        error: (error) => {

          console.error(error);

          this.loading = false;

          this.popupTitle =
            'Error';

          this.popupMessage =
            error?.error?.message ??
            'Failed to add package';

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