import { Component, EventEmitter, Output, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-search-filter',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './search-filter.html',
  styleUrl: './search-filter.css',
})
export class SearchFilter {
  @Input() type: string = '';
  minPrice: number = 0;
  maxPrice: number = 100000;
  category: string = '';

  @Output() filterChanged = new EventEmitter<{ min?: number; max?: number; category?: string; ratings?: number }>();

  minRating: number = 0;

  setRating(rating: number) {
    this.minRating = rating;
    this.updateFilters();
  }

  updateFilters() {
    this.filterChanged.emit({
      min: this.minPrice,
      max: this.maxPrice,
      category: this.category,
      ratings: this.minRating || undefined
    });
  }
}
