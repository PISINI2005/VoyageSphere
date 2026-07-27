import {
  ChangeDetectorRef,
  Component
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HotelService } from '../../../../core/services/hotel';



@Component({
  selector: 'app-update-hotel',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './update-hotel.html',
  styleUrl: './update-hotel.css'
})
export class UpdateHotel {

  hotelId!: number;

  hotel: any = null;

  loading = false;

  showPopup = false;

  popupTitle = '';

  popupMessage = '';

  shouldRedirect = false;

  constructor(
    private readonly hotelService: HotelService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef
  ) {}

  loadHotel(): void {

    if (!this.hotelId) {

      this.popupTitle = 'Validation Error';

      this.popupMessage =
        'Please enter Hotel ID';

      this.showPopup = true;

      return;

    }

    this.loading = true;

    this.hotelService
      .getHotelById(this.hotelId)
      .subscribe({

        next: (response) => {

          this.hotel = response;

          if (!this.hotel.rooms) {

            this.hotel.rooms = [];

          }

          this.loading = false;

          this.cdr.detectChanges();

        },

        error: (error) => {

          console.error(error);

          this.loading = false;

          this.popupTitle = 'Error';

          this.popupMessage =
            error?.error?.message ??
            'Hotel not found';

          this.showPopup = true;

          this.cdr.detectChanges();

        }

      });

  }

  addRoom(): void {

    this.hotel.rooms.push({

      roomType: 'STANDARD',

      price: 0,

      totalRooms: 1

    });

  }


  updateHotel(): void {

    this.loading = true;

    const payload = {

      hotelName: this.hotel.hotelName,

      ratings: this.hotel.ratings,

      city: this.hotel.city,

      address: this.hotel.address,

      contactNo: this.hotel.contactNo,

      emailId: this.hotel.emailId,

      status: this.hotel.status,

      partnerId: this.hotel.partnerId,

      rooms: this.hotel.rooms

    };

    this.hotelService
      .updateHotel(
        this.hotel.hotelId,
        payload
      )
      .subscribe({

        next: () => {

          this.loading = false;

          this.popupTitle = 'Success';

          this.popupMessage =
            'Hotel updated successfully';

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
            'Failed to update hotel';

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
        '/admin/hotel/view'
      ]);

    }

  }

}