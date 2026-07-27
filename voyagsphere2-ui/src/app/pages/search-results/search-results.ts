import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { NavbarComponent } from '../../layout/navbar/navbar';
import { SearchFilter } from '../../shared/components/search-filter/search-filter';
import { SearchService } from '../../core/services/search';
import { AgentContextService } from '../../core/services/agent-context';
import { AuthService } from '../../core/services/auth';

@Component({
  selector: 'app-search-results',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NavbarComponent,
    SearchFilter
  ],
  templateUrl: './search-results.html',
  styleUrls: ['./search-results.css']
})
export class SearchResultsComponent implements OnInit {

  type = '';
  sortBy = 'PRICE';

  sortResults(): void {
    if (!this.results || this.results.length === 0) return;

    if (this.sortBy === 'PRICE') {
      this.results = [...this.results].sort((a, b) => this.getItemPrice(a) - this.getItemPrice(b));
    } else if (this.sortBy === 'RATING') {
      this.results = [...this.results].sort((a, b) => (b.ratings || 0) - (a.ratings || 0));
    }
    this.cdr.detectChanges();
  }

  onSortChange(value: string): void {
    this.sortBy = value;
    this.sortResults();
  }

  results: any[] = [];
  
  // New properties to manage the pagination state metadata
  currentPage = 0;
pageSize = 5;

totalPages = 0;
totalElements = 0;

isFirst = true;
isLast = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private searchService: SearchService,
    private cdr: ChangeDetectorRef,
    public agentContext: AgentContextService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.type = params.get('type') || '';

      this.route.queryParams.subscribe(queryParams => {
        // Read existing pagination parameters from URL if present, otherwise default
        this.currentPage = +queryParams['page'] || 0;
        this.pageSize = +queryParams['size'] || 5;
        
        this.performSearch(queryParams);
      });
    });
  }

  performSearch(filters: any): void {
    console.log('Searching...', this.type, filters);

    // Ensure page and size properties are passed downstream
    const searchFilters = {
      page: this.currentPage,
      size: this.pageSize,
      ...filters
    };

    this.searchService.search(this.type, searchFilters).subscribe({
      next: (data: any) => {

  console.log('Search response wrapper:', data);

  if (data && data.content) {

    this.results = data.content;

    this.sortResults();

    this.currentPage = data.number;
    this.totalPages = data.totalPages;
    this.totalElements = data.totalElements;

    this.isFirst = data.first;
    this.isLast = data.last;

  } else if (Array.isArray(data)) {

    this.results = data;
    this.sortResults();
    this.totalPages = 1;
    this.totalElements = data.length;

    this.isFirst = true;
    this.isLast = true;

  } else {

    this.results = [];
    this.totalPages = 0;
    this.totalElements = 0;
  }

  this.cdr.detectChanges();
},
      error: (err) => {
        console.error('Search failed:', err);
        this.results = [];
        this.cdr.detectChanges();
      }
    });
  }

  onFilterChange(filters: any): void {
    console.log('Filter Changed:', filters);
    // Reset back to page 0 whenever filter conditions change
    this.currentPage = 0; 
    
    this.updateQueryParams({
      ...filters,
      page: this.currentPage
    });
  }

  // Method to navigate pages from HTML pagination controls
  onPageChange(newPage: number): void {
    if (newPage >= 0 && newPage < this.totalPages) {
      this.currentPage = newPage;
      this.updateQueryParams({ page: this.currentPage });
    }
  }

  private updateQueryParams(updatedParams: any): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        ...this.route.snapshot.queryParams,
        ...updatedParams
      },
      queryParamsHandling: 'merge'
    });
  }

  getItemTitle(item: any): string {
    switch (this.type.toUpperCase()) {
      case 'FLIGHT':
        return item.airlineName || item.flightNumber || 'Unknown Flight';
      case 'HOTEL':
        return item.hotelName || 'Unknown Hotel';
      case 'TRANSPORT':
        return `${item.transportType} ${item.transportNumber || ''}`;
      case 'PACKAGE':
        return item.packageName || 'Unknown Package';
      default:
        return 'Unknown Item';
    }
  }

  getItemSubtitle(item: any): string {
    switch (this.type.toUpperCase()) {
      case 'FLIGHT':
        return `${item.source} → ${item.destination}`;
      case 'HOTEL':
        return item.city || 'City not specified';
      case 'TRANSPORT':
        return `${item.source} → ${item.destination}`;
      case 'PACKAGE':
        return `${item.source} → ${item.destination}`;
      default:
        return '';
    }
  }

  getItemPrice(item: any): number {

  if (item.price) {
    return item.price;
  }

  // Flight seats
  if (item.seats?.length) {
    return Math.min(
      ...item.seats.map((seat: any) => Number(seat.price || 0))
    );
  }

  // Hotel rooms
  if (item.rooms?.length) {
    return Math.min(
      ...item.rooms.map((room: any) => Number(room.price || 0))
    );
  }

  return 0;
}

  viewDetails(id: number): void {
    this.router.navigate(
      ['/details', this.type, id],
      {
        queryParams: this.route.snapshot.queryParams
      }
    );
  }

  get pages(): number[] {
  return Array.from(
    { length: this.totalPages },
    (_, i) => i
  );
}

nextPage(): void {
  if (!this.isLast) {
    this.currentPage++;

    this.updateQueryParams({
      page: this.currentPage,
      size: this.pageSize
    });
  }
}

previousPage(): void {
  if (!this.isFirst) {
    this.currentPage--;

    this.updateQueryParams({
      page: this.currentPage,
      size: this.pageSize
    });
  }
}

goToPage(page: number): void {
  this.currentPage = page;

  this.updateQueryParams({
    page,
    size: this.pageSize
  });
}
}