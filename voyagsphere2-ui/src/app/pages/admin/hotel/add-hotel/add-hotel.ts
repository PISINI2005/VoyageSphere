import { ChangeDetectorRef, Component } from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HotelService } from '../../../../core/services/hotel';
import { HotelDTO } from '../../../../core/models/admin.model';

@Component({
  selector: 'app-add-hotel',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './add-hotel.html',
})
export class AddHotel {
  loading = false;

  showRoomSection = false;

  showPopup = false;

  popupTitle = '';

  popupMessage = '';

  shouldRedirect = false;

  hotel: HotelDTO = {
    hotelName: '',

    ratings: 1,

    city: '',

    address: '',

    contactNo: '',

    emailId: '',

    status: 'AVAILABLE' as any,

    partnerId: 0,

    rooms: [
      {
        roomType: 'STANDARD' as any,
        price: 0,
        totalRooms: 1,
      },
    ],
  };

  constructor(
    private readonly hotelService: HotelService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  proceedToRooms(): void {
    if (
      !this.hotel.hotelName?.trim() ||
      !this.hotel.city?.trim() ||
      !this.hotel.address?.trim() ||
      !this.hotel.contactNo?.trim() ||
      !this.hotel.emailId?.trim() ||
      !this.hotel.partnerId
    ) {
      this.popupTitle = 'Validation Error';

      this.popupMessage = 'Please fill all hotel details';

      this.showPopup = true;

      this.cdr.detectChanges();

      return;
    }

    this.showRoomSection = true;
  }

  addRoom(): void {
    this.hotel.rooms.push({
      roomType: 'STANDARD' as any,

      price: 0,

      totalRooms: 1,
    });
  }

  removeRoom(index: number): void {
    if (this.hotel.rooms.length === 1) {
      this.popupTitle = 'Validation Error';

      this.popupMessage = 'At least one room type is required';

      this.showPopup = true;

      this.cdr.detectChanges();

      return;
    }

    this.hotel.rooms.splice(index, 1);
  }

  addHotel(): void {
    this.loading = true;

    console.log('[AddHotel] Payload', this.hotel);

    this.hotelService.addHotel(this.hotel).subscribe({
      next: () => {
        this.loading = false;

        this.popupTitle = 'Success';

        this.popupMessage = 'Hotel added successfully';

        this.shouldRedirect = true;

        this.showPopup = true;

        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error(error);

        this.loading = false;

        this.popupTitle = 'Error';

        this.popupMessage = error?.error?.message ?? 'Failed to add hotel';

        this.showPopup = true;

        this.cdr.detectChanges();
      },
    });
  }

  closePopup(): void {
    this.showPopup = false;

    this.cdr.detectChanges();

    if (this.shouldRedirect) {
      this.router.navigate(['/admin/hotel/view']);
    }
  }
}
