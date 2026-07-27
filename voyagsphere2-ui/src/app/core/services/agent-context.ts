

import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AgentContextService {

  selectedCustomerId?: number;
  selectedCustomerEmail?: string;

  setCustomer(id: number, email: string): void {
    this.selectedCustomerId = id;
    this.selectedCustomerEmail = email;
  }

  clear(): void {
    this.selectedCustomerId = undefined;
    this.selectedCustomerEmail = undefined;
  }
}