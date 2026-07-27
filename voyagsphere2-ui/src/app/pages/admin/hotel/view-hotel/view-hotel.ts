import {
  ChangeDetectorRef,
  Component,
  OnInit
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';



import { HotelService } from '../../../../core/services/hotel';
import { HotelResponseDTO } from '../../../../core/models/admin.model';
import { HotelStatus } from '../../../../core/enums/admin-enums';

@Component({
  selector: 'app-view-hotel',
  standalone: true,
  imports: [
    FormsModule,
    CommonModule,
    RouterLink
  ],
  templateUrl: './view-hotel.html',
  styleUrl: './view-hotel.css'
})
export class ViewHotel implements OnInit {

  hotels: HotelResponseDTO[] = [];

  city = '';

  ratings?: number;

  minPrice?: number;

  maxPrice?: number;

  loading = false;

  showPopup = false;

  popupTitle = '';

  popupMessage = '';

  currentPage = 0;

pageSize = 10;

totalPages = 0;

totalElements = 0;

isFirst = true;

isLast = false;

pages: number[] = [];

  constructor(
    private readonly hotelService: HotelService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {

    this.searchHotels();

  }

  searchHotels(): void {

  this.loading = true;

  this.hotelService
    .searchHotels(
      this.city,
      this.ratings,
      this.minPrice,
      this.maxPrice,
      this.currentPage,
      this.pageSize
    )
    .subscribe({

      next: (response: any) => {

        this.hotels = response.content;

        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;

        this.currentPage = response.number;
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

        this.openPopup(
          'Error',
          'Failed to load hotels'
        );

      }

    });

}

  resetSearch(): void {

  this.city = '';

  this.ratings = undefined;

  this.minPrice = undefined;

  this.maxPrice = undefined;

  this.currentPage = 0;

  this.searchHotels();

}

  updateStatus(
    hotel: HotelResponseDTO,
    status: any
  ): void {
    const typedStatus = status as HotelStatus;

    this.hotelService
      .updateHotelStatus(
        hotel.hotelId,
        {
          status: typedStatus
        }
      )
      .subscribe({

        next: () => {

          hotel.status = typedStatus;

          this.openPopup(
            'Success',
            `Hotel status updated to ${typedStatus}`
          );

        },

        error: (error) => {

          console.error(error);

          this.openPopup(
            'Error',
            error?.error?.message ??
            'Failed to update hotel status'
          );

        }

      });

  }

  openPopup(
    title: string,
    message: string
  ): void {

    this.popupTitle = title;

    this.popupMessage = message;

    this.showPopup = true;

    this.cdr.detectChanges();

  }

  closePopup(): void {

    this.showPopup = false;

    this.cdr.detectChanges();

  }
previousPage(): void {

  if (!this.isFirst) {

    this.currentPage--;

    this.searchHotels();

  }

}

nextPage(): void {

  if (!this.isLast) {

    this.currentPage++;

    this.searchHotels();

  }

}

goToPage(page: number): void {

  if (page !== this.currentPage) {

    this.currentPage = page;

    this.searchHotels();

  }

}
}