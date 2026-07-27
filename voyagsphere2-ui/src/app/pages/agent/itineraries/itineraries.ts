import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AgentNavbar } from '../../../layout/agent-navbar/agent-navbar';
import { ItineraryService } from '../../../core/services/itinerary';
import { AgentContextService } from '../../../core/services/agent-context';
import { ItineraryResponseDTO } from '../../../core/models/travel.model';

@Component({
  selector: 'app-itineraries',
  standalone: true,
  imports: [CommonModule, AgentNavbar,RouterLink],
  templateUrl: './itineraries.html',
  styleUrl: './itineraries.css',
})
export class Itineraries implements OnInit {
  itineraries: ItineraryResponseDTO[] = [];
  isLoading = true;

  constructor(
    private itineraryService: ItineraryService,
    public agentContext: AgentContextService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCustomerItineraries();
  }

  loadCustomerItineraries(): void {
    if (!this.agentContext.selectedCustomerId) {
      this.isLoading = false;
      return;
    }

    this.itineraryService.getMyItineraries(this.agentContext.selectedCustomerId).subscribe({
      next: (data) => {
        this.itineraries = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading customer itineraries:', err);
        this.isLoading = false;
      }
    });
  }

  viewDetails(itineraryId: number): void {
    this.router.navigate(['/agent/itinerary-details', itineraryId]);
  }
}
