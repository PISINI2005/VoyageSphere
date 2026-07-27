import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { NavbarComponent } from '../../layout/navbar/navbar';
import { SearchService } from '../../core/services/search';

@Component({
  selector: 'app-service-details',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NavbarComponent
  ],
  templateUrl: './service-details.html',
  styleUrl: './service-details.css'
})
export class ServiceDetailsComponent implements OnInit {

  id!: number;
  type!: string;
  item: any = null;
  selectedDate: string = '';
  selectedCategory: string = '';
  priceCalendar: any[] = [];
  isCalendarOpen: boolean = false;
  today: Date = new Date();
  viewDate: Date = new Date();
  calendarDays: any[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private searchService: SearchService
  ) {}

  ngOnInit(): void {
    this.id = +this.route.snapshot.params['id'];
    this.type = (this.route.snapshot.params['type'] || '').toUpperCase();

    this.loadItemDetails();
  }

  itinerary: any[] = [];

  loadItemDetails(): void {

    this.route.queryParams.subscribe(params => {

      this.selectedDate = params['date'] || '';

      this.searchService.getItemById(
        this.id,
        this.type,
        params['date'],
        params['checkInDate'],
        params['checkOutDate']
      ).subscribe({
        next: (data) => {

          this.item = data;

          if (
            this.type === 'PACKAGE' &&
            this.item?.dayWisePlan
          ) {
            try {
              this.itinerary = JSON.parse(this.item.dayWisePlan);
            } catch {
              this.itinerary = [];
            }
          }

          // Setup initial category for calendar
          if (this.item) {
            if (!this.selectedCategory) {
              this.initCategory();
            }
            this.loadPriceCalendar();
          }
        },
        error: (err) => {
          console.error(err);
        }
      });

    });
  }

  initCategory() {
    if (this.type === 'FLIGHT' && this.item.seats?.length > 0) {
      this.selectedCategory = this.item.seats[0].seatType;
    } else if (this.type === 'TRANSPORT' && this.item.seats?.length > 0) {
      this.selectedCategory = this.item.seats[0].transportClass;
    }
  }

  loadPriceCalendar(): void {
    if (this.type !== 'FLIGHT' && this.type !== 'TRANSPORT') return;
    if (!this.selectedCategory) return;

    this.searchService.getPriceCalendar(this.id, this.type, this.selectedCategory).subscribe({
      next: (data) => {
        this.priceCalendar = data;
        this.generateCalendarGrid();
      },
      error: (err) => console.error('Error loading price calendar:', err)
    });
  }

  generateCalendarGrid(): void {
    const year = this.viewDate.getFullYear();
    const month = this.viewDate.getMonth();
    const firstDayOfMonth = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();

    this.calendarDays = [];

    // Get base price: use the price of the last available day in the calendar,
    // otherwise fallback to the seat category's starting price.
    let basePrice: number | null = null;
    if (this.priceCalendar && this.priceCalendar.length > 0) {
      basePrice = this.priceCalendar[this.priceCalendar.length - 1].price;
    } else {
      const currentCategorySeat = this.item?.seats?.find((s: any) =>
        (s.seatType || s.transportClass) === this.selectedCategory
      );
      basePrice = currentCategorySeat ? currentCategorySeat.price : this.getItemPrice(this.item);
    }

    // Padding for the first week
    for (let i = 0; i < firstDayOfMonth; i++) {
      this.calendarDays.push({ date: null, price: null, isEmpty: true });
    }

    // Days of the month
    for (let day = 1; day <= daysInMonth; day++) {
      const dateObj = new Date(year, month, day);
      const dateStr = `${dateObj.getFullYear()}-${String(dateObj.getMonth() + 1).padStart(2, '0')}-${String(dateObj.getDate()).padStart(2, '0')}`;
      const priceData = this.priceCalendar.find(d => d.date === dateStr);

      // Only assign price if the date is not in the past
      const isPast = this.isDateDisabled(dateStr);

      this.calendarDays.push({
        date: dateStr,
        price: isPast ? null : (priceData ? priceData.price : basePrice),
        isEmpty: false
      });
    }
  }

  onCategoryChange(category: string): void {
    this.selectedCategory = category;
    this.loadPriceCalendar();
  }

  isCurrentMonth(): boolean {
    return this.viewDate.getFullYear() === this.today.getFullYear() &&
           this.viewDate.getMonth() === this.today.getMonth();
  }

  isMaxMonth(): boolean {
    const maxDate = new Date(this.today);
    maxDate.setMonth(maxDate.getMonth() + 6);
    return this.viewDate.getFullYear() === maxDate.getFullYear() &&
           this.viewDate.getMonth() >= maxDate.getMonth();
  }

  toggleCalendar(): void {
    this.viewDate = new Date(); // Reset to current month when opening
    this.generateCalendarGrid();
    this.isCalendarOpen = !this.isCalendarOpen;
  }

  nextMonth(): void {
    const maxDate = new Date(this.today);
    maxDate.setMonth(maxDate.getMonth() + 6);

    if (this.viewDate.getFullYear() === maxDate.getFullYear() &&
        this.viewDate.getMonth() >= maxDate.getMonth()) {
      return;
    }
    this.viewDate = new Date(this.viewDate.getFullYear(), this.viewDate.getMonth() + 1, 1);
    this.generateCalendarGrid();
  }

  prevMonth(): void {
    const currentMonth = this.today.getMonth();
    const currentYear = this.today.getFullYear();
    if (this.viewDate.getFullYear() === currentYear && this.viewDate.getMonth() <= currentMonth) {
      return;
    }
    this.viewDate = new Date(this.viewDate.getFullYear(), this.viewDate.getMonth() - 1, 1);
    this.generateCalendarGrid();
  }

  selectDate(date: string): void {
    this.selectedDate = date;
    this.isCalendarOpen = false;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { date: date },
      queryParamsHandling: 'merge'
    });
  }

  isDateDisabled(dateStr: string): boolean {
    if (!dateStr) return true;
    const date = new Date(dateStr);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return date < today;
  }

  getItemTitle(item: any): string {
    if (!item) return '';

    switch (this.type) {
      case 'FLIGHT':
        return item.airlineName || item.flightNumber || 'Unknown Flight';

      case 'HOTEL':
        return item.hotelName || 'Unknown Hotel';

      case 'TRANSPORT':
        return `${item.transportType || ''} ${item.transportNumber || ''}`;

      case 'PACKAGE':
        return item.packageName || 'Unknown Package';

      default:
        return 'Unknown Item';
    }
  }

  getItemSubtitle(item: any): string {
    if (!item) return '';

    switch (this.type) {
      case 'FLIGHT':
      case 'TRANSPORT':
      case 'PACKAGE':
        return `${item.source || ''} → ${item.destination || ''}`;

      case 'HOTEL':
        return item.city || '';

      default:
        return '';
    }
  }

  getCurrentPrice(): number {
    if (this.selectedDate && this.priceCalendar.length > 0) {
      const day = this.priceCalendar.find(d => d.date === this.selectedDate);
      if (day) return day.price;
    }
    return this.getItemPrice(this.item);
  }

  getItemPrice(item: any): number {
    if (!item) return 0;

    if (item.price) {
      return item.price;
    }

    if (item.seats?.length) {
      return Math.min(...item.seats.map((s: any) => s.price));
    }

    if (item.rooms?.length) {
      return Math.min(...item.rooms.map((r: any) => r.price));
    }

    return 0;
  }

  getFlightDuration(item: any): string {
    if (!item?.departureTime || !item?.arrivalTime) {
      return 'N/A';
    }

    const departure = new Date(`1970-01-01T${item.departureTime}`);
    const arrival = new Date(`1970-01-01T${item.arrivalTime}`);

    const diff = arrival.getTime() - departure.getTime();

    const hours = Math.floor(diff / (1000 * 60 * 60));
    const minutes = Math.floor(
      (diff % (1000 * 60 * 60)) / (1000 * 60)
    );

    return `${hours}h ${minutes}m`;
  }

  bookNow(): void {
    this.router.navigate(
      ['/book', this.type, this.id],
      {
        queryParams: {
          ...this.route.snapshot.queryParams,
          category: this.selectedCategory
        }
      }
    );
  }
}
