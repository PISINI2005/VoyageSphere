import {
  ChangeDetectorRef,
  Component,
  OnInit
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TravelPackageService } from '../../../../core/services/travel-package';



@Component({
  selector: 'app-view-package',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink
  ],
  templateUrl: './view-travelpackage.html',
  styleUrl: './view-travelpackage.css'
})
export class ViewPackage implements OnInit {

packages: any[] = [];

// Pagination
currentPage = 0;
pageSize = 5;
totalPages = 0;
totalElements = 0;



pages: number[] = [];

isFirst = true;
isLast = false;


  category = '';

  loading = false;

  showPopup = false;

  popupTitle = '';

  popupMessage = '';

  selectedPlan = '';

  showPlanPopup = false;

  constructor(
    private readonly packageService: TravelPackageService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {

    this.searchPackages();

  }

 searchPackages(page: number = this.currentPage): void {
  this.loading = true;
  this.currentPage = page;

  this.packageService
    .getAllPackages(
      this.category,
      this.currentPage,
      this.pageSize
    )
    .subscribe({
      next: (response) => {
  this.packages = response.content ?? [];

  this.currentPage = response.number;
  this.totalPages = response.totalPages;
  this.totalElements = response.totalElements;

  this.isFirst = response.first;
  this.isLast = response.last;

  this.pages = Array.from(
    { length: this.totalPages },
    (_, i) => i
  );

  this.loading = false;
  this.cdr.detectChanges();
},
      error: (error) => {
        console.error(error);
        this.loading = false;
        this.popupTitle = 'Error';
        this.popupMessage = 'Failed to load packages';
        this.showPopup = true;
        this.cdr.detectChanges();
      }
    });
}


 resetSearch(): void {
  this.category = '';
  this.currentPage = 0;
  this.searchPackages(0);
}
  updateStatus(
    pkg: any,
    status: string
  ): void {

    this.packageService
      .updatePackageStatus(
        pkg.packageId,
        {
          status: status
        }
      )
      .subscribe({

        next: () => {

          pkg.status = status;

          this.popupTitle =
            'Success';

          this.popupMessage =
            'Package status updated successfully';

          this.showPopup = true;

          this.cdr.detectChanges();

        },

        error: (error) => {

          console.error(error);

          this.popupTitle =
            'Error';

          this.popupMessage =
            error?.error?.message ??
            'Failed to update package status';

          this.showPopup = true;

          this.cdr.detectChanges();

        }

      });

  }

  viewPlan(pkg: any): void {

    this.selectedPlan =
      pkg.dayWisePlan ?? 'No plan available';

    this.showPlanPopup = true;

    this.cdr.detectChanges();

  }

  closePlanPopup(): void {

    this.showPlanPopup = false;

    this.selectedPlan = '';

    this.cdr.detectChanges();

  }

  closePopup(): void {

    this.showPopup = false;

    this.cdr.detectChanges();

  }


  goToPage(page: number): void {
  this.searchPackages(page);
}

previousPage(): void {
  if (!this.isFirst) {
    this.searchPackages(this.currentPage - 1);
  }
}

nextPage(): void {
  if (!this.isLast) {
    this.searchPackages(this.currentPage + 1);
  }
}
}