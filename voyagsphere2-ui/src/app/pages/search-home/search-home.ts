import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from '../../layout/navbar/navbar';
import { AgentContextService } from '../../core/services/agent-context';
import { AuthService } from '../../core/services/auth';

@Component({
  selector: 'app-search-home',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent],
  templateUrl: './search-home.html',
  styleUrl: './search-home.css'
})
export class SearchHomeComponent {

  selectedType = 'FLIGHT';

  searchForm = {
    source: '',
    destination: '',
    city: '',
    startDate: '', // We will map this to the API's expected 'date' query parameter
    endDate: '',
    travellers: 1
  };

  constructor(
    private router: Router,
    public agentContext: AgentContextService,
    public authService: AuthService
  ) {}

  switchType(type: string) {
    this.selectedType = type;
  }

search() {

  const clean = (str: string | null | undefined): string => {
    return str ? str.trim().replace(/\s+/g, ' ') : '';
  };

  
  const source = clean(this.searchForm.source);
  const destination = clean(this.searchForm.destination);
  const city = clean(this.searchForm.city);
  const startDate = clean(this.searchForm.startDate);
  const endDate = clean(this.searchForm.endDate);

  if (this.selectedType === 'FLIGHT') {
    if (!source || !destination || !startDate) {
      alert('Please enter valid source, destination, and a travel date');
      return;
    }

    this.router.navigate(['/results', this.selectedType], {
      queryParams: { source, destination, date: startDate }
    });

  } else if (this.selectedType === 'HOTEL') {
    if (!city || !startDate || !endDate) {
      alert('Please enter valid city, check-in date, and check-out date');
      return;
    }

    this.router.navigate(['/results', this.selectedType], {
      queryParams: { city, checkInDate: startDate, checkOutDate: endDate }
    });

  } else if (this.selectedType === 'TRANSPORT') {
    if (!source || !destination || !startDate) {
      alert('Please enter valid source, destination, and travel date');
      return;
    }

    this.router.navigate(['/results', this.selectedType], {
      queryParams: { source, destination, date: startDate }
    });

  } else if (this.selectedType === 'PACKAGE') {

    if (!source || !destination || !startDate) {
      alert('Please enter valid source, destination, and a travel date');
      return;
    }
    this.router.navigate(['/results', this.selectedType], {
      queryParams: {
        source: source || null,
        destination: destination || null,
        date: startDate || null
      }
    });
  }
}

goToHotels(): void {
  this.router.navigate(
    ['/results', 'HOTEL'],
    {
      queryParams: {
        city: 'Mumbai',
        checkInDate: '2026-08-01',
        checkOutDate: '2026-08-05',
        page: 0,
        size: 5
      }
    }
  );
}

goToPackages(): void {
  this.router.navigate(
    ['/results', 'PACKAGE'],
    {
      queryParams: {
        page: 0,
        size: 5
      }
    }
  );
}

}